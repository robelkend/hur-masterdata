package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TypeRevenuDTO {
    private Long id;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private String codeRevenu;
    private String description;
    private Long rubriquePaieId;
    private String rubriquePaieCode;
    private String rubriquePaieLibelle;
    private String actif;
    private Long formuleId;
    private String formuleCodeVariable;
    private String formuleDescription;
    private String ajouterSalBase;
    private String hardcoded;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
