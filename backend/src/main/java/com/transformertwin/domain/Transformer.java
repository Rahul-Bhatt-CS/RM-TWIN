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
}
