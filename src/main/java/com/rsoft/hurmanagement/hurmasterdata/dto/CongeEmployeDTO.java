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
public class CongeEmployeDTO {
    private Long id;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long emploiEmployeId;
    private Long typeCongeId;
    private String typeCongeCode;
    private String typeCongeDescription;
    private LocalDate dateDebutPlan;
    private LocalDate dateFinPlan;
    private LocalDate dateDebutReel;
    private LocalDate dateFinReel;
    private String motif;
    private String reference;
    private String approbateur;
    private OffsetDateTime dateDecision;
    private String commentaireDecision;
    private BigDecimal nbJoursPlan;
    private BigDecimal nbJoursReel;
    private String statut;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
