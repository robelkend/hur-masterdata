package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollPeriodeBoni;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PayrollPeriodeBoniCreateDTO {
    private String code;

    @NotBlank(message = "Libelle is required")
    private String libelle;

    @NotNull(message = "Date debut is required")
    private LocalDate dateDebut;

    @NotNull(message = "Date fin is required")
    private LocalDate dateFin;

    @NotNull(message = "Statut is required")
    private PayrollPeriodeBoni.Statut statut;
}
