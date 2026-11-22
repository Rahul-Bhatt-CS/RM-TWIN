package com.transformertwin.service;

import com.transformertwin.domain.*;
import com.transformertwin.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class HealthService {
    private final SensorReadingRepository readingRepository;
    private final HealthAssessmentRepository assessmentRepository;
    private final AlertService alertService;
    @Value("${app.simulation.warning-hotspot-c:95}") private BigDecimal warningHotspot;
    @Value("${app.simulation.critical-hotspot-c:110}") private BigDecimal criticalHotspot;
    @Value("${app.simulation.warning-load-percent:100}") private BigDecimal warningLoad;

    public HealthService(SensorReadingRepository readings, HealthAssessmentRepository assessments, AlertService alerts) {
        this.readingRepository = readings; this.assessmentRepository = assessments; this.alertService = alerts;
    }

    @Transactional
    public HealthAssessment assess(Transformer transformer, Map<MeasurementType, SensorReading> latest, Instant at) {
        SensorReading hotspot = latest.get(MeasurementType.HOTSPOT_TEMPERATURE);
        SensorReading load = latest.get(MeasurementType.LOAD);
        if (hotspot == null || load == null) {
            return assessmentRepository.save(new HealthAssessment(transformer, HealthStatus.UNAVAILABLE,
                    "Insufficient simulated measurements to evaluate the configured demo rules.", at));
        }

        HealthStatus status = HealthStatus.NORMAL;
        String explanation = "No configured demo degradation condition is currently met.";
        if (hotspot.getValue().compareTo(criticalHotspot) >= 0) {
            alertService.resolveIfActive(transformer, "HOTSPOT_WARNING");
            status = HealthStatus.CRITICAL;
            explanation = "Winding hot-spot temperature has reached the configured critical demo trigger.";
            alertService.raise(transformer, "HOTSPOT_CRITICAL", "CRITICAL", hotspot.getValue(), criticalHotspot, "°C", at);
        } else if (hotspot.getValue().compareTo(warningHotspot) >= 0) {
            alertService.resolveIfActive(transformer, "HOTSPOT_CRITICAL");
            status = HealthStatus.WARNING;
            explanation = "Winding hot-spot temperature has reached the configured warning demo trigger.";
            alertService.raise(transformer, "HOTSPOT_WARNING", "WARNING", hotspot.getValue(), warningHotspot, "°C", at);
        } else {
            alertService.resolveIfActive(transformer, "HOTSPOT_WARNING");
            alertService.resolveIfActive(transformer, "HOTSPOT_CRITICAL");
        }

        if (load.getValue().compareTo(warningLoad) >= 0 && status == HealthStatus.NORMAL) {
            status = HealthStatus.WARNING;
            explanation = "Load has reached the configured warning demo trigger.";
            alertService.raise(transformer, "LOAD_WARNING", "WARNING", load.getValue(), warningLoad, "%", at);
        } else if (load.getValue().compareTo(warningLoad) < 0) {
            alertService.resolveIfActive(transformer, "LOAD_WARNING");
        }

        return assessmentRepository.save(new HealthAssessment(transformer, status, explanation, at));
    }
}
