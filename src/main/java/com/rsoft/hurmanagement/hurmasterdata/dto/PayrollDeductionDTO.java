package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PayrollDeductionDTO {
    private Long id;
    private Long payrollId;
    private Long payrollEmployeId;
    private String codeDeduction;
    private String libelle;
    private String categorie;
    private BigDecimal baseMontant;
    private BigDecimal taux;
    private BigDecimal montant;
    private BigDecimal montantCouvert;
    private String referenceExterne;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
