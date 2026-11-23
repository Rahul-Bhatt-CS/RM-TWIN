package com.transformertwin.service;
import com.transformertwin.domain.*; import com.transformertwin.repository.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.data.domain.Page; import org.springframework.data.domain.PageRequest; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.*; import java.time.Instant; import java.util.*;
@Service
public class HealthService {
 private final SensorReadingRepository readings; private final DiagnosticTestRepository diagnostics; private final ProtectionEventRepository protections;
 private final HealthAssessmentRepository assessments; private final AlertService alerts;
 @Value("${app.health.minimum-coverage:0.4}") private BigDecimal minimumCoverage;
 // Illustrative demo triggers only; replace with approved manufacturer/standard-based settings.
 @Value("${app.demo.warning-hotspot-c:95}") private BigDecimal warningHotspot;
 @Value("${app.demo.critical-hotspot-c:110}") private BigDecimal criticalHotspot;
 @Value("${app.demo.warning-load-percent:100}") private BigDecimal warningLoad;
 public HealthService(SensorReadingRepository r,DiagnosticTestRepository d,ProtectionEventRepository p,HealthAssessmentRepository a,AlertService al){readings=r;diagnostics=d;protections=p;assessments=a;alerts=al;}
 @Transactional public HealthAssessment assess(Transformer t){
  Instant now=Instant.now(); List<HealthComponentScore> parts=new ArrayList<>(); BigDecimal weighted=BigDecimal.ZERO,coverage=BigDecimal.ZERO; boolean warningDetected=false, criticalDetected=false;
  Optional<SensorReading> hot=latest(t,MeasurementType.WINDING_HOTSPOT_TEMPERATURE);
  Optional<SensorReading> load=latest(t,MeasurementType.LOAD_PERCENT);
  Optional<SensorReading> oil=latest(t,MeasurementType.TOP_OIL_TEMPERATURE);
  List<BigDecimal> thermalScores=new ArrayList<>(); List<String> thermalEvidence=new ArrayList<>();
  if(hot.isPresent()){
   BigDecimal v=hot.get().getValue(); BigDecimal s=v.compareTo(criticalHotspot)>=0?new BigDecimal("10"):v.compareTo(warningHotspot)>=0?new BigDecimal("45"):new BigDecimal("90");
   thermalScores.add(s);thermalEvidence.add("Hot spot "+v+" °C");
   if(v.compareTo(criticalHotspot)>=0){criticalDetected=true;alerts.raise(t,"HOTSPOT_CRITICAL","Critical winding hot-spot","Hot-spot crossed the configured demonstration trigger; this is not a trip command.","CRITICAL",v,"°C");alerts.resolve(t,"HOTSPOT_WARNING");}
   else {alerts.resolve(t,"HOTSPOT_CRITICAL");if(v.compareTo(warningHotspot)>=0){warningDetected=true;alerts.raise(t,"HOTSPOT_WARNING","High winding hot-spot","Hot-spot crossed the configured demonstration warning trigger.","WARNING",v,"°C");}else alerts.resolve(t,"HOTSPOT_WARNING");}
  }
  if(load.isPresent()){
   BigDecimal v=load.get().getValue();thermalScores.add(v.compareTo(warningLoad)>=0?new BigDecimal("40"):new BigDecimal("90"));thermalEvidence.add("Load "+v+"%");
   if(v.compareTo(warningLoad)>=0){warningDetected=true;alerts.raise(t,"LOAD_HIGH","High loading","Load crossed the configured demonstration trigger; duration and transformer loading guide are not modelled.","WARNING",v,"%");}else alerts.resolve(t,"LOAD_HIGH");
  }
  if(oil.isPresent()){thermalEvidence.add("Top oil "+oil.get().getValue()+" °C");}
  Optional<SensorReading> fan=latest(t,MeasurementType.COOLING_FAN_STATUS);
  if(fan.isPresent() && fan.get().getValue().compareTo(BigDecimal.ZERO)==0 && hot.isPresent() && hot.get().getValue().compareTo(warningHotspot)>=0){
   warningDetected=true;
   alerts.raise(t,"COOLING_ASSIST_UNAVAILABLE","Cooling fan not running during elevated hot-spot scenario","The demonstration simulator reports fan OFF while the hot-spot is above its configured demo warning trigger. Verify actual cooling requirements and auxiliary status on site.","WARNING",fan.get().getValue(),"ON=1 / OFF=0");
  } else { alerts.resolve(t,"COOLING_ASSIST_UNAVAILABLE"); }
  Optional<SensorReading> pump=latest(t,MeasurementType.COOLING_PUMP_STATUS);
  if(pump.isPresent() && pump.get().getValue().compareTo(BigDecimal.ZERO)==0){
   warningDetected=true;
   alerts.raise(t,"COOLING_PUMP_UNAVAILABLE","Cooling pump reported OFF","The demonstration telemetry reports pump OFF. Applicability depends on the installed cooling design.","WARNING",pump.get().getValue(),"ON=1 / OFF=0");
  } else { alerts.resolve(t,"COOLING_PUMP_UNAVAILABLE"); }
  if(!thermalScores.isEmpty()){BigDecimal s=average(thermalScores);parts.add(part("THERMAL","Thermal and loading",s,"0.25","ASSESSED",String.join("; ",thermalEvidence)+". Illustrative rule scores only."));weighted=weighted.add(s.multiply(new BigDecimal("0.25")));coverage=coverage.add(new BigDecimal("0.25"));}
  else parts.add(part("THERMAL","Thermal and loading",null,"0.25","UNKNOWN","No current thermal or load readings."));
  List<DiagnosticTest> tests=diagnostics.findTop100ByTransformerIdOrderByTestDateDesc(t.getId());
  Optional<DiagnosticTest> moisture=tests.stream().filter(x->x.getParameterName().equalsIgnoreCase("MOISTURE")).findFirst();
  if(!tests.isEmpty()){
   parts.add(part("OIL_DGA","Oil and dissolved-gas condition",null,"0.30","RECORDED_NOT_INTERPRETED","Diagnostic records exist, including moisture="+(moisture.isPresent()?moisture.get().getValue()+" "+moisture.get().getUnit():"not available")+". No automatic oil/DGA score is assigned: interpret against the applicable IEC 60599 / IEEE C57.104 method, sampling context, trend and asset baseline."));
  }else parts.add(part("OIL_DGA","Oil and dissolved-gas condition",null,"0.30","UNKNOWN","No oil/DGA test record is available. Missing evidence is not treated as healthy."));
  parts.add(part("INSULATION","Insulation condition",null,"0.25","UNKNOWN","No validated insulation assessment method or baseline configured."));
  parts.add(part("MECHANICAL","Mechanical and electrical condition",null,"0.15","UNKNOWN","No FRA, winding-resistance, bushing or partial-discharge scoring configured."));
  Page<ProtectionEvent> recent=protections.findByTransformerIdOrderByOccurredAtDesc(t.getId(),PageRequest.of(0,1));
  if(!recent.isEmpty()){
   ProtectionEvent event=recent.getContent().get(0);
   String protectionKey="PROTECTION_EVENT_"+event.getDeviceName().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]","_");
   if(event.isActive()){
    criticalDetected=true;
    alerts.raise(t,protectionKey,"Active protection event reported",event.getDeviceName()+" reported "+event.getEventType()+". Review the original protection system and site procedure.","CRITICAL",null,null);
    parts.add(part("PROTECTION","Protection and auxiliary systems",null,"0.05","ATTENTION","An active protection event is recorded. This dashboard does not replace the relay or protection system."));
   }else{
    alerts.resolve(t,protectionKey);
    parts.add(part("PROTECTION","Protection and auxiliary systems",null,"0.05","ASSESSED","Latest recorded protection event is marked inactive; this does not prove the full protection system is healthy."));
   }
  }else parts.add(part("PROTECTION","Protection and auxiliary systems",null,"0.05","UNKNOWN","No protection-event data is available; absence of a record does not prove protection health."));
  BigDecimal score=coverage.signum()==0?null:weighted.divide(coverage,2,RoundingMode.HALF_UP);
  String status=score==null||coverage.compareTo(minimumCoverage)<0?"UNAVAILABLE":score.compareTo(new BigDecimal("40"))<0?"CRITICAL":score.compareTo(new BigDecimal("65"))<0?"WARNING":"NORMAL";
  if(criticalDetected)status="CRITICAL";else if(warningDetected&&"NORMAL".equals(status))status="WARNING";
  String explanation=score==null?"Insufficient diagnostic data to calculate a demonstration index.":"Demonstration health index "+score+" / 100 based on "+coverage.multiply(new BigDecimal("100")).setScale(0,RoundingMode.HALF_UP)+"% weighted data coverage. Unknown groups are excluded from the average, not assumed healthy.";
  HealthAssessment a=new HealthAssessment(t,status,score,coverage,explanation,now);parts.forEach(a::addComponent);return assessments.save(a);
 }
 private Optional<SensorReading> latest(Transformer t,MeasurementType m){return readings.findTopByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(t.getId(),m);}
 private BigDecimal average(List<BigDecimal> x){return x.stream().reduce(BigDecimal.ZERO,BigDecimal::add).divide(BigDecimal.valueOf(x.size()),2,RoundingMode.HALF_UP);}
 private HealthComponentScore part(String k,String l,BigDecimal s,String w,String state,String e){return new HealthComponentScore(k,l,s,new BigDecimal(w),state,e);}
}
