package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.TypeEmploye;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeEmployeUpdateDTO {
    
    @NotNull(message = "Row version (rowscn) is required for optimistic concurrency control")
    private Integer rowscn;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @Size(max = 5, message = "Pause debut must be in HH:mm format")
    private String pauseDebut;
    
    @Size(max = 5, message = "Pause fin must be in HH:mm format")
    private String pauseFin;
    
    private String payerAbsence;
    
    private String payerAbsenceMotivee;
    
    private Long deviseId;
    
    private BigDecimal salaireMinimum;
    
    private BigDecimal salaireMaximum;
    
    private Integer ajouterBonusApresNbMinutePresence;
    
    private BigDecimal pourcentageJourBonus;
    
    private String genererPrestation;
    
    private Integer baseCalculBoni;
    
    @NotBlank(message = "Supplementaire is required")
    private String supplementaire; // Required
    
    private TypeEmploye.BaseCalculSupplementaire baseCalculSupplementaire;
    
    private Integer calculerSupplementaireApres;
    
    private String probation;
    
    private TypeEmploye.StatutManagement statutManagement;
    
    private Long familleMetierId;
    
    private Long niveauEmployeId;
}
