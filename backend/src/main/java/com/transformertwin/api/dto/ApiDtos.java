package com.transformertwin.api.dto;

import com.transformertwin.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}
    public record TransformerDto(Long id, String name, String assetCode, String rating) {
        public static TransformerDto from(Transformer t) { return new TransformerDto(t.getId(), t.getName(), t.getAssetCode(), t.getRating()); }
    }
    public record ReadingDto(Long id, Long transformerId, String measurementType, BigDecimal value,
                             String unit, Instant recordedAt, String source) {
        public static ReadingDto from(SensorReading r) {
            return new ReadingDto(r.getId(), r.getTransformer().getId(), r.getMeasurementType().name(),
                    r.getValue(), r.getUnit(), r.getRecordedAt(), r.getSource());
        }
    }
    public record HealthDto(Long id, Long transformerId, String healthStatus, BigDecimal score,
                            String explanation, Instant assessedAt) {
        public static HealthDto from(HealthAssessment h) {
            return new HealthDto(h.getId(), h.getTransformer().getId(), h.getHealthStatus().name(), null,
                    h.getExplanation(), h.getAssessedAt());
        }
    }
    public record AlertDto(Long id, Long transformerId, String condition, String severity,
                           BigDecimal observedValue, BigDecimal thresholdValue, String unit,
                           String status, Instant createdAt) {
        public static AlertDto from(Alert a, String unit) {
            return new AlertDto(a.getId(), a.getTransformer().getId(), a.getCondition(), a.getSeverity(),
                    a.getObservedValue(), a.getThresholdValue(), unit, a.getStatus(), a.getCreatedAt());
        }
    }
    public record DashboardDto(Long transformerId, String assetName, String source, HealthDto health,
                               Instant lastUpdated, List<ReadingDto> readings, Long activeAlertCount) {}
    public record HistoryDto(Long transformerId, Instant from, Instant to, List<ReadingDto> readings) {}
    public record HealthHistoryDto(Long transformerId, List<HealthDto> assessments) {}
    public record AlertsDto(Long transformerId, List<AlertDto> alerts) {}
    public record LatestReadingsDto(Long transformerId, String source, List<ReadingDto> readings) {}
    public record LiveEvent(String eventType, DashboardDto dashboard, AlertDto alert) {}
}
