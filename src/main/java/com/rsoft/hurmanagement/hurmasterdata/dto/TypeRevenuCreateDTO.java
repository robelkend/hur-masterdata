package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TypeRevenuCreateDTO {
    private Long entrepriseId;
    
    @NotBlank(message = "Code revenu is required")
    @Size(max = 50, message = "Code revenu must not exceed 50 characters")
    private String codeRevenu;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @NotNull(message = "Rubrique paie is required")
    private Long rubriquePaieId;
    
    @NotBlank(message = "Actif is required")
    @Size(max = 1, message = "Actif must be 'Y' or 'N'")
    private String actif = "Y";
    
    private Long formuleId;
    
    @NotBlank(message = "Ajouter sal base is required")
    @Size(max = 1, message = "Ajouter sal base must be 'Y' or 'N'")
    private String ajouterSalBase = "N";

    private String hardcoded; // 'Y' or 'N', default 'N' if null
}
