package com.transformertwin.domain;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="alert",indexes={@Index(name="idx_alert_asset_time",columnList="transformer_id,created_at"),@Index(name="idx_alert_asset_status",columnList="transformer_id,status")})
public class Alert {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="transformer_id") private Transformer transformer;
 @Column(nullable=false,length=100) private String conditionKey; @Column(nullable=false,length=180) private String title;
 @Column(nullable=false,length=1000) private String message; @Column(nullable=false,length=20) private String severity;
 @Column(nullable=false,length=20) private String status; @Column(precision=16,scale=5) private BigDecimal observedValue; @Column(length=20) private String unit;
 @Column(nullable=false) private Instant createdAt; private Instant resolvedAt; private Instant acknowledgedAt; @Column(length=100) private String acknowledgedBy;
 protected Alert(){}
 public Alert(Transformer t,String key,String title,String message,String severity,BigDecimal value,String unit,Instant at){transformer=t;conditionKey=key;this.title=title;this.message=message;this.severity=severity;observedValue=value;this.unit=unit;status="ACTIVE";createdAt=at;}
 public void resolve(Instant at){status="RESOLVED";resolvedAt=at;}
 public void acknowledge(String by,Instant at){if("ACTIVE".equals(status)&&acknowledgedAt==null){acknowledgedBy=by;acknowledgedAt=at;}}
 public Long getId(){return id;} public Transformer getTransformer(){return transformer;} public String getConditionKey(){return conditionKey;} public String getTitle(){return title;} public String getMessage(){return message;} public String getSeverity(){return severity;} public String getStatus(){return status;} public BigDecimal getObservedValue(){return observedValue;} public String getUnit(){return unit;} public Instant getCreatedAt(){return createdAt;} public Instant getResolvedAt(){return resolvedAt;} public Instant getAcknowledgedAt(){return acknowledgedAt;} public String getAcknowledgedBy(){return acknowledgedBy;}
}
