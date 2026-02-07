package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PayrollRecouvrementDTO {
    private Long id;
    private Long payrollId;
    private Long payrollEmployeId;
    private String libelle;
    private String typeRecouvrement;
    private String referenceNo;
    private BigDecimal montantPeriode;
    private BigDecimal montantInteret;
    private BigDecimal montantTotal;
    private BigDecimal soldeAvant;
    private BigDecimal soldeApres;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
