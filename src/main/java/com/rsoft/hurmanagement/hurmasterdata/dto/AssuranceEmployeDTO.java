package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class AssuranceEmployeDTO {
    private Long id;
    private Long employeId;
    private Long planAssuranceId;
    private String planAssuranceCode;
    private String planAssuranceDescription;
    private String noAssurance;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
