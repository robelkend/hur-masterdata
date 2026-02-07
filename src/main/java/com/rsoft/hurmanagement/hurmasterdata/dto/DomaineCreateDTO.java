package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomaineCreateDTO {
    
    @NotBlank(message = "Nom is required")
    @Size(max = 255, message = "Nom must not exceed 255 characters")
    private String nom;
}
