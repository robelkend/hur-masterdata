package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.TypeConge;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TypeCongeDTO {
    private Long id;
    private String codeConge;
    private String description;
    private TypeConge.CongeAnnuel congeAnnuel;
    private Integer nbJours;
    private Integer nbAnneeCumul;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
