package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PayrollEmployeAgregatDTO {
    private Long id;
    private Long regimePaieId;
    private String regimePaieCode;
    private String regimePaieLibelle;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long periodeBoniId;
    private String periodeBoniCode;
    private BigDecimal montantSalaireBase;
    private BigDecimal montantSupplementaire;
    private BigDecimal montantAutreRevenu;
    private BigDecimal montantSanctions;
    private BigDecimal montantDeductions;
    private BigDecimal montantDeductionsCouvert;
    private Integer nbPaie;
    private Integer noPeriode;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
