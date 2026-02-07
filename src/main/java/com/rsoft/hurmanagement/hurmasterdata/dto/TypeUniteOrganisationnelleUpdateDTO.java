package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TypeUniteOrganisationnelleUpdateDTO {
    @NotNull(message = "Rowscn is required for concurrency control")
    private Integer rowscn;
    
    @NotBlank(message = "Libelle is required")
    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;
    
    @NotNull(message = "Niveau hierarchique is required")
    @Min(value = 0, message = "Niveau hierarchique must be greater than or equal to 0")
    private Integer niveauHierarchique;
}
