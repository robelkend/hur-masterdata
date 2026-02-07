package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilleMetierDTO {
    private Long id;
    private String codeFamilleMetier;
    private String libelle;
    private String description;
    private Long domaineId;
    private String domaineNom;
    private Long niveauQualificationId;
    private String niveauQualificationNom; // Description from NiveauEmploye
    private Integer niveauQualificationNiveau; // niveauHierarchique from NiveauEmploye
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
