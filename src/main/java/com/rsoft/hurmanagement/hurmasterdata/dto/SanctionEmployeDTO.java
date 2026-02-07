package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanctionEmployeDTO {
    private Long id;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long emploiEmployeId;
    private LocalDate dateSanction;
    private String typeEvenement;
    private BigDecimal valeurMesuree;
    private String uniteMesure;
    private Long regleId;
    private String regleDescription;
    private String typeSanction;
    private BigDecimal valeurSanction;
    private String uniteSanction;
    private BigDecimal montantCalcule;
    private String statut;
    private String motif;
    private String referenceExterne;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
