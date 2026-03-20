package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class RefMaterielDTO {
    private Long id;
    private String codeMateriel;
    private String libelle;
    private String categorie;
    private String depreciable;
    private Integer dureeDepreciationMois;
    private Integer dureeTransfertProprieteMois;
    private BigDecimal valeurReference;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
