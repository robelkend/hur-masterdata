package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilleMetierCreateDTO {
    
    @NotBlank(message = "Code famille metier is required")
    @Size(max = 50, message = "Code famille metier must not exceed 50 characters")
    private String codeFamilleMetier;
    
    @NotBlank(message = "Libelle is required")
    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;
    
    private String description;
    
    private Long domaineId;
    
    private Long niveauQualificationId;
}
