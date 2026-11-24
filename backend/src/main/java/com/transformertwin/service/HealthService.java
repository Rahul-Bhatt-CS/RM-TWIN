package com.transformertwin.service;

import com.transformertwin.domain.*;
import com.transformertwin.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;import java.util.stream.Collectors;

/** Transparent demonstration index. Every anchor/weight is a configurable project assumption, not an IEC/CEA limit. */
@Service
public class HealthService {
 private static final BigDecimal H=bd("100");
 private static final LinkedHashMap<String,BigDecimal> W=new LinkedHashMap<>();
 static{W.put("THERMAL",bd("0.2632"));W.put("OIL_DGA",bd("0.3158"));W.put("INSULATION",bd("0.2632"));W.put("MECHANICAL",bd("0.1578"));}
 private final SensorReadingRepository readings;private final DiagnosticTestRepository diagnostics;private final HealthAssessmentRepository assessments;private final AlertService alerts;
 @Value("${app.health.minimum-coverage:0.40}") private BigDecimal minCoverage;
 @Value("${app.health.telemetry-stale-seconds:30}") private long staleSeconds;
 @Value("${app.health.dga-fresh-days:30}") private long dgaFreshDays;
 @Value("${app.health.oil-test-fresh-days:180}") private long oilFreshDays;
 @Value("${app.demo.warning-hotspot-c:95}") private BigDecimal warningHot;
 @Value("${app.demo.critical-hotspot-c:110}") private BigDecimal criticalHot;
 @Value("${app.demo.warning-load-percent:100}") private BigDecimal warningLoad;
 public HealthService(SensorReadingRepository r,DiagnosticTestRepository d,HealthAssessmentRepository a,AlertService al){readings=r;diagnostics=d;assessments=a;alerts=al;}
 @Transactional public HealthAssessment assess(Transformer t){
  Instant now=Instant.now();List<HealthComponentScore> parts=new ArrayList<>();boolean warn=false,critical=false;
  Optional<SensorReading> hot=latest(t,MeasurementType.WINDING_HOTSPOT_TEMPERATURE),oil=latest(t,MeasurementType.TOP_OIL_TEMPERATURE),load=latest(t,MeasurementType.LOAD_PERCENT),ambient=latest(t,MeasurementType.AMBIENT_TEMPERATURE),fan=latest(t,MeasurementType.COOLING_FAN_STATUS),pump=latest(t,MeasurementType.COOLING_PUMP_STATUS),oilLevel=latest(t,MeasurementType.OIL_LEVEL_PERCENT);
  List<BigDecimal> thermal=new ArrayList<>();List<String> ev=new ArrayList<>();
  addFresh(thermal,ev,hot,now,"Hot-spot",v->down(v,bd("80"),bd("95"),bd("110")));
  addFresh(thermal,ev,oil,now,"Top-oil",v->down(v,bd("65"),bd("80"),bd("95")));
  if(thermal.size()==2)add(parts,"THERMAL","Thermal condition",avg(thermal),"SIMULATED_SCENARIO",String.join("; ",ev)+". Load is treated as operating stress and an alert input, not as a direct asset-health score. Ambient is context only.");
  else add(parts,"THERMAL","Thermal condition",null,"INSUFFICIENT_DATA","Both fresh top-oil and winding hot-spot measurements are required for this demonstration thermal score.");
  // Freshness: do not let stale telemetry continue to look live.
  for(MeasurementType m:List.of(MeasurementType.TOP_OIL_TEMPERATURE,MeasurementType.WINDING_HOTSPOT_TEMPERATURE,MeasurementType.LOAD_PERCENT)){
   Optional<SensorReading> r=latest(t,m);if(r.isEmpty()||r.get().getRecordedAt().isBefore(now.minusSeconds(staleSeconds))){warn=true;alerts.raise(t,"SENSOR_STALE_"+m.name(),"Monitoring data stale",label(m)+" has no fresh reading within "+staleSeconds+" seconds. Health assessment coverage may be incomplete.","WARNING",r.map(SensorReading::getValue).orElse(null),r.map(SensorReading::getUnit).orElse(null));}else alerts.resolve(t,"SENSOR_STALE_"+m.name(),"Fresh reading received at "+r.get().getRecordedAt()+" with value "+r.get().getValue()+" "+r.get().getUnit()+".");
  }
  if(hot.isPresent()&&fresh(hot.get(),now)){BigDecimal v=hot.get().getValue();if(v.compareTo(criticalHot)>=0){critical=true;alerts.raise(t,"HOTSPOT_CRITICAL","Critical winding hot-spot","Configured demonstration trigger crossed; this is not a trip command or OEM limit.","CRITICAL",v,"°C");alerts.resolve(t,"HOTSPOT_WARNING");}else{alerts.resolve(t,"HOTSPOT_CRITICAL","Latest winding hot-spot reading "+v+" °C is below the configured critical trigger "+criticalHot+" °C.");if(v.compareTo(warningHot)>=0){warn=true;alerts.raise(t,"HOTSPOT_WARNING","High winding hot-spot","Configured demonstration warning trigger crossed.","WARNING",v,"°C");}else alerts.resolve(t,"HOTSPOT_WARNING","Latest winding hot-spot reading "+v+" °C is below the configured warning trigger "+warningHot+" °C.");}}
  if(load.isPresent()&&fresh(load.get(),now)&&load.get().getValue().compareTo(warningLoad)>=0){warn=true;alerts.raise(t,"LOAD_HIGH","High transformer loading","Configured demonstration loading trigger crossed.","WARNING",load.get().getValue(),"%");}else alerts.resolve(t,"LOAD_HIGH",load.isPresent()?"Latest load reading is "+load.get().getValue()+"%, below the configured warning trigger "+warningLoad+"%.":"No current load reading is available; alert condition cannot be confirmed.");
  String cooling=Objects.toString(t.getCoolingStages(),Objects.toString(t.getCoolingType(),"ONAN/ONAF")).toUpperCase(Locale.ROOT);boolean fanInstalled=cooling.contains("ONAF")||cooling.contains("OFAF")||cooling.contains("ODAF");boolean pumpInstalled=cooling.contains("OFAF")||cooling.contains("ODAF")||cooling.contains("OFWF");
  if(fanInstalled&&fan.isPresent()&&fresh(fan.get(),now)&&fan.get().getValue().signum()==0&&load.map(x->x.getValue().compareTo(bd("65"))>=0).orElse(false)){warn=true;alerts.raise(t,"COOLING_FAN_UNAVAILABLE","Cooling fan unavailable under demand","Fan is reported OFF while the configured forced-cooling stage is demanded.","WARNING",fan.get().getValue(),fan.get().getUnit());}else alerts.resolve(t,"COOLING_FAN_UNAVAILABLE","Latest cooling evaluation no longer detects an unavailable forced-air fan under demand.");
  if(pumpInstalled&&pump.isPresent()&&fresh(pump.get(),now)&&pump.get().getValue().signum()==0&&load.map(x->x.getValue().compareTo(bd("65"))>=0).orElse(false)){warn=true;alerts.raise(t,"COOLING_PUMP_UNAVAILABLE","Cooling pump unavailable under demand","Pump is reported OFF while the configured forced-oil/water cooling stage is demanded.","WARNING",pump.get().getValue(),pump.get().getUnit());}else alerts.resolve(t,"COOLING_PUMP_UNAVAILABLE","Latest cooling evaluation no longer detects an unavailable pump under demand.");
  if(oilLevel.isPresent()&&fresh(oilLevel.get(),now)&&oilLevel.get().getValue().compareTo(bd("85"))<0){warn=true;alerts.raise(t,"OIL_LEVEL_LOW","Low conservator oil indication","Simulated oil-level indication is below the project screening anchor; confirm gauge and oil temperature.","WARNING",oilLevel.get().getValue(),"%");}else alerts.resolve(t,"OIL_LEVEL_LOW",oilLevel.isPresent()?"Latest oil-level indication is "+oilLevel.get().getValue()+"%, at or above the configured project screening anchor.":"No current oil-level reading is available.");

  // Pull a bounded recent window. Offline test freshness is applied per test family.
  List<DiagnosticTest> all=diagnostics.findByTransformerIdOrderByTestDateDesc(t.getId(),PageRequest.of(0,500)).getContent();
  List<DiagnosticTest> current=all.stream().filter(d->diagnosticFresh(d,now)).toList();
  List<DiagnosticTest> oilTests=current.stream().filter(d->Set.of("OIL_TEST").contains(d.getTestType().toUpperCase(Locale.ROOT))).toList();
  List<DiagnosticTest> dga=current.stream().filter(d->d.getTestType().equalsIgnoreCase("DGA")).toList();
  List<DiagnosticTest> ins=current.stream().filter(d->Set.of("INSULATION_TEST","DIELECTRIC_TEST","BUSHING_TEST").contains(d.getTestType().toUpperCase(Locale.ROOT))).toList();
  List<DiagnosticTest> mech=current.stream().filter(d->Set.of("FRA_TEST","ELECTRICAL_TEST","OLTC_TEST","COOLING_TEST").contains(d.getTestType().toUpperCase(Locale.ROOT))).toList();
  Group oilGroup=score(oilTests,this::oilScore,"Oil quality scores use project screening anchors. IEC 60422:2024 provides oil supervision guidance; actual limits depend on equipment, sampling and test method.");
  // DGA concentrations are intentionally not averaged into a single scalar score. Interpret concentrations, ratios, trends and sampling context together.
  if(!dga.isEmpty())oilGroup=new Group(oilGroup.score,oilGroup.score==null?"DGA_REVIEW_REQUIRED":oilGroup.state,(oilGroup.explanation+" DGA samples are present ("+dga.size()+" gas records); no standalone DGA score is assigned without coherent sample grouping, trends and interpretation context.").trim());
  add(parts,"OIL_DGA","Oil quality and DGA",oilGroup.score,oilGroup.state,oilGroup.explanation);
  Group ig=score(ins,this::insulationScore,"Project screening anchors only; valid interpretation requires test voltage, temperature, connection, method and asset baseline.");add(parts,"INSULATION","Insulation condition",ig.score,ig.state,ig.explanation);
  Group mg=score(mech,this::mechanicalScore,"Project screening anchors only; FRA traces, phase comparisons and OEM baselines are required for engineering diagnosis.");add(parts,"MECHANICAL","Mechanical and electrical condition",mg.score,mg.state,mg.explanation);
  // Screening alerts only for oil-quality tests with known test family; DGA is not converted to a simplistic single-gas alarm.
  for(DiagnosticTest d:oilTests){String p=d.getParameterName().toUpperCase(Locale.ROOT);BigDecimal v=d.getValue();
   if(p.equals("BREAKDOWN_VOLTAGE")){if(v.compareTo(bd("50"))<0){warn=true;alerts.raise(t,"OIL_BDV_LOW","Low oil breakdown voltage","Below project screening anchor; verify sample handling, test method and applicable equipment criteria.","WARNING",v,d.getUnit());}else alerts.resolve(t,"OIL_BDV_LOW","Latest breakdown-voltage test result is "+v+" "+d.getUnit()+", at or above the project screening anchor.");}
   if(p.equals("MOISTURE")){if(v.compareTo(bd("20"))>0){warn=true;alerts.raise(t,"OIL_MOISTURE_HIGH","Elevated oil moisture","Above project screening anchor; interpret with oil temperature, water activity and paper moisture context.","WARNING",v,d.getUnit());}else alerts.resolve(t,"OIL_MOISTURE_HIGH","Latest moisture test result is "+v+" "+d.getUnit()+", at or below the project screening anchor.");}
  }
  BigDecimal weighted=BigDecimal.ZERO,coverage=BigDecimal.ZERO;for(HealthComponentScore c:parts)if(c.getScore()!=null){weighted=weighted.add(c.getScore().multiply(c.getWeight()));coverage=coverage.add(c.getWeight());}
  BigDecimal index=coverage.compareTo(minCoverage)>=0?weighted.divide(coverage,2,RoundingMode.HALF_UP):null;
  String status=index==null?"UNAVAILABLE":index.compareTo(bd("40"))<0?"CRITICAL":index.compareTo(bd("65"))<0?"WARNING":"NORMAL";if(critical)status="CRITICAL";else if(warn&&"NORMAL".equals(status))status="WARNING";
  String explain="HI = sum(category score × category weight) / sum(weights of assessed categories). Coverage="+coverage.multiply(H).setScale(0,RoundingMode.HALF_UP)+"% of configured weights. The original 25:30:25:15 relative proportions were normalized across the four scored groups after protection scoring was removed. Protection and auxiliary systems are excluded from the index because no validated condition evidence is implemented. DGA concentrations are not averaged as independent health scores. Status may be overridden by critical events. Project model, not IEC/CEA-prescribed formula.";
  Instant hour=now.truncatedTo(ChronoUnit.HOURS);
  HealthAssessment a=assessments.findByTransformerIdAndAssessmentHour(t.getId(),hour).orElse(null);
  if(a==null)a=new HealthAssessment(t,status,index,coverage,explain,now);else a.replaceWith(status,index,coverage,explain,now);
  parts.forEach(a::addComponent);return assessments.save(a);
 }
 private boolean diagnosticFresh(DiagnosticTest d,Instant now){long days=d.getTestType().equalsIgnoreCase("DGA")?dgaFreshDays:d.getTestType().equalsIgnoreCase("OIL_TEST")?oilFreshDays:730;return !d.getTestDate().isBefore(now.minus(days,java.time.temporal.ChronoUnit.DAYS));}
 private boolean fresh(SensorReading r,Instant n){return !r.getRecordedAt().isBefore(n.minusSeconds(staleSeconds));}
 private Optional<SensorReading> latest(Transformer t,MeasurementType m){return readings.findTopByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(t.getId(),m);}
 private interface Scorer{BigDecimal score(DiagnosticTest d);}
 private Group score(List<DiagnosticTest> ds,Scorer scorer,String caveat){
  Map<String,DiagnosticTest> latestByParameterComponentPhase=new LinkedHashMap<>();
  ds.stream().sorted(Comparator.comparing(DiagnosticTest::getTestDate)).forEach(d->{String key=d.getParameterName()+"|"+Objects.toString(d.getComponentKey(),"")+"|"+Objects.toString(d.getPhase(),"");latestByParameterComponentPhase.put(key,d);});
  Map<String,List<DiagnosticTest>> grouped=new LinkedHashMap<>();for(DiagnosticTest d:latestByParameterComponentPhase.values()){String key=d.getParameterName()+"|"+Objects.toString(d.getComponentKey(),"");grouped.computeIfAbsent(key,x->new ArrayList<>()).add(d);}
  List<BigDecimal> vals=new ArrayList<>();List<String> evidence=new ArrayList<>();for(List<DiagnosticTest> records:grouped.values()){List<BigDecimal> phaseScores=records.stream().map(scorer::score).filter(Objects::nonNull).toList();if(!phaseScores.isEmpty()){BigDecimal value=avg(phaseScores);vals.add(value);DiagnosticTest d=records.get(0);evidence.add(d.getParameterName()+" (latest evidence across "+records.size()+" phase record(s)) → "+value+"/100");}}
  return vals.isEmpty()?new Group(null,"INSUFFICIENT_DATA",ds.isEmpty()?"No current diagnostic evidence is available. "+caveat:"Records exist but no safe scoring rule is configured. "+caveat):new Group(avg(vals),latestByParameterComponentPhase.values().stream().anyMatch(x->x.getSource()==DataSourceType.SIMULATED)?"SIMULATED_SCENARIO":"ASSESSED",String.join("; ",evidence)+". "+caveat);
 }
 private BigDecimal oilScore(DiagnosticTest d){return switch(d.getParameterName().toUpperCase(Locale.ROOT)){case "MOISTURE"->down(d.getValue(),bd("10"),bd("20"),bd("35"));case "BREAKDOWN_VOLTAGE"->up(d.getValue(),bd("60"),bd("50"),bd("40"));case "ACIDITY"->down(d.getValue(),bd("0.10"),bd("0.20"),bd("0.40"));default->null;};}
 private BigDecimal insulationScore(DiagnosticTest d){return switch(d.getParameterName().toUpperCase(Locale.ROOT)){case "INSULATION_RESISTANCE"->up(d.getValue(),bd("5000"),bd("2000"),bd("500"));case "POLARIZATION_INDEX"->up(d.getValue(),bd("2.0"),bd("1.5"),bd("1.0"));case "WINDING_TAN_DELTA"->down(d.getValue(),bd("0.003"),bd("0.007"),bd("0.015"));case "FURAN_2_FAL"->null;case "BUSHING_TAN_DELTA"->down(d.getValue(),bd("0.003"),bd("0.007"),bd("0.015"));default->null;};}
 private BigDecimal mechanicalScore(DiagnosticTest d){return switch(d.getParameterName().toUpperCase(Locale.ROOT)){case "FRA_DEVIATION"->down(d.getValue(),bd("1"),bd("3"),bd("8"));case "FAN_VIBRATION"->down(d.getValue(),bd("2"),bd("4"),bd("8"));default->null;};}
 private static BigDecimal up(BigDecimal v,BigDecimal g,BigDecimal w,BigDecimal b){if(v.compareTo(g)>=0)return H;if(v.compareTo(b)<=0)return BigDecimal.ZERO;return v.compareTo(w)>=0?interp(v,g,w,H,bd("60")):interp(v,w,b,bd("60"),BigDecimal.ZERO);}
 private static BigDecimal down(BigDecimal v,BigDecimal g,BigDecimal w,BigDecimal b){if(v.compareTo(g)<=0)return H;if(v.compareTo(b)>=0)return BigDecimal.ZERO;return v.compareTo(w)<=0?interp(v,g,w,H,bd("60")):interp(v,w,b,bd("60"),BigDecimal.ZERO);}
 private static BigDecimal interp(BigDecimal v,BigDecimal x,BigDecimal y,BigDecimal a,BigDecimal b){return a.add(v.subtract(x).multiply(b.subtract(a)).divide(y.subtract(x),4,RoundingMode.HALF_UP)).max(BigDecimal.ZERO).min(H).setScale(2,RoundingMode.HALF_UP);}
 private void add(List<HealthComponentScore> p,String k,String l,BigDecimal s,String state,String e){p.add(new HealthComponentScore(k,l,s,W.get(k),state,e));}
 private void addFresh(List<BigDecimal> scores,List<String> evidence,Optional<SensorReading> r,Instant now,String label,java.util.function.Function<BigDecimal,BigDecimal> f){if(r.isPresent()&&fresh(r.get(),now)){scores.add(f.apply(r.get().getValue()));evidence.add(label+"="+r.get().getValue()+" "+r.get().getUnit());}}
 private static BigDecimal avg(List<BigDecimal> x){return x.stream().reduce(BigDecimal.ZERO,BigDecimal::add).divide(BigDecimal.valueOf(x.size()),2,RoundingMode.HALF_UP);}
 private static BigDecimal bd(String s){return new BigDecimal(s);}private static String label(MeasurementType m){return m.name().replace('_',' ').toLowerCase(Locale.ROOT);}
 private record Group(BigDecimal score,String state,String explanation){}
}
