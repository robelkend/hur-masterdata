package com.rsoft.hurmanagement.hurmasterdata.util;

import com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaie;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollTaxePeriodTest {

    @Test
    void annualizesWithMultiplier() {
        BigDecimal montant = new BigDecimal("100");
        BigDecimal annual = PayrollTaxePeriod.toAnnualAmount(montant, RegimePaie.Periodicite.QUINZAINE, 2);
        assertEquals(0, annual.compareTo(new BigDecimal("1300.000000")));
    }

    @Test
    void deAnnualizesWithMultiplier() {
        BigDecimal annual = new BigDecimal("1200");
        BigDecimal period = PayrollTaxePeriod.fromAnnualAmount(annual, RegimePaie.Periodicite.MENSUEL, 3);
        assertEquals(0, period.compareTo(new BigDecimal("300.000000")));
    }

    @Test
    void defaultsMultiplierToOne() {
        BigDecimal montant = new BigDecimal("200");
        BigDecimal annual = PayrollTaxePeriod.toAnnualAmount(montant, RegimePaie.Periodicite.MENSUEL, 0);
        assertEquals(0, annual.compareTo(new BigDecimal("2400.000000")));
    }
}
