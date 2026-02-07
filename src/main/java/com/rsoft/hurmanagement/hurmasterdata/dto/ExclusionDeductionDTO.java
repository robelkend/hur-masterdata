package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ExclusionDeductionDTO {
    private Long id;
    private Long typeEmployeId;
    private String typeEmployeDescription;
    private Long definitionDeductionId;
    private String definitionDeductionCode;
    private String definitionDeductionLibelle;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
