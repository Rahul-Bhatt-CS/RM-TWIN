package com.transformertwin.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ElectricalCalculationsTest {
    @Test void calculatesRatedThreePhaseCurrentsForReferenceTransformer() {
        double apparent = ElectricalCalculations.apparentPowerMva(100.0, 100.0);
        assertEquals(100.0, apparent, 1e-9);
        assertEquals(262.43, ElectricalCalculations.balancedLineCurrentA(apparent, 220.0), 0.02);
        assertEquals(1749.55, ElectricalCalculations.balancedLineCurrentA(apparent, 33.0), 0.02);
    }

    @Test void rejectsInvalidNameplateInputs() {
        assertThrows(IllegalArgumentException.class, () -> ElectricalCalculations.apparentPowerMva(0, 100));
        assertThrows(IllegalArgumentException.class, () -> ElectricalCalculations.balancedLineCurrentA(100, 0));
    }
}
