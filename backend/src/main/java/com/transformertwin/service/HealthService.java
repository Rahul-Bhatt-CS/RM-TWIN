package com.transformertwin.service;

import com.transformertwin.domain.*;
import com.transformertwin.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
public class HealthService {
    private final SensorReadingRepository readings;
    private final DiagnosticTestRepository diagnostics;
    private final ProtectionEventRepository protections;
    private final HealthAssessmentRepository assessments;
    private final AlertService alerts;

    @Value("${app.health.minimum-coverage:0.4}") private BigDecimal minimumCoverage;
    // Demonstration triggers only; not IEC/CEA/OEM-prescribed operating limits.
    @Value("${app.demo.warning-hotspot-c:95}") private BigDecimal warningHotspot;
    @Value("${app.demo.critical-hotspot-c:110}") private BigDecimal criticalHotspot;
    @Value("${app.demo.warning-load-percent:100}") private BigDecimal warningLoad;

    public HealthService(SensorReadingRepository readings, DiagnosticTestRepository diagnostics,
                         ProtectionEventRepository protections, HealthAssessmentRepository assessments,
                         AlertService alerts) {
        this.readings = readings; this.diagnostics = diagnostics; this.protections = protections;
        this.assessments = assessments; this.alerts = alerts;
    }

    @Transactional
    public HealthAssessment assess(Transformer transformer) {
        Instant now = Instant.now();
        List<HealthComponentScore> parts = new ArrayList<>();
        BigDecimal weighted = BigDecimal.ZERO;
        BigDecimal coverage = BigDecimal.ZERO;
        boolean warningDetected = false;
        boolean criticalDetected = false;

        Optional<SensorReading> hot = latest(transformer, MeasurementType.WINDING_HOTSPOT_TEMPERATURE);
        Optional<SensorReading> load = latest(transformer, MeasurementType.LOAD_PERCENT);
        Optional<SensorReading> oil = latest(transformer, MeasurementType.TOP_OIL_TEMPERATURE);
        List<BigDecimal> thermalScores = new ArrayList<>();
        List<String> thermalEvidence = new ArrayList<>();

        if (hot.isPresent()) {
            BigDecimal value = hot.get().getValue();
            BigDecimal score = value.compareTo(criticalHotspot) >= 0 ? bd("10")
                    : value.compareTo(warningHotspot) >= 0 ? bd("45") : bd("90");
            thermalScores.add(score); thermalEvidence.add("Winding hot spot " + value + " °C");
            if (value.compareTo(criticalHotspot) >= 0) {
                criticalDetected = true;
                alerts.raise(transformer, "HOTSPOT_CRITICAL", "Critical winding hot-spot",
                        "Hot-spot crossed the configured demonstration trigger; this is not a trip command.",
                        "CRITICAL", value, "°C");
                alerts.resolve(transformer, "HOTSPOT_WARNING");
            } else {
                alerts.resolve(transformer, "HOTSPOT_CRITICAL");
                if (value.compareTo(warningHotspot) >= 0) {
                    warningDetected = true;
                    alerts.raise(transformer, "HOTSPOT_WARNING", "High winding hot-spot",
                            "Hot-spot crossed the configured demonstration warning trigger.", "WARNING", value, "°C");
                } else alerts.resolve(transformer, "HOTSPOT_WARNING");
            }
        }
        if (load.isPresent()) {
            BigDecimal value = load.get().getValue();
            thermalScores.add(value.compareTo(warningLoad) >= 0 ? bd("40") : bd("90"));
            thermalEvidence.add("Load " + value + "%");
            if (value.compareTo(warningLoad) >= 0) {
                warningDetected = true;
                alerts.raise(transformer, "LOAD_HIGH", "High loading",
                        "Load crossed the configured demonstration trigger; duration and transformer loading guide are not modelled.",
                        "WARNING", value, "%");
            } else alerts.resolve(transformer, "LOAD_HIGH");
        }
        if (oil.isPresent()) thermalEvidence.add("Top oil " + oil.get().getValue() + " °C");

        Optional<SensorReading> fan = latest(transformer, MeasurementType.COOLING_FAN_STATUS);
        if (fan.isPresent() && fan.get().getValue().compareTo(BigDecimal.ZERO) == 0 && hot.isPresent()
                && hot.get().getValue().compareTo(warningHotspot) >= 0) {
            warningDetected = true;
            alerts.raise(transformer, "COOLING_ASSIST_UNAVAILABLE", "Cooling fan not running during elevated hot-spot scenario",
                    "The demonstration simulator reports fan OFF while hot-spot is above its configured demo warning trigger. Verify actual cooling requirements and auxiliary status on site.",
                    "WARNING", fan.get().getValue(), "ON=1 / OFF=0");
        } else alerts.resolve(transformer, "COOLING_ASSIST_UNAVAILABLE");

        Optional<SensorReading> pump = latest(transformer, MeasurementType.COOLING_PUMP_STATUS);
        if (pump.isPresent() && pump.get().getValue().compareTo(BigDecimal.ZERO) == 0) {
            warningDetected = true;
            alerts.raise(transformer, "COOLING_PUMP_UNAVAILABLE", "Cooling pump reported OFF",
                    "The demonstration telemetry reports pump OFF. Applicability depends on installed cooling design.",
                    "WARNING", pump.get().getValue(), "ON=1 / OFF=0");
        } else alerts.resolve(transformer, "COOLING_PUMP_UNAVAILABLE");

        if (!thermalScores.isEmpty()) {
            BigDecimal score = average(thermalScores);
            parts.add(part("THERMAL", "Thermal and loading", score, "0.25", "SIMULATED_SCENARIO",
                    String.join("; ", thermalEvidence) + ". Score uses the project's illustrative demonstration rules, not an OEM-calibrated thermal model."));
            weighted = weighted.add(score.multiply(bd("0.25"))); coverage = coverage.add(bd("0.25"));
        } else parts.add(part("THERMAL", "Thermal and loading", null, "0.25", "UNKNOWN", "No current thermal or load readings."));

        List<DiagnosticTest> tests = diagnostics.findTop100ByTransformerIdOrderByTestDateDesc(transformer.getId());
        List<DiagnosticTest> oilTests = byType(tests, "DGA", "OIL_TEST");
        List<DiagnosticTest> insulationTests = byType(tests, "INSULATION_TEST", "DIELECTRIC_TEST", "BUSHING_TEST");
        List<DiagnosticTest> mechanicalTests = byType(tests, "FRA_TEST", "ELECTRICAL_TEST", "PARTIAL_DISCHARGE_TEST", "OLTC_TEST", "COOLING_TEST");

        GroupResult oilGroup = diagnosticGroup(oilTests, "OIL_DGA", "Oil and dissolved-gas condition", "0.30", bd("84"),
                "Synthetic DGA/oil sample records are available. These values are not automatically diagnosed against IEC 60599 or IEEE C57.104; use asset-specific baselines, sampling context and expert interpretation.");
        parts.add(oilGroup.part());
        if (oilGroup.assessed()) { weighted = weighted.add(oilGroup.part().getScore().multiply(bd("0.30"))); coverage = coverage.add(bd("0.30")); }

        GroupResult insulationGroup = diagnosticGroup(insulationTests, "INSULATION", "Insulation condition", "0.25", bd("88"),
                "Synthetic IR, PI, dielectric and phase-specific bushing test records are available. The scenario score is illustrative; no pass/fail conclusion is made against IEC/IS limits or OEM baselines.");
        parts.add(insulationGroup.part());
        if (insulationGroup.assessed()) { weighted = weighted.add(insulationGroup.part().getScore().multiply(bd("0.25"))); coverage = coverage.add(bd("0.25")); }

        GroupResult mechanicalGroup = diagnosticGroup(mechanicalTests, "MECHANICAL", "Mechanical and electrical condition", "0.15", bd("86"),
                "Synthetic FRA, winding-resistance, partial-discharge and OLTC diagnostic records are available. No automatic fault classification is applied; compare with factory baselines and method-specific criteria.");
        parts.add(mechanicalGroup.part());
        if (mechanicalGroup.assessed()) { weighted = weighted.add(mechanicalGroup.part().getScore().multiply(bd("0.15"))); coverage = coverage.add(bd("0.15")); }

        Page<ProtectionEvent> recent = protections.findByTransformerIdOrderByOccurredAtDesc(transformer.getId(), PageRequest.of(0, 1));
        if (!recent.isEmpty()) {
            ProtectionEvent event = recent.getContent().get(0);
            String key = "PROTECTION_EVENT_" + event.getDeviceName().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "_");
            if (event.isActive()) {
                criticalDetected = true;
                alerts.raise(transformer, key, "Active protection event reported",
                        event.getDeviceName() + " reported " + event.getEventType() + ". Review the original protection system and site procedure.",
                        "CRITICAL", null, null);
                parts.add(part("PROTECTION", "Protection and auxiliary systems", null, "0.05", "ATTENTION",
                        "An active protection event is recorded. This dashboard does not replace the relay or protection system."));
            } else if (event.getSource() == DataSourceType.SIMULATED && event.getEventType().startsWith("SIMULATED_")) {
                alerts.resolve(transformer, key);
                BigDecimal score = bd("95");
                parts.add(part("PROTECTION", "Protection and auxiliary systems", score, "0.05", "SIMULATED_SCENARIO",
                        "Synthetic normal-indication records are available for the protection/auxiliary UI. They are not relay self-tests and do not prove protection health."));
                weighted = weighted.add(score.multiply(bd("0.05"))); coverage = coverage.add(bd("0.05"));
            } else {
                alerts.resolve(transformer, key);
                parts.add(part("PROTECTION", "Protection and auxiliary systems", null, "0.05", "RECORDED_NOT_VERIFIED",
                        "A protection event record exists, but an inactive event does not prove the complete protection system is healthy."));
            }
        } else parts.add(part("PROTECTION", "Protection and auxiliary systems", null, "0.05", "UNKNOWN",
                "No protection-event or simulated status record is available."));

        BigDecimal score = coverage.signum() == 0 ? null : weighted.divide(coverage, 2, RoundingMode.HALF_UP);
        String status = score == null || coverage.compareTo(minimumCoverage) < 0 ? "UNAVAILABLE"
                : score.compareTo(bd("40")) < 0 ? "CRITICAL" : score.compareTo(bd("65")) < 0 ? "WARNING" : "NORMAL";
        if (criticalDetected) status = "CRITICAL"; else if (warningDetected && "NORMAL".equals(status)) status = "WARNING";
        String explanation = score == null ? "Insufficient data to calculate the demonstration index."
                : "Demonstration health index " + score + " / 100 based on "
                + coverage.multiply(bd("100")).setScale(0, RoundingMode.HALF_UP)
                + "% weighted data coverage. Synthetic diagnostic scores are illustrative scenario values, not standards-derived health ratings. Imported records without an interpretation method are not scored.";
        HealthAssessment assessment = new HealthAssessment(transformer, status, score, coverage, explanation, now);
        parts.forEach(assessment::addComponent);
        return assessments.save(assessment);
    }

    private GroupResult diagnosticGroup(List<DiagnosticTest> group, String key, String label, String weight,
                                        BigDecimal demoScore, String explanation) {
        boolean hasSimulated = group.stream().anyMatch(x -> x.getSource() == DataSourceType.SIMULATED);
        boolean hasImported = !group.isEmpty();
        if (hasSimulated) {
            String details = group.size() + " synthetic diagnostic records available. " + explanation;
            return new GroupResult(part(key, label, demoScore, weight, "SIMULATED_SCENARIO", details), true);
        }
        if (hasImported) {
            return new GroupResult(part(key, label, null, weight, "RECORDED_NOT_INTERPRETED",
                    group.size() + " imported diagnostic records exist, but no validated interpretation method/baseline is configured. " + explanation), false);
        }
        return new GroupResult(part(key, label, null, weight, "UNKNOWN", "No diagnostic records are available."), false);
    }

    private List<DiagnosticTest> byType(List<DiagnosticTest> tests, String... types) {
        Set<String> allowed = Set.of(types);
        return tests.stream().filter(x -> allowed.contains(x.getTestType().toUpperCase(Locale.ROOT))).toList();
    }

    private Optional<SensorReading> latest(Transformer t, MeasurementType type) {
        return readings.findTopByTransformerIdAndMeasurementTypeOrderByRecordedAtDesc(t.getId(), type);
    }
    private BigDecimal average(List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }
    private BigDecimal bd(String value) { return new BigDecimal(value); }
    private HealthComponentScore part(String key, String label, BigDecimal score, String weight, String state, String explanation) {
        return new HealthComponentScore(key, label, score, bd(weight), state, explanation);
    }
    private record GroupResult(HealthComponentScore part, boolean assessed) {}
}
