package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HoraireCloneDTO {
    @NotBlank(message = "Code horaire is required")
    @Size(max = 50, message = "Code horaire must not exceed 50 characters")
    private String codeHoraire;
}
