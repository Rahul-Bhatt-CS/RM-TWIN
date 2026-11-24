package com.transformertwin.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="transformer")
public class Transformer {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=120) private String name;
 @Column(name="asset_code",nullable=false,unique=true,length=60) private String assetCode;
 @Column(length=120) private String manufacturer; @Column(length=120) private String model; @Column(length=100) private String serialNumber;
 @Column(precision=10,scale=2) private BigDecimal voltageClassKv; @Column(name="lv_voltage_kv",precision=10,scale=2) private BigDecimal lvVoltageKv; @Column(precision=10,scale=2) private BigDecimal ratedPowerMva; @Column(name="rated_frequency_hz",precision=6,scale=2) private BigDecimal ratedFrequencyHz; @Column(name="reference_profile",length=100) private String referenceProfile;
 @Column(length=40) private String coolingType; @Column(length=60) private String insulationType; private LocalDate commissioningDate;
 @Column(length=180) private String location; @Column(length=1000) private String description;
 private boolean simulationEnabled=true;
 @Column(name="rated_top_oil_rise_k",precision=8,scale=3) private BigDecimal ratedTopOilRiseK;
 @Column(name="rated_hotspot_gradient_k",precision=8,scale=3) private BigDecimal ratedHotspotGradientK;
 @Column(name="oil_time_constant_min",precision=8,scale=3) private BigDecimal oilTimeConstantMin;
 @Column(name="winding_time_constant_min",precision=8,scale=3) private BigDecimal windingTimeConstantMin;
 @Column(name="loss_ratio",precision=8,scale=3) private BigDecimal lossRatio;
 @Column(name="cooling_stages",length=300) private String coolingStages;
 @Column(name="sensor_mapping",columnDefinition="TEXT") private String sensorMapping;
 @Column(name="simulation_tick") private Long simulationTick=0L;
 @Column(name="top_oil_rise_k_state",precision=10,scale=4) private BigDecimal topOilRiseState=BigDecimal.valueOf(35);
 @Column(name="hotspot_gradient_k_state",precision=10,scale=4) private BigDecimal hotspotGradientState=BigDecimal.valueOf(15);
 @Column(name="oltc_tap_position") private Integer oltcTapPosition=0;
 @Column(name="oltc_operation_count") private Long oltcOperationCount=0L;
 @Column(nullable=false) private Instant createdAt; @Column(nullable=false) private Instant updatedAt;
 @PrePersist void create(){createdAt=Instant.now();updatedAt=createdAt;}
 @PreUpdate void update(){updatedAt=Instant.now();}
 protected Transformer(){}
 public Transformer(String name,String assetCode,String manufacturer,String model,String serialNumber,BigDecimal voltageClassKv,BigDecimal lvVoltageKv,BigDecimal ratedPowerMva,BigDecimal ratedFrequencyHz,String referenceProfile,String coolingType,String insulationType,LocalDate commissioningDate,String location,String description,boolean simulationEnabled){
 this.name=name;this.assetCode=assetCode;this.manufacturer=manufacturer;this.model=model;this.serialNumber=serialNumber;this.voltageClassKv=voltageClassKv;this.lvVoltageKv=lvVoltageKv;this.ratedPowerMva=ratedPowerMva;this.ratedFrequencyHz=ratedFrequencyHz;this.referenceProfile=referenceProfile;this.coolingType=coolingType;this.insulationType=insulationType;this.commissioningDate=commissioningDate;this.location=location;this.description=description;this.simulationEnabled=simulationEnabled;}
 public Long getId(){return id;} public String getName(){return name;} public String getAssetCode(){return assetCode;}
 public String getManufacturer(){return manufacturer;} public String getModel(){return model;} public String getSerialNumber(){return serialNumber;}
 public BigDecimal getVoltageClassKv(){return voltageClassKv;} public BigDecimal getLvVoltageKv(){return lvVoltageKv;} public BigDecimal getRatedPowerMva(){return ratedPowerMva;} public BigDecimal getRatedFrequencyHz(){return ratedFrequencyHz;} public String getReferenceProfile(){return referenceProfile;}
 public String getCoolingType(){return coolingType;} public String getInsulationType(){return insulationType;}
 public LocalDate getCommissioningDate(){return commissioningDate;} public String getLocation(){return location;}
 public String getDescription(){return description;} public boolean isSimulationEnabled(){return simulationEnabled;}
 public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
 public BigDecimal getRatedTopOilRiseK(){return ratedTopOilRiseK;} public BigDecimal getRatedHotspotGradientK(){return ratedHotspotGradientK;} public BigDecimal getOilTimeConstantMin(){return oilTimeConstantMin;} public BigDecimal getWindingTimeConstantMin(){return windingTimeConstantMin;} public BigDecimal getLossRatio(){return lossRatio;} public String getCoolingStages(){return coolingStages;} public String getSensorMapping(){return sensorMapping;}
 public Long getSimulationTick(){return simulationTick==null?0L:simulationTick;} public BigDecimal getTopOilRiseState(){return topOilRiseState==null?BigDecimal.valueOf(35):topOilRiseState;} public BigDecimal getHotspotGradientState(){return hotspotGradientState==null?BigDecimal.valueOf(15):hotspotGradientState;} public Integer getOltcTapPosition(){return oltcTapPosition==null?0:oltcTapPosition;} public Long getOltcOperationCount(){return oltcOperationCount==null?0L:oltcOperationCount;}
 public void updateEngineeringConfig(BigDecimal topOil,BigDecimal hotspot,BigDecimal oilTau,BigDecimal windingTau,BigDecimal losses,String stages,String mapping){ratedTopOilRiseK=topOil;ratedHotspotGradientK=hotspot;oilTimeConstantMin=oilTau;windingTimeConstantMin=windingTau;lossRatio=losses;coolingStages=stages;sensorMapping=mapping;}
 public void updateSimulationState(long tick,BigDecimal oilRise,BigDecimal hotspotGradient,int tap,long operations){simulationTick=tick;topOilRiseState=oilRise;hotspotGradientState=hotspotGradient;oltcTapPosition=tap;oltcOperationCount=operations;}
}
