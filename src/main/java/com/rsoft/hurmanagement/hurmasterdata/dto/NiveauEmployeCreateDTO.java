package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NiveauEmployeCreateDTO {
    
    @NotBlank(message = "Code niveau is required")
    @Size(max = 50, message = "Code niveau must not exceed 50 characters")
    private String codeNiveau;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @NotNull(message = "Niveau hierarchique is required")
    @Min(value = 0, message = "Niveau hierarchique must be a positive integer or zero")
    private Integer niveauHierarchique;
}
