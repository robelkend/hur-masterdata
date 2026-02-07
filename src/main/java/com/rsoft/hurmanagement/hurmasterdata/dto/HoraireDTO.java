package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.Horaire;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class HoraireDTO {
    private Long id;
    private String codeHoraire;
    private String description;
    private String genererAbsence;
    private String payerSupplementaire;
    private String montantFixe;
    private BigDecimal montantHeureSup;
    private BigDecimal coeffJourFerie;
    private BigDecimal nbHeuresRef;
    private BigDecimal coeffDimanche;
    private BigDecimal coeffSuppJourFerie;
    private BigDecimal coeffSoir;
    private BigDecimal coeffSuppSoir;
    private BigDecimal coeffSuppOff;
    private Long deviseId;
    private String deviseCode;
    private String deviseDescription;
    private String alternerJourNuit;
    private Horaire.UniteAlternance uniteAlternance;
    private Integer nbUniteJour;
    private String heureDebutNuit;
    private String heureFinNuit;
    private String heureFermetureAutoJour;
    private String heureFermetureAutoNuit;
    private String heureDebut;
    private String heureFin;
    private String detailPresent;
    private Horaire.ShiftEncours shiftEncours;
    private Integer defaultNbHovertime;
    private String debutSupplementaire;
    private String minHeurePonctualite;
    private Integer nbMinutePonctualite;
    private Integer toleranceRetardMin;
    private Integer seuilDoublonMin;
    private Integer maxSessionHeures;
    private String exigerPlanNuit;
    private String planifierNuitAuto;
    private String heureFinDemiJournee;
    private List<HoraireDtDTO> horaireDts;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
