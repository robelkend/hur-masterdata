package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.TypeEmploye;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeEmployeCreateDTO {
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @Size(max = 5, message = "Pause debut must be in HH:mm format")
    private String pauseDebut;
    
    @Size(max = 5, message = "Pause fin must be in HH:mm format")
    private String pauseFin;
    
    private String payerAbsence; // 'Y' or 'N', default 'Y' if null
    
    private String payerAbsenceMotivee; // 'Y' or 'N', default 'Y' if null
    
    private Long deviseId;
    
    private BigDecimal salaireMinimum; // default 0 if null
    
    private BigDecimal salaireMaximum; // default 0 if null
    
    private Integer ajouterBonusApresNbMinutePresence;
    
    private BigDecimal pourcentageJourBonus;
    
    private String genererPrestation; // 'Y' or 'N', default 'Y' if null
    
    private Integer baseCalculBoni; // 1-12
    
    private String supplementaire; // 'Y' or 'N', default 'Y', required
    
    private TypeEmploye.BaseCalculSupplementaire baseCalculSupplementaire;
    
    private Integer calculerSupplementaireApres;
    
    private String probation; // 'Y' or 'N', default 'Y' if null
    
    private TypeEmploye.StatutManagement statutManagement; // default NON_MANAGER if null
    
    private Long familleMetierId;
    
    private Long niveauEmployeId;
}
