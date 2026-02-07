package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class RubriquePaieDeductionDTO {
    private Long id;
    private Long definitionDeductionId;
    private Long rubriquePaieId;
    private String rubriquePaieCode;
    private String rubriquePaieLibelle;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
