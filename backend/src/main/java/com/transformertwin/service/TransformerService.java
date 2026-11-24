package com.transformertwin.service;
import com.transformertwin.api.dto.ApiDtos;
import com.transformertwin.api.dto.ApiDtos.*; import com.transformertwin.domain.*; import com.transformertwin.repository.*;
import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.Instant; import java.util.*; import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class TransformerService {
 private final TransformerRepository transformers; private final SensorReadingRepository readings; private final HealthAssessmentRepository health; private final AlertRepository alerts; private final ObjectMapper objectMapper;
 public TransformerService(TransformerRepository t,SensorReadingRepository r,HealthAssessmentRepository h,AlertRepository a,ObjectMapper objectMapper){transformers=t;readings=r;health=h;alerts=a;this.objectMapper=objectMapper;}
 public PageResponse<TransformerView> list(int page,int size){return ApiDtos.page(transformers.findAll(PageRequest.of(page,size,Sort.by("name").ascending())),TransformerView::from);}
 public Transformer get(Long id){return transformers.findById(id).orElseThrow(()->new NoSuchElementException("Transformer not found"));}
 @Transactional public TransformerView create(TransformerCreate x){
  if(transformers.findByAssetCode(x.assetCode()).isPresent())throw new IllegalArgumentException("Asset code already exists.");
  boolean simulationEnabled=x.simulationEnabled()==null||x.simulationEnabled();
  if(simulationEnabled&&(x.voltageClassKv()==null||x.lvVoltageKv()==null||x.ratedPowerMva()==null||x.ratedFrequencyHz()==null)){
   throw new IllegalArgumentException("HV voltage, LV voltage, rated MVA and frequency are required when the simulator is enabled.");
  }
  if("CEA-100MVA-220-33KV-3PH".equals(x.referenceProfile())){
   if(x.voltageClassKv()==null||x.lvVoltageKv()==null||x.ratedPowerMva()==null||x.ratedFrequencyHz()==null
      ||x.voltageClassKv().compareTo(new java.math.BigDecimal("220"))!=0
      ||x.lvVoltageKv().compareTo(new java.math.BigDecimal("33"))!=0
      ||x.ratedPowerMva().compareTo(new java.math.BigDecimal("100"))!=0
      ||x.ratedFrequencyHz().compareTo(new java.math.BigDecimal("50"))!=0){
    throw new IllegalArgumentException("The CEA reference profile requires 100 MVA, 220/33 kV, three-phase, 50 Hz nameplate values.");
   }
  }
  if(x.sensorMapping()!=null&&!x.sensorMapping().isBlank())try{if(!objectMapper.readTree(x.sensorMapping()).isObject())throw new IllegalArgumentException("Sensor mapping must be a JSON object.");}catch(com.fasterxml.jackson.core.JsonProcessingException ex){throw new IllegalArgumentException("Sensor mapping must contain valid JSON.");}
  validatePositive(x.ratedTopOilRiseK(),"Rated top-oil rise");validatePositive(x.ratedHotspotGradientK(),"Rated hot-spot gradient");validatePositive(x.oilTimeConstantMin(),"Oil time constant");validatePositive(x.windingTimeConstantMin(),"Winding time constant");validatePositive(x.lossRatio(),"Loss ratio");
  Transformer t=new Transformer(x.name().trim(),x.assetCode().trim().toUpperCase(Locale.ROOT),x.manufacturer(),x.model(),x.serialNumber(),x.voltageClassKv(),x.lvVoltageKv(),x.ratedPowerMva(),x.ratedFrequencyHz(),x.referenceProfile(),x.coolingType(),x.insulationType(),x.commissioningDate(),x.location(),x.description(),x.simulationEnabled()==null?true:x.simulationEnabled());
  t.updateEngineeringConfig(x.ratedTopOilRiseK(),x.ratedHotspotGradientK(),x.oilTimeConstantMin(),x.windingTimeConstantMin(),x.lossRatio(),x.coolingStages(),x.sensorMapping());
  return TransformerView.from(transformers.save(t));
 }
 private void validatePositive(java.math.BigDecimal value,String field){if(value!=null&&value.signum()<=0)throw new IllegalArgumentException(field+" must be greater than zero.");}
 public Dashboard dashboard(Long id){
  Transformer t=get(id);var latest=java.util.Arrays.stream(MeasurementType.values()).map(m->readings.findTopByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(id,m)).flatMap(java.util.Optional::stream).map(ReadingView::from).toList();
  var h=health.findTopByTransformerIdOrderByAssessedAtDesc(id).map(HealthView::from).orElse(null);
  Instant updated=latest.stream().map(ReadingView::recordedAt).max(Instant::compareTo).orElse(null);
  return new Dashboard(TransformerView.from(t),h,latest,alerts.countByTransformerIdAndStatus(id,"ACTIVE"),updated);
 }
}
