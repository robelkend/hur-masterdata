package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupeRoleUtilisateurCreateDTO {
    @NotNull(message = "Groupe is required")
    private Long groupeId;

    private String estPrimaire = "N";
}
