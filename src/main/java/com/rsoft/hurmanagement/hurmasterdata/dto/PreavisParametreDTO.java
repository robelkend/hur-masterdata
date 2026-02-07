package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreavisParametreDTO {
    private Long id;
    private Long typeEmployeId;
    private String typeEmployeDescription;
    private Long regimePaieId;
    private String regimePaieCode;
    private String regimePaieDescription;
    private String typeDepart;
    private Integer ancienneteMin;
    private Integer ancienneteMax;
    private String inclureMax;
    private Integer valeurPreavis;
    private String unitePreavis;
    private String modeApplication;
    private Integer priorite;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
