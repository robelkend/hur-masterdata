package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.Horaire;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class HoraireCreateDTO {
    @NotBlank(message = "Code horaire is required")
    @Size(max = 50, message = "Code horaire must not exceed 50 characters")
    private String codeHoraire;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    private String genererAbsence; // 'Y' or 'N', default 'Y' if null
    private String payerSupplementaire; // 'Y' or 'N', default 'Y' if null
    private String montantFixe; // 'Y' or 'N', default 'N' if null
    private BigDecimal montantHeureSup; // default 0 if null
    private BigDecimal coeffJourFerie; // default 0 if null
    private BigDecimal nbHeuresRef; // default 0 if null
    private BigDecimal coeffDimanche; // default 0 if null
    private BigDecimal coeffSuppJourFerie; // default 0 if null
    private BigDecimal coeffSoir; // default 0 if null
    private BigDecimal coeffSuppSoir; // default 0 if null
    private BigDecimal coeffSuppOff; // default 0 if null
    
    @NotNull(message = "Devise is required")
    private Long deviseId;
    
    private String alternerJourNuit; // 'Y' or 'N', default 'N' if null
    private Horaire.UniteAlternance uniteAlternance;
    private Integer nbUniteJour; // default 0 if null
    private String heureDebutNuit; // format HH:mi
    private String heureFinNuit; // format HH:mi
    private String heureFermetureAutoJour; // format HH:mi
    private String heureFermetureAutoNuit; // format HH:mi
    private String heureDebut; // format HH:mi
    private String heureFin; // format HH:mi
    private String detailPresent; // 'Y' or 'N', default 'Y' if null
    private Horaire.ShiftEncours shiftEncours;
    private Integer defaultNbHovertime;
    private String debutSupplementaire; // format HH:mi
    private String minHeurePonctualite; // format HH:mi
    private Integer nbMinutePonctualite;
    private Integer toleranceRetardMin; // default 5 if null
    private Integer seuilDoublonMin; // default 2 if null
    private Integer maxSessionHeures; // default 16 if null
    private String exigerPlanNuit; // 'Y' or 'N', default 'Y' if null
    private String planifierNuitAuto; // 'Y' or 'N', default 'Y' if null
    private String heureFinDemiJournee; // format HH:mi
    
    private List<HoraireDtCreateDTO> horaireDts;
}
