package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class PayrollSanctionDTO {
    private Long id;
    private Long payrollId;
    private Long payrollEmployeId;
    private String typeSanction;
    private LocalDate dateJour;
    private BigDecimal quantiteMinute;
    private BigDecimal montant;
    private String referenceExterne;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
