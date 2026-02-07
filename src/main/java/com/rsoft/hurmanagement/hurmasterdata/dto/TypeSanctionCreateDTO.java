package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.TypeSanction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TypeSanctionCreateDTO {
    @NotBlank(message = "Code sanction is required")
    private String codeSanction;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Gravite is required")
    private TypeSanction.Gravite gravite;
    
    @NotNull(message = "Categorie is required")
    private TypeSanction.Categorie categorie;
}
