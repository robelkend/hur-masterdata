package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TypeUniteOrganisationnelleDTO {
    private Long id;
    private String code;
    private String libelle;
    private Integer niveauHierarchique;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
