package com.rsoft.hurmanagement.hurmasterdata.util;

import com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaie;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PayrollTaxePeriod {

    private PayrollTaxePeriod() {
    }

    public static BigDecimal toAnnualAmount(BigDecimal montant,
                                            RegimePaie.Periodicite periodicite,
                                            int periodMultiplier) {
        if (montant == null) {
            return BigDecimal.ZERO;
        }
        int factor = annualFactor(periodicite);
        int multiplier = Math.max(1, periodMultiplier);
        BigDecimal adjustedFactor = BigDecimal.valueOf(factor)
                .divide(BigDecimal.valueOf(multiplier), 6, RoundingMode.HALF_UP);
        return montant.multiply(adjustedFactor);
    }

    public static BigDecimal fromAnnualAmount(BigDecimal montantAnnuel,
                                              RegimePaie.Periodicite periodicite,
                                              int periodMultiplier) {
        if (montantAnnuel == null) {
            return BigDecimal.ZERO;
        }
        int factor = annualFactor(periodicite);
        int multiplier = Math.max(1, periodMultiplier);
        BigDecimal adjustedFactor = BigDecimal.valueOf(factor)
                .divide(BigDecimal.valueOf(multiplier), 6, RoundingMode.HALF_UP);
        if (adjustedFactor.compareTo(BigDecimal.ZERO) <= 0) {
            return montantAnnuel;
        }
        return montantAnnuel.divide(adjustedFactor, 6, RoundingMode.HALF_UP);
    }

    public static int annualFactor(RegimePaie.Periodicite periodicite) {
        if (periodicite == null) {
            return 1;
        }
        return switch (periodicite) {
            case JOURNALIER -> 365;
            case HEBDO -> 52;
            case QUINZAINE -> 26;
            case QUINZOMADAIRE -> 24;
            case MENSUEL -> 12;
            case TRIMESTRIEL -> 4;
            case SEMESTRIEL -> 2;
            case ANNUEL -> 1;
        };
    }
}
