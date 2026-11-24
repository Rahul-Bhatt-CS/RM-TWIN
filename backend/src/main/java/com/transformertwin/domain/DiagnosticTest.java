package com.transformertwin.domain;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="diagnostic_test",indexes={@Index(name="idx_diagnostic_asset_date",columnList="transformer_id,test_date"),@Index(name="idx_diagnostic_component_phase",columnList="transformer_id,component_key,phase")})
public class DiagnosticTest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="transformer_id") private Transformer transformer;
 @Column(nullable=false,length=60) private String testType; @Column(nullable=false,length=80) private String parameterName;
 @Column(nullable=false,precision=16,scale=5) private BigDecimal value; @Column(nullable=false,length=40) private String unit;
 @Column(nullable=false) private Instant testDate; @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private DataSourceType source;
 @Column(length=160) private String laboratory; @Column(length=500) private String referenceNote; @Column(length=1000) private String notes;
 @Column(name="component_key",length=60) private String componentKey; @Column(length=8) private String phase;
 @Column(name="test_method",length=120) private String testMethod; @Column(name="sample_point",length=80) private String samplePoint;
 @Column(name="test_temperature_c",precision=8,scale=3) private BigDecimal testTemperatureC; @Column(name="test_voltage",length=40) private String testVoltage;
 @Column(name="test_duration_seconds") private Integer testDurationSeconds; @Column(name="baseline_reference",length=180) private String baselineReference; @Column(name="sample_id",length=100) private String sampleId;
 protected DiagnosticTest(){}
 public DiagnosticTest(Transformer t,String type,String param,BigDecimal value,String unit,Instant date,DataSourceType source,String lab,String ref,String notes,String componentKey,String phase){this.transformer=t;this.testType=type;this.parameterName=param;this.value=value;this.unit=unit;this.testDate=date;this.source=source;this.laboratory=lab;this.referenceNote=ref;this.notes=notes;this.componentKey=componentKey;this.phase=phase;}
 public Long getId(){return id;} public Transformer getTransformer(){return transformer;} public String getTestType(){return testType;} public String getParameterName(){return parameterName;} public BigDecimal getValue(){return value;} public String getUnit(){return unit;} public Instant getTestDate(){return testDate;} public DataSourceType getSource(){return source;} public String getLaboratory(){return laboratory;} public String getReferenceNote(){return referenceNote;} public String getNotes(){return notes;} public String getComponentKey(){return componentKey;} public String getPhase(){return phase;} public String getTestMethod(){return testMethod;} public String getSamplePoint(){return samplePoint;} public BigDecimal getTestTemperatureC(){return testTemperatureC;} public String getTestVoltage(){return testVoltage;} public Integer getTestDurationSeconds(){return testDurationSeconds;} public String getBaselineReference(){return baselineReference;} public String getSampleId(){return sampleId;} public void setSampleId(String sampleId){this.sampleId=sampleId;} public void setTestMetadata(String method,String point,BigDecimal temp,String voltage,Integer duration,String baseline){this.testMethod=method;this.samplePoint=point;this.testTemperatureC=temp;this.testVoltage=voltage;this.testDurationSeconds=duration;this.baselineReference=baseline;}
}
