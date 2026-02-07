package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class RoleGroupeDTO {
    private Long id;
    private String codeGroupe;
    private String libelle;
    private String actif;
    private String allAccess;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
