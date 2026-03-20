package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeuillePayrollReportRowDTO {
    private String codeEmploye;
    private String nomEmploye;
    private String prenomEmploye;
    private LocalDate datePremiereEmbauche;
    private BigDecimal montantSalaireBase;
    private BigDecimal montantSupplementaire;
    private BigDecimal montantAutreRevenu;
    private BigDecimal montantDeductions;
    private BigDecimal montantRecouvrements;
    private BigDecimal montantSanctions;
    private BigDecimal montantBrut;
    private BigDecimal montantNetAPayer;
}
