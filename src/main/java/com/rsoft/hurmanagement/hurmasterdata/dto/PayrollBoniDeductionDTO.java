package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayrollBoniDeductionDTO {
    private Long id;
    private Long payrollBoniId;
    private Long employeId;
    private String codeDeduction;
    private String libelle;
    private BigDecimal baseMontant;
    private BigDecimal taux;
    private BigDecimal montant;
    private BigDecimal montantCouvert;
}
