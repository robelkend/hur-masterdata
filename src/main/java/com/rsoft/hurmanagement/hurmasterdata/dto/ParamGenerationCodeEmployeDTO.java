package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParamGenerationCodeEmployeDTO {
    private Long id;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private Long typeEmployeId;
    private String typeEmployeDescription;
    private LocalDate dateEffectif;
    private LocalDate dateFin;
    private String modeGeneration;
    private Integer valeurDepart;
    private Integer valeurCourante;
    private Integer pasIncrementation;
    private Integer longueurMin;
    private String paddingChar;
    private String prefixeFixe;
    private String suffixeFixe;
    private String pattern;
    private String majuscules;
    private String enleverAccents;
    private String options; // JSON string
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
