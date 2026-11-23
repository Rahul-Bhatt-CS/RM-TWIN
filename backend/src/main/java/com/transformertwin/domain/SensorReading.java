package com.transformertwin.domain;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="sensor_reading",indexes=@Index(name="idx_reading_asset_metric_time",columnList="transformer_id,measurement_type,recorded_at"))
public class SensorReading {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="transformer_id") private Transformer transformer;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=60) private MeasurementType measurementType;
 @Column(nullable=false,precision=16,scale=5) private BigDecimal value;
 @Column(nullable=false,length=40) private String unit;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private DataSourceType source;
 @Column(nullable=false) private Instant recordedAt;
 protected SensorReading(){}
 public SensorReading(Transformer t,MeasurementType m,BigDecimal v,String u,DataSourceType s,Instant at){transformer=t;measurementType=m;value=v;unit=u;source=s;recordedAt=at;}
 public Long getId(){return id;} public Transformer getTransformer(){return transformer;} public MeasurementType getMeasurementType(){return measurementType;}
 public BigDecimal getValue(){return value;} public String getUnit(){return unit;} public DataSourceType getSource(){return source;} public Instant getRecordedAt(){return recordedAt;}
}
