package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeMaterielCreateDTO {
    @NotNull
    private Long employeId;

    @NotNull
    private Long materielId;

    private String numeroSerie;

    @NotNull
    private LocalDate dateAttribution;

    private LocalDate dateFinPrevue;
    private BigDecimal valeurAttribution;
    private String statut;
    private LocalDate dateTransfertPropriete;
    private LocalDate dateRestitutionEffective;
    private BigDecimal valeurResiduelleCalculee;
    private String observations;
}
