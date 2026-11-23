package com.transformertwin.service;

/** Ideal balanced three-phase apparent-power calculations used by the demo simulator. */
public final class ElectricalCalculations {
    private ElectricalCalculations() {}

    public static double apparentPowerMva(double ratedPowerMva, double loadPercent) {
        if (!Double.isFinite(ratedPowerMva) || ratedPowerMva <= 0 || !Double.isFinite(loadPercent) || loadPercent < 0) {
            throw new IllegalArgumentException("Rated power must be positive and load percent must be non-negative.");
        }
        return ratedPowerMva * loadPercent / 100.0;
    }

    public static double balancedLineCurrentA(double apparentPowerMva, double lineVoltageKv) {
        if (!Double.isFinite(apparentPowerMva) || apparentPowerMva < 0 || !Double.isFinite(lineVoltageKv) || lineVoltageKv <= 0) {
            throw new IllegalArgumentException("Apparent power must be non-negative and line voltage must be positive.");
        }
        return apparentPowerMva * 1000.0 / (Math.sqrt(3.0) * lineVoltageKv);
    }
}
