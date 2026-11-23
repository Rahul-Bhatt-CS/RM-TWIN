package com.transformertwin.service;

import com.transformertwin.domain.*;
import com.transformertwin.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SimulationService {
    private final TransformerRepository transformers;
    private final SensorReadingRepository readings;
    private final DiagnosticTestRepository diagnostics;
    private final ProtectionEventRepository protections;
    private final HealthService health;
    private final RealtimeService realtime;
    private final TransformerService transformerService;
    private final ConcurrentHashMap<Long, AtomicLong> ticks = new ConcurrentHashMap<>();

    @Value("${app.simulation.enabled:true}") private boolean enabled;

    public SimulationService(TransformerRepository transformers, SensorReadingRepository readings,
                             DiagnosticTestRepository diagnostics, ProtectionEventRepository protections,
                             HealthService health, RealtimeService realtime, TransformerService transformerService) {
        this.transformers = transformers;
        this.readings = readings;
        this.diagnostics = diagnostics;
        this.protections = protections;
        this.health = health;
        this.realtime = realtime;
        this.transformerService = transformerService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
        if (!enabled) return;
        // Seed historical synthetic diagnostic evidence once per asset. These records are
        // deliberately marked SIMULATED and are not represented as actual laboratory tests.
        for (Transformer transformer : transformers.findAll()) {
            if (transformer.isSimulationEnabled()) seedDiagnosticHistory(transformer);
        }
    }

    @Scheduled(fixedDelayString = "${app.simulation.interval-ms:5000}")
    @Transactional
    public void tick() {
        if (!enabled) return;
        for (Transformer t : transformers.findAll()) {
            if (!t.isSimulationEnabled() || t.getRatedPowerMva() == null || t.getVoltageClassKv() == null
                    || t.getLvVoltageKv() == null || t.getRatedFrequencyHz() == null) continue;

            seedDiagnosticHistory(t);
            long n = ticks.computeIfAbsent(t.getId(), x -> new AtomicLong()).getAndIncrement();
            int phase = (int) (n % 48);
            double load, oil, hot, ambient;
            if (phase < 12) {
                load = 58 + phase * .9; oil = 52 + phase * .35; hot = 68 + phase * .7;
            } else if (phase < 24) {
                int p = phase - 12; load = 72 + p * 2.8; oil = 57 + p * 1.7; hot = 77 + p * 2.1;
            } else if (phase < 36) {
                int p = phase - 24; load = 106 + p * .5; oil = 78 + p * 1.0; hot = 101 + p * 1.2;
            } else {
                int p = phase - 36; load = 96 - p * 3.0; oil = 78 - p * 2.0; hot = 94 - p * 2.0;
            }
            ambient = 27 + (n % 8) * .5;
            Instant now = Instant.now();
            double apparentMva = ElectricalCalculations.apparentPowerMva(t.getRatedPowerMva().doubleValue(), load);
            double hvCurrentA = ElectricalCalculations.balancedLineCurrentA(apparentMva, t.getVoltageClassKv().doubleValue());
            double lvCurrentA = ElectricalCalculations.balancedLineCurrentA(apparentMva, t.getLvVoltageKv().doubleValue());

            // Synthetic scenario signals, not OEM control logic or protective relay commands.
            double fanStatus = (phase >= 12 && phase < 30) ? 1.0 : 0.0;
            double pumpStatus = 1.0;
            double oilLevel = 96.0 - ((phase >= 24 && phase < 36) ? 1.5 : 0.0);
            double radiatorOutlet = Math.max(ambient + 5.0, oil - 8.0);
            double tapPosition = (phase / 8) % 5 - 2;
            double operationCount = n / 48;
            double motorCurrent = (n % 48 == 0) ? 4.2 : 0.0;

            save(t, MeasurementType.TOP_OIL_TEMPERATURE, oil, "°C", now);
            save(t, MeasurementType.WINDING_HOTSPOT_TEMPERATURE, hot, "°C", now);
            save(t, MeasurementType.LOAD_PERCENT, load, "%", now);
            save(t, MeasurementType.AMBIENT_TEMPERATURE, ambient, "°C", now);
            save(t, MeasurementType.APPARENT_POWER_MVA, apparentMva, "MVA", now);
            save(t, MeasurementType.HV_LINE_CURRENT_A, hvCurrentA, "A", now);
            save(t, MeasurementType.LV_LINE_CURRENT_A, lvCurrentA, "A", now);
            save(t, MeasurementType.COOLING_FAN_STATUS, fanStatus, "ON=1 / OFF=0", now);
            save(t, MeasurementType.COOLING_PUMP_STATUS, pumpStatus, "ON=1 / OFF=0", now);
            save(t, MeasurementType.OIL_LEVEL_PERCENT, oilLevel, "% · synthetic indication", now);
            save(t, MeasurementType.RADIATOR_OUTLET_TEMPERATURE, radiatorOutlet, "°C", now);
            save(t, MeasurementType.OLTC_TAP_POSITION, tapPosition, "position · demo", now);
            save(t, MeasurementType.OLTC_OPERATION_COUNT, operationCount, "operations · demo counter", now);
            save(t, MeasurementType.OLTC_MOTOR_CURRENT_A, motorCurrent, "A · demo", now);

            health.assess(t);
            realtime.publish(t.getId(), transformerService.dashboard(t.getId()));
        }
    }

    private void seedDiagnosticHistory(Transformer t) {
        if (!diagnostics.existsByTransformerIdAndSourceAndTestType(t.getId(), DataSourceType.SIMULATED, "DGA")) {
            Instant base = Instant.now().minus(2, ChronoUnit.DAYS);
            // Oil / DGA: sample-like values, included to exercise history and trend screens.
            addTest(t, "DGA", "HYDROGEN", 42.0, "ppm", base.minus(20, ChronoUnit.DAYS), "MAIN_TANK", null);
            addTest(t, "DGA", "METHANE", 11.0, "ppm", base.minus(20, ChronoUnit.DAYS), "MAIN_TANK", null);
            addTest(t, "DGA", "ETHANE", 4.0, "ppm", base.minus(20, ChronoUnit.DAYS), "MAIN_TANK", null);
            addTest(t, "DGA", "ETHYLENE", 8.0, "ppm", base.minus(20, ChronoUnit.DAYS), "MAIN_TANK", null);
            addTest(t, "DGA", "ACETYLENE", 0.0, "ppm", base.minus(20, ChronoUnit.DAYS), "MAIN_TANK", null);
            addTest(t, "DGA", "CARBON_MONOXIDE", 180.0, "ppm", base.minus(20, ChronoUnit.DAYS), "MAIN_TANK", null);
            addTest(t, "DGA", "CARBON_DIOXIDE", 1450.0, "ppm", base.minus(20, ChronoUnit.DAYS), "MAIN_TANK", null);
            addTest(t, "OIL_TEST", "MOISTURE", 8.0, "ppm", base, "MAIN_TANK", null);
            addTest(t, "OIL_TEST", "BREAKDOWN_VOLTAGE", 68.0, "kV", base, "MAIN_TANK", null);
            addTest(t, "OIL_TEST", "ACIDITY", 0.08, "mgKOH/g", base, "MAIN_TANK", null);
            addTest(t, "OIL_TEST", "MOISTURE", 8.2, "ppm", base, "CONSERVATOR", null);
            addTest(t, "OIL_TEST", "OIL_LEVEL_GAUGE_CHECK", 96.0, "% · synthetic indication", base, "CONSERVATOR", null);

            // Insulation diagnostics. Values are synthetic examples, not pass/fail conclusions.
            addTest(t, "INSULATION_TEST", "INSULATION_RESISTANCE", 5200.0, "MOhm", base, "MAIN_TANK", null);
            addTest(t, "INSULATION_TEST", "POLARIZATION_INDEX", 2.1, "ratio", base, "MAIN_TANK", null);
            addTest(t, "DIELECTRIC_TEST", "WINDING_TAN_DELTA", 0.0034, "ratio", base, "MAIN_TANK", null);
            addTest(t, "DIELECTRIC_TEST", "FURAN_2_FAL", 0.12, "ppm", base, "MAIN_TANK", null);

            // Phase-specific bushing diagnostic examples.
            for (String phase : new String[]{"A", "B", "C"}) {
                addTest(t, "BUSHING_TEST", "BUSHING_TAN_DELTA", 0.0038, "ratio", base, "HV_BUSHING", phase);
                addTest(t, "BUSHING_TEST", "BUSHING_CAPACITANCE", 420.0, "pF", base, "HV_BUSHING", phase);
                addTest(t, "BUSHING_TEST", "BUSHING_TAN_DELTA", 0.0041, "ratio", base, "LV_BUSHING", phase.toLowerCase());
            }

            // Mechanical / electrical diagnostics and OLTC examples.
            addTest(t, "FRA_TEST", "FRA_DEVIATION", 1.8, "%", base, "MAIN_TANK", null);
            addTest(t, "ELECTRICAL_TEST", "WINDING_RESISTANCE", 0.15, "ohm", base, "MAIN_TANK", null);
            addTest(t, "PARTIAL_DISCHARGE_TEST", "APPARENT_CHARGE", 85.0, "pC · illustrative", base, "MAIN_TANK", null);
            addTest(t, "OLTC_TEST", "CONTACT_RESISTANCE", 82.0, "microohm", base, "OLTC", null);
            addTest(t, "OLTC_TEST", "OPERATING_TIME", 65.0, "ms", base, "OLTC", null);
            addTest(t, "COOLING_TEST", "RADIATOR_TEMPERATURE_DIFFERENCE", 8.0, "°C", base, "RADIATOR", null);
            addTest(t, "COOLING_TEST", "FAN_VIBRATION", 2.1, "mm/s · illustrative", base, "COOLING_FAN", null);
        }

        if (!protections.existsByTransformerIdAndSourceAndEventType(t.getId(), DataSourceType.SIMULATED, "SIMULATED_STATUS_AVAILABLE")) {
            protections.save(new ProtectionEvent(t, "Protection and auxiliary panel", "SIMULATED_STATUS_AVAILABLE", false,
                    Instant.now().minus(1, ChronoUnit.HOURS), DataSourceType.SIMULATED,
                    "Synthetic demo status record only. This is not a relay self-test, protection certification or proof of trip-circuit health."));
            protections.save(new ProtectionEvent(t, "Buchholz relay", "SIMULATED_NORMAL_INDICATION", false,
                    Instant.now().minus(55, ChronoUnit.MINUTES), DataSourceType.SIMULATED,
                    "Synthetic indication for UI demonstration; not connected to a physical relay."));
            protections.save(new ProtectionEvent(t, "Pressure relief device", "SIMULATED_NORMAL_INDICATION", false,
                    Instant.now().minus(50, ChronoUnit.MINUTES), DataSourceType.SIMULATED,
                    "Synthetic indication for UI demonstration; not connected to a physical device."));
        }
    }

    private void addTest(Transformer t, String type, String parameter, double value, String unit, Instant date,
                         String component, String phase) {
        diagnostics.save(new DiagnosticTest(t, type, parameter, bd(value), unit, date, DataSourceType.SIMULATED,
                "GridPulse synthetic data generator", "Synthetic demonstration value; not a measured result or standard pass/fail limit.",
                "Generated for UI and workflow demonstration only. Do not use for operating decisions.", component, phase));
    }

    private void save(Transformer t, MeasurementType type, double value, String unit, Instant at) {
        readings.save(new SensorReading(t, type, bd(value), unit, DataSourceType.SIMULATED, at));
    }

    private BigDecimal bd(double value) { return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP); }
}
