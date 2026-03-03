package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceReportStatsDTO {
    private int total;
    private int presences;
    private int offs;
    private int conges;
    private int feries;
    private int absences;
    private BigDecimal tauxAbsence; // percentage
    private BigDecimal totalSupplementaireMinutes;
}
