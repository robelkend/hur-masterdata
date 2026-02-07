package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreavisParametreUpdateDTO {
    @NotNull(message = "Id is required")
    private Long id;
    
    private Long typeEmployeId;
    private Long regimePaieId;
    
    @NotNull(message = "Type depart is required")
    private String typeDepart;
    
    @NotNull(message = "Anciennete min is required")
    @Min(value = 0, message = "Anciennete min must be >= 0")
    private Integer ancienneteMin;
    
    private Integer ancienneteMax;
    
    @NotNull(message = "Inclure max is required")
    private String inclureMax;
    
    @NotNull(message = "Valeur preavis is required")
    @Min(value = 0, message = "Valeur preavis must be >= 0")
    private Integer valeurPreavis;
    
    @NotNull(message = "Unite preavis is required")
    private String unitePreavis;
    
    @NotNull(message = "Mode application is required")
    private String modeApplication;
    
    @NotNull(message = "Priorite is required")
    @Min(value = 1, message = "Priorite must be >= 1")
    private Integer priorite;
    
    @NotNull(message = "Entreprise is required")
    private Long entrepriseId;
    
    @NotNull(message = "Actif is required")
    private String actif;
    
    @NotNull(message = "Rowscn is required")
    private Integer rowscn;
}
