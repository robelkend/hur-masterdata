package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplementaireEmployeUpdateDTO {
    @NotNull(message = "Id is required")
    private Long id;

    @NotNull(message = "Employe is required")
    private Long employeId;

    private Long emploiEmployeId;

    @NotBlank(message = "Memo is required")
    private String memo;

    @NotNull(message = "Date jour is required")
    private LocalDate dateJour;

    @NotBlank(message = "Heure debut is required")
    private String heureDebut; // VARCHAR, displayed as time picker

    @NotBlank(message = "Heure fin is required")
    private String heureFin; // VARCHAR, displayed as time picker

    @NotNull(message = "Type supplementaire is required")
    private String typeSupplementaire;

    // Fields from details JSON (for form submission)
    private BigDecimal nbHeures;
    private BigDecimal nbJours;
    private BigDecimal nbNuits;
    private BigDecimal nbOffs;
    private BigDecimal nbConges;

    private String baseCalcul;
    private BigDecimal montantBase;

    private Long deviseId;

    private BigDecimal montantCalcule;

    private String automatique = "N";

    @NotNull(message = "Statut is required")
    private String statut;

    private Long entrepriseId;

    @NotNull(message = "Rowscn is required")
    private Integer rowscn;

    // details will be generated from nbHeures, nbJours, etc. in the service
}
