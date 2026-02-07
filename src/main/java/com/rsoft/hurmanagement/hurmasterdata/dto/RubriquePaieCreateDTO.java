package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePaie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RubriquePaieCreateDTO {
    @NotBlank(message = "Code rubrique is required")
    @Size(max = 50, message = "Code rubrique must not exceed 50 characters")
    private String codeRubrique;
    
    @NotBlank(message = "Libelle is required")
    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;
    
    @NotNull(message = "Type rubrique is required")
    private RubriquePaie.TypeRubrique typeRubrique;
    
    @NotNull(message = "Mode calcul is required")
    private RubriquePaie.ModeCalcul modeCalcul;
    
    private String boni; // 'Y' or 'N', default 'Y' if null
    
    private String prestation; // 'Y' or 'N', default 'Y' if null
    
    private String imposable; // 'Y' or 'N', default 'Y' if null

    private String preavis; // 'Y' or 'N', default 'N' if null

    private String taxesSpeciaux; // 'Y' or 'N', default 'N' if null

    private String soumisCotisations; // 'Y' or 'N', default 'N' if null

    private String hardcoded; // 'Y' or 'N', default 'N' if null
}
