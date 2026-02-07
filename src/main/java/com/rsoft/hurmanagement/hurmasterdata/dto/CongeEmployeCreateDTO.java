package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongeEmployeCreateDTO {
    private Long entrepriseId;

    @NotNull(message = "Employe is required")
    private Long employeId;

    private Long emploiEmployeId;

    @NotNull(message = "Type conge is required")
    private Long typeCongeId;

    @NotNull(message = "Date debut plan is required")
    private LocalDate dateDebutPlan;

    @NotNull(message = "Date fin plan is required")
    private LocalDate dateFinPlan;

    private LocalDate dateDebutReel;
    private LocalDate dateFinReel;
    private String motif;
    private String reference;
    private String statut = "BROUILLON";
}
