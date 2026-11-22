package com.transformertwin.api;

import com.transformertwin.api.dto.ApiDtos;
import com.transformertwin.domain.*;
import com.transformertwin.repository.*;
import com.transformertwin.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TransformerController {
    private final TransformerRepository transformers;
    private final SensorReadingRepository readings;
    private final HealthAssessmentRepository assessments;
    private final AlertService alerts;
    private final RealtimeService realtime;

    public TransformerController(TransformerRepository transformers, SensorReadingRepository readings,
                                 HealthAssessmentRepository assessments, AlertService alerts, RealtimeService realtime) {
        this.transformers = transformers; this.readings = readings; this.assessments = assessments;
        this.alerts = alerts; this.realtime = realtime;
    }

    @GetMapping("/transformers")
    public List<ApiDtos.TransformerDto> list() {
        return transformers.findAll().stream().map(ApiDtos.TransformerDto::from).toList();
    }

    @GetMapping("/transformers/{id}")
    public ApiDtos.TransformerDto get(@PathVariable Long id) {
        return ApiDtos.TransformerDto.from(findTransformer(id));
    }

    @GetMapping("/transformers/{id}/dashboard")
    public ApiDtos.DashboardDto dashboard(@PathVariable Long id) {
        Transformer transformer = findTransformer(id);
        List<SensorReading> latest = new ArrayList<>();
        for (MeasurementType type : MeasurementType.values()) {
            readings.findTopByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(id, type).ifPresent(latest::add);
        }
        HealthAssessment health = assessments.findTopByTransformerIdOrderByAssessedAtDesc(id).orElse(null);
        Instant last = latest.stream().map(SensorReading::getRecordedAt).max(Instant::compareTo).orElse(null);
        long active = alerts.findAll(id).stream().filter(a -> "ACTIVE".equals(a.getStatus())).count();
        return new ApiDtos.DashboardDto(id, transformer.getName(), "SIMULATED",
                health == null ? null : ApiDtos.HealthDto.from(health), last,
                latest.stream().map(ApiDtos.ReadingDto::from).toList(), active);
    }

    @GetMapping("/transformers/{id}/readings/latest")
    public ApiDtos.LatestReadingsDto latest(@PathVariable Long id) {
        findTransformer(id);
        List<ApiDtos.ReadingDto> result = new ArrayList<>();
        for (MeasurementType type : MeasurementType.values()) {
            readings.findTopByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(id, type)
                    .ifPresent(r -> result.add(ApiDtos.ReadingDto.from(r)));
        }
        return new ApiDtos.LatestReadingsDto(id, "SIMULATED", result);
    }

    @GetMapping("/transformers/{id}/history")
    public ApiDtos.HistoryDto history(@PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) MeasurementType measurementType) {
        findTransformer(id);
        if (!from.isBefore(to)) throw new IllegalArgumentException("'from' must be earlier than 'to'.");
        List<ApiDtos.ReadingDto> result = readings.findByTransformerIdAndRecordedAtBetweenOrderByRecordedAtDesc(id, from, to)
                .stream().filter(r -> measurementType == null || r.getMeasurementType() == measurementType)
                .map(ApiDtos.ReadingDto::from).toList();
        return new ApiDtos.HistoryDto(id, from, to, result);
    }

    @GetMapping("/transformers/{id}/health/history")
    public ApiDtos.HealthHistoryDto healthHistory(@PathVariable Long id) {
        findTransformer(id);
        return new ApiDtos.HealthHistoryDto(id, assessments.findByTransformerIdOrderByAssessedAtDesc(id)
                .stream().map(ApiDtos.HealthDto::from).toList());
    }

    @GetMapping("/transformers/{id}/alerts")
    public ApiDtos.AlertsDto getAlerts(@PathVariable Long id) {
        findTransformer(id);
        List<ApiDtos.AlertDto> result = alerts.findAll(id).stream()
                .map(a -> ApiDtos.AlertDto.from(a, unitForCondition(a.getCondition()))).toList();
        return new ApiDtos.AlertsDto(id, result);
    }

    @GetMapping(value = "/transformers/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable Long id) {
        findTransformer(id);
        return realtime.subscribe();
    }

    private Transformer findTransformer(Long id) {
        return transformers.findById(id).orElseThrow(() -> new NoSuchElementException("Transformer " + id + " was not found."));
    }
    private String unitForCondition(String condition) {
        return condition.startsWith("LOAD") ? "%" : "°C";
    }
}
