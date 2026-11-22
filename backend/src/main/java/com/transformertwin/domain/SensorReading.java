package com.transformertwin.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "sensor_reading", indexes = {
        @Index(name = "idx_reading_transformer_time", columnList = "transformer_id, recorded_at")
})
public class SensorReading {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transformer_id", nullable = false)
    private Transformer transformer;
    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_type", nullable = false, length = 60)
    private MeasurementType measurementType;
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal value;
    @Column(nullable = false, length = 20)
    private String unit;
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
    @Column(nullable = false, length = 20)
    private String source;

    protected SensorReading() {}
    public SensorReading(Transformer transformer, MeasurementType type, BigDecimal value, String unit, Instant recordedAt) {
        this.transformer = transformer; this.measurementType = type; this.value = value;
        this.unit = unit; this.recordedAt = recordedAt; this.source = "SIMULATED";
    }
    public Long getId() { return id; }
    public Transformer getTransformer() { return transformer; }
    public MeasurementType getMeasurementType() { return measurementType; }
    public BigDecimal getValue() { return value; }
    public String getUnit() { return unit; }
    public Instant getRecordedAt() { return recordedAt; }
    public String getSource() { return source; }
}
