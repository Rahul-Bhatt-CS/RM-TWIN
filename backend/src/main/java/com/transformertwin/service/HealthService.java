package com.transformertwin.service;

import com.transformertwin.domain.*;
import com.transformertwin.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

/** Explainable project scoring model. Limits and weights are demonstration assumptions, not standards limits. */
@Service
public class HealthService {
    private static final BigDecimal HUNDRED = bd("100");
    private static final LinkedHashMap<String, BigDecimal> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put("THERMAL", bd("0.25")); WEIGHTS.put("OIL_DGA", bd("0.30"));
        WEIGHTS.put("INSULATION", bd("0.25")); WEIGHTS.put("MECHANICAL", bd("0.15"));
        WEIGHTS.put("PROTECTION", bd("0.05"));
    }
    private final SensorReadingRepository readings;
    private final DiagnosticTestRepository diagnostics;
    private final ProtectionEventRepository protections;
    private final HealthAssessmentRepository assessments;
    private final AlertService alerts;
    @Value("${app.health.minimum-coverage:0.60}") private BigDecimal minimumCoverage;
    @Value("${app.demo.warning-hotspot-c:95}") private BigDecimal warningHotspot;
    @Value("${app.demo.critical-hotspot-c:110}") private BigDecimal criticalHotspot;
    @Value("${app.demo.warning-load-percent:100}") private BigDecimal warningLoad;

    public HealthService(SensorReadingRepository readings, DiagnosticTestRepository diagnostics,
                         ProtectionEventRepository protections, HealthAssessmentRepository assessments, AlertService alerts) {
        this.readings=readings; this.diagnostics=diagnostics; this.protections=protections; this.assessments=assessments; this.alerts=alerts;
    }

    @Transactional
    public HealthAssessment assess(Transformer t) {
        Instant now=Instant.now();
        List<HealthComponentScore> parts=new ArrayList<>();
        BigDecimal weighted=BigDecimal.ZERO, coverage=BigDecimal.ZERO;
        boolean warning=false, critical=false;

        // Thermal category: score each available telemetry parameter, then average available scores.
        List<BigDecimal> thermal=new ArrayList<>(); List<String> thermalEvidence=new ArrayList<>();
        Optional<SensorReading> hot=latest(t,MeasurementType.WINDING_HOTSPOT_TEMPERATURE);
        Optional<SensorReading> oil=latest(t,MeasurementType.TOP_OIL_TEMPERATURE);
        Optional<SensorReading> load=latest(t,MeasurementType.LOAD_PERCENT);
        Optional<SensorReading> ambient=latest(t,MeasurementType.AMBIENT_TEMPERATURE);
        Optional<SensorReading> fan=latest(t,MeasurementType.COOLING_FAN_STATUS);
        Optional<SensorReading> pump=latest(t,MeasurementType.COOLING_PUMP_STATUS);
        Optional<SensorReading> oilLevel=latest(t,MeasurementType.OIL_LEVEL_PERCENT);
        hot.ifPresent(r->{thermal.add(descending(r.getValue(),bd("80"),bd("95"),bd("110")));thermalEvidence.add("Hot-spot="+r.getValue()+" °C");});
        oil.ifPresent(r->{thermal.add(descending(r.getValue(),bd("65"),bd("80"),bd("95")));thermalEvidence.add("Top-oil="+r.getValue()+" °C");});
        load.ifPresent(r->{thermal.add(descending(r.getValue(),bd("80"),bd("100"),bd("120")));thermalEvidence.add("Load="+r.getValue()+"%");});
        ambient.ifPresent(r->{thermal.add(descending(r.getValue(),bd("35"),bd("45"),bd("55")));thermalEvidence.add("Ambient="+r.getValue()+" °C");});
        fan.ifPresent(r->{boolean on=r.getValue().signum()>0;boolean coolingRequired=load.map(x->x.getValue().compareTo(bd("80"))>=0).orElse(true);thermal.add(on||!coolingRequired?HUNDRED:bd("55"));thermalEvidence.add("Cooling fan="+(on?"ON":"OFF")+(coolingRequired?" (cooling demand present)":" (no high-load demand)"));});
        pump.ifPresent(r->{thermal.add(r.getValue().compareTo(BigDecimal.ZERO)>0?HUNDRED:bd("55"));thermalEvidence.add("Cooling pump="+(r.getValue().signum()>0?"ON":"OFF"));});
        oilLevel.ifPresent(r->{thermal.add(ascending(r.getValue(),bd("90"),bd("80"),bd("65")));thermalEvidence.add("Oil level="+r.getValue()+"%");});
        if(!thermal.isEmpty()) add(parts,"THERMAL","Thermal and loading",average(thermal),"SIMULATED_SCENARIO",String.join("; ",thermalEvidence)+". Parameter scores use linear interpolation between project demo anchors.");
        else add(parts,"THERMAL","Thermal and loading",null,"UNKNOWN","No current thermal/loading telemetry.");

        // Sensor-driven alerts: alarm state is derived from latest readings, never manually entered.
        if(hot.isPresent()) {
            BigDecimal v=hot.get().getValue();
            if(v.compareTo(criticalHotspot)>=0){critical=true;alerts.raise(t,"HOTSPOT_CRITICAL","Critical winding hot-spot","Simulated hot-spot is at or above the configured critical demonstration trigger. Not a trip command.","CRITICAL",v,"°C");alerts.resolve(t,"HOTSPOT_WARNING");}
            else {alerts.resolve(t,"HOTSPOT_CRITICAL");if(v.compareTo(warningHotspot)>=0){warning=true;alerts.raise(t,"HOTSPOT_WARNING","High winding hot-spot","Simulated hot-spot crossed the configured warning demonstration trigger.","WARNING",v,"°C");}else alerts.resolve(t,"HOTSPOT_WARNING");}
        }
        if(load.isPresent()&&load.get().getValue().compareTo(warningLoad)>=0){warning=true;alerts.raise(t,"LOAD_HIGH","High transformer loading","Simulated loading crossed the configured demonstration trigger.","WARNING",load.get().getValue(),"%");}else alerts.resolve(t,"LOAD_HIGH");
        if(fan.isPresent()&&fan.get().getValue().signum()==0&&hot.isPresent()&&hot.get().getValue().compareTo(warningHotspot)>=0){warning=true;alerts.raise(t,"COOLING_ASSIST_UNAVAILABLE","Cooling fan unavailable during elevated temperature","Simulator reports fan OFF while winding hot-spot is elevated.","WARNING",fan.get().getValue(),"ON=1 / OFF=0");}else alerts.resolve(t,"COOLING_ASSIST_UNAVAILABLE");
        if(pump.isPresent()&&pump.get().getValue().signum()==0){warning=true;alerts.raise(t,"COOLING_PUMP_UNAVAILABLE","Cooling pump reported OFF","Simulator reports cooling pump OFF; applicability depends on installed cooling design.","WARNING",pump.get().getValue(),"ON=1 / OFF=0");}else alerts.resolve(t,"COOLING_PUMP_UNAVAILABLE");
        if(oilLevel.isPresent()&&oilLevel.get().getValue().compareTo(bd("85"))<0){warning=true;alerts.raise(t,"OIL_LEVEL_LOW","Low conservator oil level","Simulated oil-level indication fell below the project warning anchor.","WARNING",oilLevel.get().getValue(),"%");}else alerts.resolve(t,"OIL_LEVEL_LOW");

        List<DiagnosticTest> tests=diagnostics.findTop100ByTransformerIdOrderByTestDateDesc(t.getId());
        List<DiagnosticTest> oilTests=filter(tests,"DGA","OIL_TEST");
        List<DiagnosticTest> insulationTests=filter(tests,"INSULATION_TEST","DIELECTRIC_TEST","BUSHING_TEST");
        List<DiagnosticTest> mechanicalTests=filter(tests,"FRA_TEST","ELECTRICAL_TEST","PARTIAL_DISCHARGE_TEST","OLTC_TEST","COOLING_TEST");
        Group oilGroup=scoreGroup(oilTests,"OIL_DGA","Oil and dissolved-gas condition",this::oilScore,"Oil and DGA scores are indicative project rules. DGA diagnosis must consider trends, ratios, sampling and asset baseline.");
        add(parts,"OIL_DGA","Oil and dissolved-gas condition",oilGroup.score,oilGroup.state,oilGroup.explanation);
        Group insGroup=scoreGroup(insulationTests,"INSULATION","Insulation condition",this::insulationScore,"Insulation scores are indicative project rules; compare actual test conditions and OEM baseline before engineering interpretation.");
        add(parts,"INSULATION","Insulation condition",insGroup.score,insGroup.state,insGroup.explanation);
        Group mechGroup=scoreGroup(mechanicalTests,"MECHANICAL","Mechanical and electrical condition",this::mechanicalScore,"Mechanical/electrical scores are indicative project rules and do not replace FRA trace comparison, phase analysis or OEM limits.");
        add(parts,"MECHANICAL","Mechanical and electrical condition",mechGroup.score,mechGroup.state,mechGroup.explanation);

        // Diagnostic-triggered alerts are generated automatically from seeded or imported diagnostic records.
        for(DiagnosticTest d:latestPerParameter(tests)){String p=d.getParameterName().toUpperCase(Locale.ROOT);BigDecimal v=d.getValue();
            if(p.equals("ACETYLENE")){if(v.compareTo(bd("2"))>=0){warning=true;if(v.compareTo(bd("10"))>=0)critical=true;alerts.raise(t,"DGA_ACETYLENE","Acetylene detected in DGA","Recorded acetylene is at/above the project screening anchor; investigate with an appropriate DGA method.",v.compareTo(bd("10"))>=0?"CRITICAL":"WARNING",v,d.getUnit());}else alerts.resolve(t,"DGA_ACETYLENE");}
            if(p.equals("BREAKDOWN_VOLTAGE")){if(v.compareTo(bd("50"))<0){warning=true;alerts.raise(t,"OIL_BDV_LOW","Low oil breakdown voltage","Recorded BDV is below the project screening anchor.","WARNING",v,d.getUnit());}else alerts.resolve(t,"OIL_BDV_LOW");}
            if(p.equals("MOISTURE")){if(v.compareTo(bd("20"))>0){warning=true;alerts.raise(t,"OIL_MOISTURE_HIGH","Elevated oil moisture","Recorded moisture is above the project screening anchor.","WARNING",v,d.getUnit());}else alerts.resolve(t,"OIL_MOISTURE_HIGH");}
            if(p.equals("FAN_VIBRATION")){if(v.compareTo(bd("4"))>0){warning=true;alerts.raise(t,"FAN_VIBRATION_HIGH","Cooling fan vibration elevated","Recorded vibration is above the project demonstration anchor.","WARNING",v,d.getUnit());}else alerts.resolve(t,"FAN_VIBRATION_HIGH");}
        }
        if(tests.stream().noneMatch(x->x.getParameterName().equalsIgnoreCase("ACETYLENE")))alerts.resolve(t,"DGA_ACETYLENE");
        if(tests.stream().noneMatch(x->x.getParameterName().equalsIgnoreCase("BREAKDOWN_VOLTAGE")))alerts.resolve(t,"OIL_BDV_LOW");
        if(tests.stream().noneMatch(x->x.getParameterName().equalsIgnoreCase("MOISTURE")))alerts.resolve(t,"OIL_MOISTURE_HIGH");
        if(tests.stream().noneMatch(x->x.getParameterName().equalsIgnoreCase("FAN_VIBRATION")))alerts.resolve(t,"FAN_VIBRATION_HIGH");

        Page<ProtectionEvent> recent=protections.findByTransformerIdOrderByOccurredAtDesc(t.getId(),PageRequest.of(0,100));
        Optional<ProtectionEvent> activeEvent=recent.getContent().stream().filter(ProtectionEvent::isActive).findFirst();
        if(activeEvent.isPresent()){
            ProtectionEvent e=activeEvent.get();critical=true;
            String key="PROTECTION_EVENT_"+e.getDeviceName().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]","_");
            alerts.raise(t,key,"Active protection event reported",e.getDeviceName()+" reported "+e.getEventType()+". Review the source protection system and site procedure.","CRITICAL",null,null);
            add(parts,"PROTECTION","Protection and auxiliary systems",null,"ATTENTION","An active event is recorded; inactive synthetic indications do not prove protection health.");
        } else {
            alerts.resolve(t,"PROTECTION_EVENT_BUCHHOLZ_RELAY");
            Optional<ProtectionEvent> latestProtection=recent.getContent().stream().findFirst();
            if(latestProtection.isPresent()&&latestProtection.get().getSource()==DataSourceType.SIMULATED&&latestProtection.get().getEventType().startsWith("SIMULATED_"))
                add(parts,"PROTECTION","Protection and auxiliary systems",bd("95"),"SIMULATED_SCENARIO","Synthetic normal-indication records are available for dashboard demonstration only; they are not relay self-tests.");
            else add(parts,"PROTECTION","Protection and auxiliary systems",null,"UNKNOWN",recent.isEmpty()?"No protection-event indication is available.":"Recorded inactive events do not prove protection-system health.");
        }

        for(HealthComponentScore p:parts){if(p.getScore()!=null){weighted=weighted.add(p.getScore().multiply(p.getWeight()));coverage=coverage.add(p.getWeight());}}
        BigDecimal index=coverage.compareTo(minimumCoverage)>=0?weighted.divide(coverage,2,RoundingMode.HALF_UP):null;
        String status=index==null?"UNAVAILABLE":index.compareTo(bd("40"))<0?"CRITICAL":index.compareTo(bd("65"))<0?"WARNING":"NORMAL";
        if(critical)status="CRITICAL";else if(warning&&"NORMAL".equals(status))status="WARNING";
        String explanation=index==null?"Insufficient assessed category coverage to calculate the Health Index. Missing data is not treated as healthy.":
                "Health Index = sum(category score × category weight) / sum(weights with assessed data). Assessed coverage: "+coverage.multiply(HUNDRED).setScale(0,RoundingMode.HALF_UP)+"%. Weights: Thermal 25%, Oil/DGA 30%, Insulation 25%, Mechanical/Electrical 15%, Protection/Auxiliary 5%. Critical alarms override the displayed status, not the numeric index.";
        HealthAssessment assessment=new HealthAssessment(t,status,index,coverage,explanation,now);parts.forEach(assessment::addComponent);return assessments.save(assessment);
    }

    private interface Scorer{BigDecimal score(DiagnosticTest d);}
    private Group scoreGroup(List<DiagnosticTest> group,String key,String label,Scorer scorer,String caveat){
        List<BigDecimal> scores=new ArrayList<>();List<String> evidence=new ArrayList<>();
        for(DiagnosticTest d:latestPerParameter(group)){BigDecimal s=scorer.score(d);if(s!=null){scores.add(s);evidence.add(d.getParameterName()+"="+d.getValue()+" "+d.getUnit()+" → "+s+"/100");}}
        if(scores.isEmpty())return new Group(null,"UNKNOWN",group.isEmpty()?"No records available.":"Records exist, but no scoring rule is configured for these parameters. "+caveat);
        return new Group(average(scores),group.stream().anyMatch(x->x.getSource()==DataSourceType.SIMULATED)?"SIMULATED_SCENARIO":"ASSESSED",String.join("; ",evidence)+". "+caveat);
    }
    private BigDecimal oilScore(DiagnosticTest d){String p=d.getParameterName().toUpperCase(Locale.ROOT);BigDecimal v=d.getValue();return switch(p){
        case "HYDROGEN"->descending(v,bd("50"),bd("150"),bd("300"));case "METHANE"->descending(v,bd("30"),bd("130"),bd("250"));
        case "ETHANE"->descending(v,bd("20"),bd("90"),bd("180"));case "ETHYLENE"->descending(v,bd("20"),bd("100"),bd("280"));
        case "ACETYLENE"->descending(v,bd("0.5"),bd("2"),bd("10"));case "CARBON_MONOXIDE"->descending(v,bd("350"),bd("600"),bd("1000"));
        case "CARBON_DIOXIDE"->descending(v,bd("2500"),bd("5000"),bd("10000"));case "MOISTURE"->descending(v,bd("10"),bd("20"),bd("35"));
        case "BREAKDOWN_VOLTAGE"->ascending(v,bd("60"),bd("50"),bd("40"));case "ACIDITY"->descending(v,bd("0.10"),bd("0.20"),bd("0.40"));
        case "OIL_LEVEL_GAUGE_CHECK"->ascending(v,bd("95"),bd("85"),bd("70"));default->null;};}
    private BigDecimal insulationScore(DiagnosticTest d){String p=d.getParameterName().toUpperCase(Locale.ROOT);BigDecimal v=d.getValue();return switch(p){
        case "INSULATION_RESISTANCE"->ascending(v,bd("5000"),bd("2000"),bd("500"));case "POLARIZATION_INDEX"->ascending(v,bd("2.0"),bd("1.5"),bd("1.0"));
        case "WINDING_TAN_DELTA","BUSHING_TAN_DELTA"->descending(v,bd("0.003"),bd("0.007"),bd("0.015"));
        case "FURAN_2_FAL"->descending(v,bd("0.1"),bd("1.0"),bd("5.0"));default->null;};}
    private BigDecimal mechanicalScore(DiagnosticTest d){String p=d.getParameterName().toUpperCase(Locale.ROOT);BigDecimal v=d.getValue();return switch(p){
        case "FRA_DEVIATION"->descending(v,bd("1"),bd("3"),bd("8"));
        case "APPARENT_CHARGE"->descending(v,bd("50"),bd("100"),bd("300"));case "CONTACT_RESISTANCE"->descending(v,bd("80"),bd("100"),bd("150"));
        case "OPERATING_TIME"->descending(v,bd("60"),bd("80"),bd("120"));case "RADIATOR_TEMPERATURE_DIFFERENCE"->descending(v,bd("10"),bd("20"),bd("30"));
        case "FAN_VIBRATION"->descending(v,bd("2"),bd("4"),bd("8"));default->null;};}
    /** Higher-is-better interpolation: full score at good, zero at bad. */
    private static BigDecimal ascending(BigDecimal v,BigDecimal good,BigDecimal warn,BigDecimal bad){if(v.compareTo(good)>=0)return HUNDRED;if(v.compareTo(bad)<=0)return BigDecimal.ZERO;if(v.compareTo(warn)>=0)return interpolate(v,good,warn,bd("100"),bd("60"));return interpolate(v,warn,bad,bd("60"),BigDecimal.ZERO);}
    /** Lower-is-better interpolation: full score at good, zero at bad. */
    private static BigDecimal descending(BigDecimal v,BigDecimal good,BigDecimal warn,BigDecimal bad){if(v.compareTo(good)<=0)return HUNDRED;if(v.compareTo(bad)>=0)return BigDecimal.ZERO;if(v.compareTo(warn)<=0)return interpolate(v,good,warn,bd("100"),bd("60"));return interpolate(v,warn,bad,bd("60"),BigDecimal.ZERO);}
    private static BigDecimal interpolate(BigDecimal v,BigDecimal x1,BigDecimal x2,BigDecimal y1,BigDecimal y2){return y1.add(v.subtract(x1).multiply(y2.subtract(y1)).divide(x2.subtract(x1),4,RoundingMode.HALF_UP)).max(BigDecimal.ZERO).min(HUNDRED).setScale(2,RoundingMode.HALF_UP);}
    private void add(List<HealthComponentScore> parts,String key,String label,BigDecimal score,String state,String explanation){parts.add(new HealthComponentScore(key,label,score,WEIGHTS.get(key),state,explanation));}
    private List<DiagnosticTest> latestPerParameter(List<DiagnosticTest> xs){
        Set<String> seen=new HashSet<>();List<DiagnosticTest> result=new ArrayList<>();
        for(DiagnosticTest d:xs){String key=d.getParameterName().toUpperCase(Locale.ROOT)+"|"+Objects.toString(d.getComponentKey(),"")+"|"+Objects.toString(d.getPhase(),"");if(seen.add(key))result.add(d);}
        return result;
    }
    private List<DiagnosticTest> filter(List<DiagnosticTest> xs,String... types){Set<String>s=Set.of(types);return xs.stream().filter(x->s.contains(x.getTestType().toUpperCase(Locale.ROOT))).toList();}
    private Optional<SensorReading> latest(Transformer t,MeasurementType m){return readings.findTopByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(t.getId(),m);}
    private static BigDecimal average(List<BigDecimal> xs){return xs.stream().reduce(BigDecimal.ZERO,BigDecimal::add).divide(BigDecimal.valueOf(xs.size()),2,RoundingMode.HALF_UP);}
    private static BigDecimal bd(String s){return new BigDecimal(s);}
    private record Group(BigDecimal score,String state,String explanation){}
}
