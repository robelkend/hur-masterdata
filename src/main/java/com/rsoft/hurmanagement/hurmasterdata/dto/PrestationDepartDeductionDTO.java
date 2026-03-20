package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrestationDepartDeductionDTO {
    private Long id;
    private Long payrollEmployeId;
    private String codeDeduction;
    private String libelle;
    private String categorie;
    private BigDecimal baseMontant;
    private BigDecimal taux;
    private BigDecimal montant;
    private String referenceExterne;
    private BigDecimal montantCouvert;
}
