package com.transformertwin.service;
/** First-order dynamic thermal response using the structure described by IEC 60076-7.
 * Inputs must come from OEM heat-run data for an operational model; defaults are illustrative only.
 */
public final class ThermalModel{
 private ThermalModel(){}
 public static double steadyTopOilRise(double loadPu,double ratedRiseK,double lossRatio,double exponent,double coolingFactor){double k=Math.max(0,loadPu);return ratedRiseK*Math.pow((k*k*lossRatio+1)/(lossRatio+1),exponent)*coolingFactor;}
 public static double steadyHotspotGradient(double loadPu,double ratedGradientK,double exponent){return ratedGradientK*Math.pow(Math.max(0,loadPu),exponent);}
 public static double next(double previous,double steady,double elapsedMinutes,double timeConstantMinutes){if(timeConstantMinutes<=0||elapsedMinutes<0)throw new IllegalArgumentException("Time and time constant must be positive.");return previous+(steady-previous)*(1-Math.exp(-elapsedMinutes/timeConstantMinutes));}
}
