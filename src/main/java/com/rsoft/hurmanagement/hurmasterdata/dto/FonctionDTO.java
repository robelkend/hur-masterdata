package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class FonctionDTO {
    private Long id;
    private String codeFonction;
    private String description;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
