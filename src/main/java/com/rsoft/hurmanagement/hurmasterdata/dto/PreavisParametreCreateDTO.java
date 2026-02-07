package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreavisParametreCreateDTO {
    private Long typeEmployeId;
    private Long regimePaieId;
    
    @NotNull(message = "Type depart is required")
    private String typeDepart; // DEMISSION, ABANDON, LICENCIEMENT, FIN_CONTRAT, RETRAITE
    
    @NotNull(message = "Anciennete min is required")
    @Min(value = 0, message = "Anciennete min must be >= 0")
    private Integer ancienneteMin;
    
    private Integer ancienneteMax;
    
    @NotNull(message = "Inclure max is required")
    private String inclureMax; // 'Y' or 'N'
    
    @NotNull(message = "Valeur preavis is required")
    @Min(value = 0, message = "Valeur preavis must be >= 0")
    private Integer valeurPreavis;
    
    @NotNull(message = "Unite preavis is required")
    private String unitePreavis; // JOUR, MOIS
    
    @NotNull(message = "Mode application is required")
    private String modeApplication; // A_EFFECTUER, A_PAYER
    
    @NotNull(message = "Priorite is required")
    @Min(value = 1, message = "Priorite must be >= 1")
    private Integer priorite;
    
    @NotNull(message = "Entreprise is required")
    private Long entrepriseId;
    
    @NotNull(message = "Actif is required")
    private String actif; // 'Y' or 'N'
}
