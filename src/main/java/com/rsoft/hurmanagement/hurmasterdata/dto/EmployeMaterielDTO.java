package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class EmployeMaterielDTO {
    private Long id;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long materielId;
    private String materielCode;
    private String materielLibelle;
    private String numeroSerie;
    private LocalDate dateAttribution;
    private LocalDate dateFinPrevue;
    private BigDecimal valeurAttribution;
    private String statut;
    private LocalDate dateTransfertPropriete;
    private LocalDate dateRestitutionEffective;
    private BigDecimal valeurResiduelleCalculee;
    private String observations;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
