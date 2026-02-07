package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PayrollEmployeStatsDTO {
    private Long id;
    private Long payrollId;
    private Long payrollEmployeId;
    private String metricCode;
    private String metricLabel;
    private String metricGroup;
    private String uniteMesure;
    private BigDecimal quantite;
    private BigDecimal montant;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
