package com.transformertwin.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "health_assessment", indexes = {
        @Index(name = "idx_health_transformer_time", columnList = "transformer_id, assessed_at")
})
public class HealthAssessment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transformer_id", nullable = false)
    private Transformer transformer;
    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false, length = 30)
    private HealthStatus healthStatus;
    @Column(columnDefinition = "TEXT")
    private String explanation;
    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt;

    protected HealthAssessment() {}
    public HealthAssessment(Transformer transformer, HealthStatus status, String explanation, Instant assessedAt) {
        this.transformer = transformer; this.healthStatus = status; this.explanation = explanation; this.assessedAt = assessedAt;
    }
    public Long getId() { return id; }
    public Transformer getTransformer() { return transformer; }
    public HealthStatus getHealthStatus() { return healthStatus; }
    public String getExplanation() { return explanation; }
    public Instant getAssessedAt() { return assessedAt; }
}
