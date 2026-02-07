package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoadingChamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceLoadingChampCreateDTO {
    @NotNull(message = "Loading ID is required")
    private Long loadingId;
    
    @NotBlank(message = "Nom cible is required")
    private String nomCible;
    
    private String nomSource;
    
    @NotNull(message = "Type donnée is required")
    private InterfaceLoadingChamp.TypeDonnee typeDonnee;
    
    private Integer taille;
    private String format;
    
    @NotNull(message = "Position is required")
    private Integer position;
    
    private String valeur;
    private String updateChamp;
    private String updateValeur;
    private String updateCondition;
    private String obligatoire = "N";
}
