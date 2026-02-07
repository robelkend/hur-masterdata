package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePrestation;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class RubriquePrestationDTO {
    private Long id;
    private String codePrestation;
    private String description;
    private RubriquePrestation.Prelevement prelevement;
    private String hardcoded;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
