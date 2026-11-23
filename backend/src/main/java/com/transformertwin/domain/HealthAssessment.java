package com.transformertwin.domain;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant; import java.util.*;
@Entity @Table(name="health_assessment",indexes=@Index(name="idx_health_asset_time",columnList="transformer_id,assessed_at"))
public class HealthAssessment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="transformer_id") private Transformer transformer;
 @Column(nullable=false,length=20) private String healthStatus; @Column(precision=6,scale=2) private BigDecimal healthIndex;
 @Column(nullable=false,precision=5,scale=4) private BigDecimal coverage;
 @Column(nullable=false,length=1500) private String explanation; @Column(nullable=false) private Instant assessedAt;
 @OneToMany(mappedBy="assessment",cascade=CascadeType.ALL,orphanRemoval=true) private List<HealthComponentScore> components=new ArrayList<>();
 protected HealthAssessment(){}
 public HealthAssessment(Transformer t,String status,BigDecimal index,BigDecimal coverage,String explanation,Instant at){this.transformer=t;this.healthStatus=status;this.healthIndex=index;this.coverage=coverage;this.explanation=explanation;this.assessedAt=at;}
 public void addComponent(HealthComponentScore c){components.add(c);c.setAssessment(this);}
 public Long getId(){return id;} public Transformer getTransformer(){return transformer;} public String getHealthStatus(){return healthStatus;} public BigDecimal getHealthIndex(){return healthIndex;} public BigDecimal getCoverage(){return coverage;} public String getExplanation(){return explanation;} public Instant getAssessedAt(){return assessedAt;} public List<HealthComponentScore> getComponents(){return components;}
}
