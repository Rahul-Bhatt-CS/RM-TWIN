package com.transformertwin.service;

import com.transformertwin.api.dto.ApiDtos;
import com.transformertwin.domain.*;
import com.transformertwin.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
public class SimulationService {
    private final TransformerRepository transformerRepository;
    private final SensorReadingRepository readingRepository;
    private final HealthService healthService;
    private final HealthAssessmentRepository assessmentRepository;
    private final AlertRepository alertRepository;
    private final RealtimeService realtimeService;
    private long tick = 0;

    @Value("${app.simulation.enabled:true}") private boolean enabled;

    public SimulationService(TransformerRepository transformers, SensorReadingRepository readings,
                             HealthService healthService, HealthAssessmentRepository assessments,
                             AlertRepository alerts, RealtimeService realtime) {
        this.transformerRepository = transformers; this.readingRepository = readings;
        this.healthService = healthService; this.assessmentRepository = assessments;
        this.alertRepository = alerts; this.realtimeService = realtime;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedTransformer() {
        if (transformerRepository.count() == 0) {
            transformerRepository.save(new Transformer("Transformer T-01", "DEMO-T01", null));
        }
    }

    @Scheduled(fixedDelayString = "${app.simulation.interval-ms:5000}")
    @Transactional
    public void generateTick() {
        if (!enabled) return;
        Transformer transformer = transformerRepository.findAll().stream().findFirst().orElse(null);
        if (transformer == null) return;

        // Deterministic demo sequence: 0-5 normal, 6-11 stress, 12-17 degraded, 18-23 recovery.
        int phase = (int) (tick % 24);
        double load;
        double oil;
        double hotspot;
        if (phase < 6) {
            load = 65 + (phase * 1.4);
            oil = 55 + (phase * 0.7);
            hotspot = 70 + (phase * 1.0);
        } else if (phase < 12) {
            int p = phase - 6;
            load = 78 + p * 4.2;
            oil = 60 + p * 3.0;
            hotspot = 78 + p * 3.5;
        } else if (phase < 18) {
            int p = phase - 12;
            load = 103 + p * 1.5;
            oil = 78 + p * 2.0;
            hotspot = 99 + p * 2.8;
        } else {
            int p = phase - 18;
            load = 92 - p * 5.0;
            oil = 76 - p * 3.5;
            hotspot = 94 - p * 4.5;
        }

        Instant now = Instant.now();
        readingRepository.saveAll(List.of(
                new SensorReading(transformer, MeasurementType.OIL_TEMPERATURE, bd(oil), "°C", now),
                new SensorReading(transformer, MeasurementType.HOTSPOT_TEMPERATURE, bd(hotspot), "°C", now),
                new SensorReading(transformer, MeasurementType.LOAD, bd(load), "%", now)
        ));

        Map<MeasurementType, SensorReading> latest = new EnumMap<>(MeasurementType.class);
        for (MeasurementType type : MeasurementType.values()) {
            readingRepository.findTopByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(transformer.getId(), type)
                    .ifPresent(r -> latest.put(type, r));
        }
        healthService.assess(transformer, latest, now);
        tick++;

        var health = assessmentRepository.findTopByTransformerIdOrderByAssessedAtDesc(transformer.getId()).orElse(null);
        var readings = readingRepository.findTop3ByTransformerIdOrderByRecordedAtDesc(transformer.getId())
                .stream().sorted(Comparator.comparing(SensorReading::getMeasurementType))
                .map(ApiDtos.ReadingDto::from).toList();
        var activeCount = alertRepository.findByTransformerIdOrderByCreatedAtDesc(transformer.getId())
                .stream().filter(a -> "ACTIVE".equals(a.getStatus())).count();
        var dashboard = new ApiDtos.DashboardDto(transformer.getId(), transformer.getName(), "SIMULATED",
                health == null ? null : ApiDtos.HealthDto.from(health), now, readings, activeCount);
        realtimeService.publish(new ApiDtos.LiveEvent("DASHBOARD_UPDATED", dashboard, null));
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
