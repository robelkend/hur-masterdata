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
public class PresenceEmployeCreateDTO {
    private Long entrepriseId;

    @NotNull(message = "Employe is required")
    private Long employeId;

    @NotNull(message = "Date jour is required")
    private LocalDate dateJour;

    private LocalDate dateDepart;

    @NotBlank(message = "Heure arrivee is required")
    private String heureArrivee;

    private String heureDepart;

    private Integer cumulPauseMin;

    private String commentaire;

    private String sourceSaisie = "MANUEL";
}
