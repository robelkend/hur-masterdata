package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplementaireGenerationRequestDTO {
    @NotBlank(message = "Date debut is required")
    private String dateDebut; // ISO yyyy-MM-dd

    @NotBlank(message = "Date fin is required")
    private String dateFin; // ISO yyyy-MM-dd

    private Long entrepriseId;
    private Long employeId;
}
