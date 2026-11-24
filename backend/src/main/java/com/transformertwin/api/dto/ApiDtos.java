package com.transformertwin.api.dto;
import com.transformertwin.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
public final class ApiDtos {
 private ApiDtos(){}
 public record TransformerCreate(
  @NotBlank @Size(max=120) String name,
  @NotBlank @Pattern(regexp="[A-Za-z0-9._-]{2,60}") String assetCode,
  @Size(max=120) String manufacturer,@Size(max=120) String model,@Size(max=100) String serialNumber,
  @DecimalMin("0.01") @DecimalMax("2000") BigDecimal voltageClassKv,
  @DecimalMin("0.01") @DecimalMax("2000") BigDecimal lvVoltageKv,
  @DecimalMin("0.01") @DecimalMax("10000") BigDecimal ratedPowerMva,
  @DecimalMin("1") @DecimalMax("400") BigDecimal ratedFrequencyHz, @Size(max=100) String referenceProfile,
  @Size(max=40) String coolingType,@Size(max=60) String insulationType,
  LocalDate commissioningDate,@Size(max=180) String location,@Size(max=1000) String description,
  Boolean simulationEnabled,
  @DecimalMin("0.01") BigDecimal ratedTopOilRiseK,@DecimalMin("0.01") BigDecimal ratedHotspotGradientK,@DecimalMin("0.01") BigDecimal oilTimeConstantMin,@DecimalMin("0.01") BigDecimal windingTimeConstantMin,@DecimalMin("0.01") BigDecimal lossRatio,@Size(max=300) String coolingStages,@Size(max=5000) String sensorMapping) {}
 public record TransformerView(Long id,String name,String assetCode,String manufacturer,String model,String serialNumber,
  BigDecimal voltageClassKv,BigDecimal lvVoltageKv,BigDecimal ratedPowerMva,BigDecimal ratedFrequencyHz,String referenceProfile,String coolingType,String insulationType,LocalDate commissioningDate,
  String location,String description,boolean simulationEnabled,BigDecimal ratedTopOilRiseK,BigDecimal ratedHotspotGradientK,BigDecimal oilTimeConstantMin,BigDecimal windingTimeConstantMin,BigDecimal lossRatio,String coolingStages,String sensorMapping,Instant createdAt,Instant updatedAt) {
  public static TransformerView from(Transformer t){return new TransformerView(t.getId(),t.getName(),t.getAssetCode(),t.getManufacturer(),t.getModel(),t.getSerialNumber(),t.getVoltageClassKv(),t.getLvVoltageKv(),t.getRatedPowerMva(),t.getRatedFrequencyHz(),t.getReferenceProfile(),t.getCoolingType(),t.getInsulationType(),t.getCommissioningDate(),t.getLocation(),t.getDescription(),t.isSimulationEnabled(),t.getRatedTopOilRiseK(),t.getRatedHotspotGradientK(),t.getOilTimeConstantMin(),t.getWindingTimeConstantMin(),t.getLossRatio(),t.getCoolingStages(),t.getSensorMapping(),t.getCreatedAt(),t.getUpdatedAt());}
 }
 public record ReadingView(Long id,Long transformerId,String measurementType,BigDecimal value,String unit,String source,Instant recordedAt,String sensorKey) {
  public static ReadingView from(SensorReading r){return new ReadingView(r.getId(),r.getTransformer().getId(),r.getMeasurementType().name(),r.getValue(),r.getUnit(),r.getSource().name(),r.getRecordedAt(),r.getSensorKey());}
 }
 public record ComponentView(String key,String label,BigDecimal score,BigDecimal weight,String state,String explanation) {
  public static ComponentView from(HealthComponentScore c){return new ComponentView(c.getComponentKey(),c.getComponentLabel(),c.getScore(),c.getWeight(),c.getState(),c.getExplanation());}
 }
 public record HealthView(Long id,Long transformerId,String status,BigDecimal healthIndex,BigDecimal coverage,String explanation,Instant assessedAt,Instant assessmentHour,List<ComponentView> components) {
  public static HealthView from(HealthAssessment h){return new HealthView(h.getId(),h.getTransformer().getId(),h.getHealthStatus(),h.getHealthIndex(),h.getCoverage(),h.getExplanation(),h.getAssessedAt(),h.getAssessmentHour(),h.getComponents().stream().map(ComponentView::from).toList());} public static HealthView summary(HealthAssessment h){return new HealthView(h.getId(),h.getTransformer().getId(),h.getHealthStatus(),h.getHealthIndex(),h.getCoverage(),h.getExplanation(),h.getAssessedAt(),h.getAssessmentHour(),h.getComponents().stream().map(ComponentView::from).toList());}
 }
 public record AlertView(Long id,Long transformerId,String conditionKey,String title,String message,String severity,String status,BigDecimal observedValue,String unit,Instant createdAt,Instant resolvedAt,Instant acknowledgedAt,String acknowledgedBy,Integer occurrenceCount,Instant lastOccurredAt,String resolutionEvidence,String operatorNotes) {
  public static AlertView from(Alert a){return new AlertView(a.getId(),a.getTransformer().getId(),a.getConditionKey(),a.getTitle(),a.getMessage(),a.getSeverity(),a.getStatus(),a.getObservedValue(),a.getUnit(),a.getCreatedAt(),a.getResolvedAt(),a.getAcknowledgedAt(),a.getAcknowledgedBy(),a.getOccurrenceCount(),a.getLastOccurredAt(),a.getResolutionEvidence(),a.getOperatorNotes());}
 }
 public record NotificationView(Long id,Long transformerId,Long alertId,String title,String message,String severity,Instant createdAt,Instant readAt){public static NotificationView from(AlertNotification n){return new NotificationView(n.getId(),n.getTransformer().getId(),n.getAlert().getId(),n.getTitle(),n.getMessage(),n.getSeverity(),n.getCreatedAt(),n.getReadAt());}}
 public record DiagnosticCreate(@NotBlank @Size(max=60) String testType,@NotBlank @Size(max=80) String parameterName,
  @NotNull @DecimalMin("-1000000") @DecimalMax("1000000") BigDecimal value,@NotBlank @Size(max=20) String unit,
  @NotNull Instant testDate,@Size(max=160) String laboratory,@Size(max=500) String referenceNote,@Size(max=1000) String notes,
  @Pattern(regexp="MAIN_TANK|CONSERVATOR|HV_BUSHING|LV_BUSHING|RADIATOR|COOLING_FAN|OLTC|CONTROL_CABINET") String componentKey,@Pattern(regexp="A|B|C|a|b|c") String phase,
  @Size(max=120) String testMethod,@Size(max=80) String samplePoint,BigDecimal testTemperatureC,@Size(max=40) String testVoltage,@Min(0) @Max(86400) Integer testDurationSeconds,@Size(max=180) String baselineReference,@Size(max=100) String sampleId) {}
 public record DiagnosticView(Long id,Long transformerId,String testType,String parameterName,BigDecimal value,String unit,Instant testDate,String source,String laboratory,String referenceNote,String notes,String componentKey,String phase,String testMethod,String samplePoint,BigDecimal testTemperatureC,String testVoltage,Integer testDurationSeconds,String baselineReference,String sampleId) {
  public static DiagnosticView from(DiagnosticTest d){return new DiagnosticView(d.getId(),d.getTransformer().getId(),d.getTestType(),d.getParameterName(),d.getValue(),d.getUnit(),d.getTestDate(),d.getSource().name(),d.getLaboratory(),d.getReferenceNote(),d.getNotes(),d.getComponentKey(),d.getPhase(),d.getTestMethod(),d.getSamplePoint(),d.getTestTemperatureC(),d.getTestVoltage(),d.getTestDurationSeconds(),d.getBaselineReference(),d.getSampleId());}
 }
 public record MaintenanceCreate(@NotBlank @Size(max=60) String activityType,@NotBlank @Size(max=80) String componentKey,@NotBlank @Size(max=1000) String description,@NotNull Instant performedAt,@Size(max=120) String performedBy,@Size(max=1000) String findings,Instant nextDueAt,Long relatedAlertId,Long relatedDiagnosticId){}
 public record MaintenanceView(Long id,Long transformerId,String activityType,String componentKey,String description,Instant performedAt,String performedBy,String findings,Instant nextDueAt,Long relatedAlertId,Long relatedDiagnosticId){public static MaintenanceView from(MaintenanceRecord m){return new MaintenanceView(m.getId(),m.getTransformer().getId(),m.getActivityType(),m.getComponentKey(),m.getDescription(),m.getPerformedAt(),m.getPerformedBy(),m.getFindings(),m.getNextDueAt(),m.getRelatedAlertId(),m.getRelatedDiagnosticId());}}
 public record Dashboard(TransformerView transformer,HealthView health,List<ReadingView> latestReadings,long activeAlerts,Instant lastUpdated){}
 public record PageResponse<T>(List<T> content,int page,int size,long totalElements,int totalPages,boolean first,boolean last){}
 public static <A,B> PageResponse<B> page(org.springframework.data.domain.Page<A> p,java.util.function.Function<A,B> map){return new PageResponse<>(p.getContent().stream().map(map).toList(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages(),p.isFirst(),p.isLast());}
 public record ErrorResponse(String code,String message,Instant timestamp){}
}
