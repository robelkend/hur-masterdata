package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceUniteReportRowDTO {
    private String codeEmploye;
    private String nomEmploye;
    private Map<String, String> valeursParDate;
    private BigDecimal nbHeuresTot;
}
