package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstitutionTierseCreateDTO {
    @NotBlank(message = "Code institution is required")
    @Size(max = 50, message = "Code institution must not exceed 50 characters")
    private String codeInstitution;
    
    @NotBlank(message = "Nom is required")
    @Size(max = 255, message = "Nom must not exceed 255 characters")
    private String nom;
    
    @Size(max = 255, message = "Reference must not exceed 255 characters")
    private String reference;
}
