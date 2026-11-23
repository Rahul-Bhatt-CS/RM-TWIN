package com.transformertwin.domain;
import jakarta.persistence.*; import java.math.BigDecimal;
@Entity @Table(name="health_component_score")
public class HealthComponentScore {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="assessment_id") private HealthAssessment assessment;
 @Column(nullable=false,length=60) private String componentKey; @Column(nullable=false,length=100) private String componentLabel;
 @Column(precision=6,scale=2) private BigDecimal score; @Column(nullable=false,precision=5,scale=4) private BigDecimal weight;
 @Column(nullable=false,length=20) private String state; @Column(nullable=false,length=1000) private String explanation;
 protected HealthComponentScore(){}
 public HealthComponentScore(String key,String label,BigDecimal score,BigDecimal weight,String state,String explanation){componentKey=key;componentLabel=label;this.score=score;this.weight=weight;this.state=state;this.explanation=explanation;}
 void setAssessment(HealthAssessment a){assessment=a;}
 public Long getId(){return id;} public String getComponentKey(){return componentKey;} public String getComponentLabel(){return componentLabel;} public BigDecimal getScore(){return score;} public BigDecimal getWeight(){return weight;} public String getState(){return state;} public String getExplanation(){return explanation;}
}
