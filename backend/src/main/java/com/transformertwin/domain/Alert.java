package com.transformertwin.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "alert", indexes = {
        @Index(name = "idx_alert_transformer_time", columnList = "transformer_id, created_at")
})
public class Alert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transformer_id", nullable = false)
    private Transformer transformer;
    @Column(name = "alert_condition", nullable = false, length = 120)
    private String condition;
    @Column(length = 20)
    private String severity;
    @Column(name = "observed_value", precision = 12, scale = 4)
    private BigDecimal observedValue;
    @Column(name = "threshold_value", precision = 12, scale = 4)
    private BigDecimal thresholdValue;
    @Column(length = 20)
    private String status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Alert() {}
    public Alert(Transformer transformer, String condition, String severity, BigDecimal observedValue,
                 BigDecimal thresholdValue, String status, Instant createdAt) {
        this.transformer = transformer; this.condition = condition; this.severity = severity;
        this.observedValue = observedValue; this.thresholdValue = thresholdValue; this.status = status; this.createdAt = createdAt;
    }
    public void resolve() { this.status = "RESOLVED"; }
    public Long getId() { return id; }
    public Transformer getTransformer() { return transformer; }
    public String getCondition() { return condition; }
    public String getSeverity() { return severity; }
    public BigDecimal getObservedValue() { return observedValue; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
