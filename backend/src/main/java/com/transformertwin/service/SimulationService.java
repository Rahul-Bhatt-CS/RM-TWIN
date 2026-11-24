package com.transformertwin.service;

import com.transformertwin.domain.*;
import com.transformertwin.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Synthetic operating scenario with a first-order IEC 60076-7-style thermal response.
 * Parameters are demonstration defaults and must be replaced by heat-run/OEM data for an asset.
 */
@Service
public class SimulationService {
 private final TransformerRepository transformers; private final SensorReadingRepository readings;
 private final DiagnosticTestRepository diagnostics;
 private final RealtimeService realtime; private final TransformerService transformerService; private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
 @Value("${app.simulation.enabled:true}") private boolean enabled;
 @Value("${app.simulation.interval-ms:5000}") private long intervalMs;
 @Value("${app.simulation.virtual-minutes-per-tick:1}") private double virtualMinutes;
 @Value("${app.thermal.rated-top-oil-rise-k:55}") private double ratedTopOilRise;
 @Value("${app.thermal.rated-hotspot-gradient-k:25}") private double ratedHotspotGradient;
 @Value("${app.thermal.oil-time-constant-min:180}") private double oilTau;
 @Value("${app.thermal.winding-time-constant-min:7}") private double windingTau;
 @Value("${app.thermal.loss-ratio:5}") private double lossRatio;
 @Value("${app.thermal.oil-exponent:0.8}") private double oilExponent;
 @Value("${app.thermal.winding-exponent:1.6}") private double windingExponent;
 public SimulationService(TransformerRepository t,SensorReadingRepository r,DiagnosticTestRepository d,RealtimeService rt,TransformerService ts,com.fasterxml.jackson.databind.ObjectMapper objectMapper){transformers=t;readings=r;diagnostics=d;realtime=rt;transformerService=ts;this.objectMapper=objectMapper;}
 @EventListener(ApplicationReadyEvent.class) @Transactional public void initialize(){if(!enabled)return;for(Transformer t:transformers.findAll())if(t.isSimulationEnabled())seedDiagnosticHistory(t);}
 @Scheduled(fixedDelayString="${app.simulation.interval-ms:5000}") @Transactional public void tick(){
  if(!enabled)return;
  for(Transformer t:transformers.findAll()){
   if(!t.isSimulationEnabled()||t.getRatedPowerMva()==null||t.getVoltageClassKv()==null||t.getLvVoltageKv()==null||t.getRatedFrequencyHz()==null)continue;
   seedDiagnosticHistory(t); long n=t.getSimulationTick();
   double topOilState=t.getTopOilRiseState().doubleValue(),hotspotState=t.getHotspotGradientState().doubleValue();
   int tapPosition=t.getOltcTapPosition();long operationCount=t.getOltcOperationCount();
   // Smooth, repeatable daily-like loading scenario; no discontinuous temperature jumps.
   double load=72+24*Math.sin((n%288)/288.0*2*Math.PI-0.8)+12*Math.sin((n%96)/96.0*2*Math.PI);
   load=Math.max(35,Math.min(112,load)); double ambient=29+5*Math.sin((n%288)/288.0*2*Math.PI-1.2);
   String cooling=(t.getCoolingType()==null?"ONAN/ONAF":t.getCoolingType()).toUpperCase();
   String configuredStages=(t.getCoolingStages()==null||t.getCoolingStages().isBlank()?cooling:t.getCoolingStages()).toUpperCase();
   boolean fanInstalled=configuredStages.contains("ONAF")||configuredStages.contains("OFAF")||configuredStages.contains("ODAF");
   boolean pumpInstalled=configuredStages.contains("OFAF")||configuredStages.contains("ODAF")||configuredStages.contains("OFWF");
   // Demonstration fan-bank failure is only modelled where a forced-air stage is configured.
   boolean fanFailure=fanInstalled && n%288>=150 && n%288<174;
   boolean fanOn=fanInstalled && load>=65 && !fanFailure;
   boolean pumpOn=pumpInstalled && load>=65;
   boolean coolingActive=(!fanInstalled||fanOn)&&(!pumpInstalled||pumpOn);
   boolean forcedCoolingActive=(fanInstalled||pumpInstalled)&&coolingActive;
   double effectiveCoolingFactor=forcedCoolingActive?0.72:1.0;
   double k=load/100.0;
   double configuredOilRise=t.getRatedTopOilRiseK()==null?ratedTopOilRise:t.getRatedTopOilRiseK().doubleValue();
   double configuredHotspot=t.getRatedHotspotGradientK()==null?ratedHotspotGradient:t.getRatedHotspotGradientK().doubleValue();
   double configuredOilTau=t.getOilTimeConstantMin()==null?oilTau:t.getOilTimeConstantMin().doubleValue();
   double configuredWindingTau=t.getWindingTimeConstantMin()==null?windingTau:t.getWindingTimeConstantMin().doubleValue();
   double configuredLoss=t.getLossRatio()==null?lossRatio:t.getLossRatio().doubleValue();
   double steadyOilRise=ThermalModel.steadyTopOilRise(k,configuredOilRise,configuredLoss,oilExponent,effectiveCoolingFactor);
   double steadyHotspotGradient=ThermalModel.steadyHotspotGradient(k,configuredHotspot,windingExponent);
   double dt=Math.max(0.1,virtualMinutes);
   topOilState=ThermalModel.next(topOilState,steadyOilRise,dt,Math.max(1,configuredOilTau));
   hotspotState=ThermalModel.next(hotspotState,steadyHotspotGradient,dt,Math.max(1,configuredWindingTau));
   double oil=ambient+topOilState; double hot=oil+hotspotState;
   double mva=t.getRatedPowerMva().doubleValue()*load/100.0;
   double hv=ElectricalCalculations.balancedLineCurrentA(mva,t.getVoltageClassKv().doubleValue());
   double lv=ElectricalCalculations.balancedLineCurrentA(mva,t.getLvVoltageKv().doubleValue());
   // Tap operation is event-driven: position/count/current change together once per operation window.
   boolean tapOperation=n>0 && n%72==0;
   if(tapOperation){tapPosition=tapPosition>=2?-2:tapPosition+1;operationCount++;}
   double motorCurrent=tapOperation?4.2:0.0;
   // Conservator level drifts only in the explicit leak demonstration window.
   double oilLevel=(n%288>=220&&n%288<250)?82.5:96.0;
   double radiatorOutlet=Math.max(ambient+4,oil-(forcedCoolingActive?13:8));
   Instant now=Instant.now();
   save(t,MeasurementType.TOP_OIL_TEMPERATURE,oil,"°C",now);save(t,MeasurementType.WINDING_HOTSPOT_TEMPERATURE,hot,"°C",now);
   save(t,MeasurementType.LOAD_PERCENT,load,"%",now);save(t,MeasurementType.AMBIENT_TEMPERATURE,ambient,"°C",now);
   save(t,MeasurementType.APPARENT_POWER_MVA,mva,"MVA",now);save(t,MeasurementType.HV_LINE_CURRENT_A,hv,"A",now);save(t,MeasurementType.LV_LINE_CURRENT_A,lv,"A",now);
   if(fanInstalled)save(t,MeasurementType.COOLING_FAN_STATUS,fanOn?1:0,"ON=1 / OFF=0",now);
   if(pumpInstalled)save(t,MeasurementType.COOLING_PUMP_STATUS,pumpOn?1:0,"ON=1 / OFF=0",now);
   save(t,MeasurementType.OIL_LEVEL_PERCENT,oilLevel,"%",now);save(t,MeasurementType.RADIATOR_OUTLET_TEMPERATURE,radiatorOutlet,"°C",now);
   save(t,MeasurementType.OLTC_TAP_POSITION,tapPosition,"position",now);save(t,MeasurementType.OLTC_OPERATION_COUNT,operationCount,"operations",now);save(t,MeasurementType.OLTC_MOTOR_CURRENT_A,motorCurrent,"A",now);
   t.updateSimulationState(n+1,BigDecimal.valueOf(topOilState),BigDecimal.valueOf(hotspotState),tapPosition,operationCount);
   realtime.publish(t.getId(),transformerService.dashboard(t.getId()));
  }
 }
 private void seedDiagnosticHistory(Transformer t){
  if(!diagnostics.existsByTransformerIdAndSourceAndTestType(t.getId(),DataSourceType.SIMULATED,"DGA")){
   Instant base=Instant.now().minus(2,ChronoUnit.DAYS);String sampleId="SIM-DGA-"+t.getAssetCode()+"-INITIAL";
   add(t,"DGA","HYDROGEN",42,"ppm",base.minus(20,ChronoUnit.DAYS),"MAIN_TANK",sampleId);add(t,"DGA","METHANE",11,"ppm",base.minus(20,ChronoUnit.DAYS),"MAIN_TANK",sampleId);
   add(t,"DGA","ETHANE",4,"ppm",base.minus(20,ChronoUnit.DAYS),"MAIN_TANK",sampleId);add(t,"DGA","ETHYLENE",8,"ppm",base.minus(20,ChronoUnit.DAYS),"MAIN_TANK",sampleId);
   add(t,"DGA","ACETYLENE",0,"ppm",base.minus(20,ChronoUnit.DAYS),"MAIN_TANK",sampleId);add(t,"DGA","CARBON_MONOXIDE",180,"ppm",base.minus(20,ChronoUnit.DAYS),"MAIN_TANK",sampleId);add(t,"DGA","CARBON_DIOXIDE",1450,"ppm",base.minus(20,ChronoUnit.DAYS),"MAIN_TANK",sampleId);
   add(t,"OIL_TEST","MOISTURE",8,"ppm",base,"MAIN_TANK");add(t,"OIL_TEST","BREAKDOWN_VOLTAGE",68,"kV",base,"MAIN_TANK");add(t,"OIL_TEST","ACIDITY",0.08,"mgKOH/g",base,"MAIN_TANK");
   add(t,"INSULATION_TEST","INSULATION_RESISTANCE",5200,"MOhm",base,"MAIN_TANK");add(t,"INSULATION_TEST","POLARIZATION_INDEX",2.1,"ratio",base,"MAIN_TANK");add(t,"DIELECTRIC_TEST","WINDING_TAN_DELTA",0.0034,"ratio",base,"MAIN_TANK");add(t,"DIELECTRIC_TEST","FURAN_2_FAL",0.12,"ppm",base,"MAIN_TANK");
   for(String ph:new String[]{"A","B","C"}){add(t,"BUSHING_TEST","BUSHING_TAN_DELTA",0.0038,"ratio",base,"HV_BUSHING");add(t,"BUSHING_TEST","BUSHING_CAPACITANCE",420,"pF",base,"HV_BUSHING");}
   add(t,"FRA_TEST","FRA_DEVIATION",1.8,"%",base,"MAIN_TANK");add(t,"ELECTRICAL_TEST","WINDING_RESISTANCE",0.15,"ohm",base,"MAIN_TANK");
   add(t,"PARTIAL_DISCHARGE_TEST","APPARENT_CHARGE",85,"pC",base,"MAIN_TANK");add(t,"OLTC_TEST","CONTACT_RESISTANCE",82,"microohm",base,"OLTC");add(t,"OLTC_TEST","OPERATING_TIME",65,"ms",base,"OLTC");
   add(t,"COOLING_TEST","RADIATOR_TEMPERATURE_DIFFERENCE",8,"°C",base,"RADIATOR");add(t,"COOLING_TEST","FAN_VIBRATION",2.1,"mm/s",base,"COOLING_FAN");
  }
  // No synthetic normal relay records: their existence cannot establish protection-system health.
 }
 private void add(Transformer t,String type,String parameter,double value,String unit,Instant date,String component){add(t,type,parameter,value,unit,date,component,null);}
 private void add(Transformer t,String type,String parameter,double value,String unit,Instant date,String component,String sampleId){DiagnosticTest d=new DiagnosticTest(t,type,parameter,BigDecimal.valueOf(value),unit,date,DataSourceType.SIMULATED,"Synthetic demonstration dataset","Project illustrative value; not a laboratory result or standards pass/fail conclusion.","Simulated diagnostic example",component,null);d.setSampleId(sampleId);diagnostics.save(d);}
 private void save(Transformer t,MeasurementType m,double v,String u,Instant at){
  String sensorKey=null;try{if(t.getSensorMapping()!=null&&!t.getSensorMapping().isBlank()){var node=objectMapper.readTree(t.getSensorMapping()).get(m.name());if(node!=null&&!node.isNull())sensorKey=node.asText();}}catch(Exception ignored){/* Registration validates mapping JSON; a malformed legacy value remains unmapped. */}
  readings.save(new SensorReading(t,m,BigDecimal.valueOf(v).setScale(4,RoundingMode.HALF_UP),u,DataSourceType.SIMULATED,at,sensorKey));
 }
}
