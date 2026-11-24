package com.transformertwin.service;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class ThermalModelTest{
 @Test void temperatureMovesTowardSteadyStateWithoutJumping(){double next=ThermalModel.next(30,60,1,180);assertTrue(next>30&&next<60);}
 @Test void higherLoadRaisesSteadyThermalRise(){double low=ThermalModel.steadyTopOilRise(.6,55,5,.8,1);double high=ThermalModel.steadyTopOilRise(1.1,55,5,.8,1);assertTrue(high>low);}
 @Test void fanFailureFactorRaisesSteadyTopOilRise(){double cooled=ThermalModel.steadyTopOilRise(1,55,5,.8,.72);double failed=ThermalModel.steadyTopOilRise(1,55,5,.8,1);assertTrue(failed>cooled);}
 @Test void negativeElapsedTimeIsRejected(){assertThrows(IllegalArgumentException.class,()->ThermalModel.next(1,2,-1,10));}
}
