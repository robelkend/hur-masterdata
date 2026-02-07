package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleGroupeCreateDTO {
    
    @NotBlank(message = "Code groupe is required")
    @Size(max = 100, message = "Code groupe must not exceed 100 characters")
    private String codeGroupe;
    
    @NotBlank(message = "Libelle is required")
    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;

    private String allAccess = "N";
}
