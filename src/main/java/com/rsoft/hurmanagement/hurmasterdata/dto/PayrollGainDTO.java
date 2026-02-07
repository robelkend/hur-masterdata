package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PayrollGainDTO {
    private Long id;
    private Long payrollId;
    private Long payrollEmployeId;
    private Long rubriquePaieId;
    private String rubriquePaieCode;
    private String rubriquePaieLibelle;
    private BigDecimal montant;
    private String imposable;
    private String soumisCotisations;
    private String source;
    private String referenceExterne;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
