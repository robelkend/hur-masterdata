package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDeductionDetailsRowDTO {
    private Long employeId;
    private String codeEmploye;
    private String nomEmploye;
    private String prenomEmploye;
    private LocalDate datePremiereEmbauche;
    private Map<String, BigDecimal> montantsByDate = new LinkedHashMap<>();
    private BigDecimal montantSupplementaire = BigDecimal.ZERO;
    private BigDecimal montantAutreRevenu = BigDecimal.ZERO;
    private BigDecimal montantDeductions = BigDecimal.ZERO;
    private BigDecimal montantBrut = BigDecimal.ZERO;
    private BigDecimal montantNetAPayer = BigDecimal.ZERO;
    private Map<String, BigDecimal> deductionsEmploye = new LinkedHashMap<>();
    private Map<String, BigDecimal> deductionsEmployeur = new LinkedHashMap<>();
}
