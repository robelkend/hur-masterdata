package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstitutionTierseUpdateDTO {
    @NotNull(message = "Rowscn is required for concurrency control")
    private Integer rowscn;
    
    @NotBlank(message = "Nom is required")
    @Size(max = 255, message = "Nom must not exceed 255 characters")
    private String nom;
    
    @Size(max = 255, message = "Reference must not exceed 255 characters")
    private String reference;
}
