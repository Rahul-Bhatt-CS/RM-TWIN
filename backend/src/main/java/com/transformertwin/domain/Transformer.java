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
 @Column(precision=10,scale=2) private BigDecimal voltageClassKv; @Column(precision=10,scale=2) private BigDecimal ratedPowerMva;
 @Column(length=40) private String coolingType; @Column(length=60) private String insulationType; private LocalDate commissioningDate;
 @Column(length=180) private String location; @Column(length=1000) private String description;
 private boolean simulationEnabled=true;
 @Column(nullable=false) private Instant createdAt; @Column(nullable=false) private Instant updatedAt;
 @PrePersist void create(){createdAt=Instant.now();updatedAt=createdAt;}
 @PreUpdate void update(){updatedAt=Instant.now();}
 protected Transformer(){}
 public Transformer(String name,String assetCode,String manufacturer,String model,String serialNumber,BigDecimal voltageClassKv,BigDecimal ratedPowerMva,String coolingType,String insulationType,LocalDate commissioningDate,String location,String description,boolean simulationEnabled){
 this.name=name;this.assetCode=assetCode;this.manufacturer=manufacturer;this.model=model;this.serialNumber=serialNumber;this.voltageClassKv=voltageClassKv;this.ratedPowerMva=ratedPowerMva;this.coolingType=coolingType;this.insulationType=insulationType;this.commissioningDate=commissioningDate;this.location=location;this.description=description;this.simulationEnabled=simulationEnabled;}
 public Long getId(){return id;} public String getName(){return name;} public String getAssetCode(){return assetCode;}
 public String getManufacturer(){return manufacturer;} public String getModel(){return model;} public String getSerialNumber(){return serialNumber;}
 public BigDecimal getVoltageClassKv(){return voltageClassKv;} public BigDecimal getRatedPowerMva(){return ratedPowerMva;}
 public String getCoolingType(){return coolingType;} public String getInsulationType(){return insulationType;}
 public LocalDate getCommissioningDate(){return commissioningDate;} public String getLocation(){return location;}
 public String getDescription(){return description;} public boolean isSimulationEnabled(){return simulationEnabled;}
 public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
