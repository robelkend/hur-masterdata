package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AbsenceGenerationRequestDTO {
    @NotBlank
    private String dateDebut; // ISO yyyy-MM-dd
    @NotBlank
    private String dateFin; // ISO yyyy-MM-dd
    private Long entrepriseId;
    private Long employeId;
}
