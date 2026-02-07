package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.TypeEmploye;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeEmployeDTO {
    private Long id;
    private String description;
    private String pauseDebut;
    private String pauseFin;
    private String payerAbsence;
    private String payerAbsenceMotivee;
    private Long deviseId;
    private String deviseCode;
    private String deviseDescription;
    private BigDecimal salaireMinimum;
    private BigDecimal salaireMaximum;
    private Integer ajouterBonusApresNbMinutePresence;
    private BigDecimal pourcentageJourBonus;
    private String genererPrestation;
    private Integer baseCalculBoni;
    private String supplementaire;
    private TypeEmploye.BaseCalculSupplementaire baseCalculSupplementaire;
    private Integer calculerSupplementaireApres;
    private String probation;
    private TypeEmploye.StatutManagement statutManagement;
    private Long familleMetierId;
    private String familleMetierCode;
    private String familleMetierLibelle;
    private Long niveauEmployeId;
    private String niveauEmployeCode;
    private String niveauEmployeDescription;
    private Integer niveauEmployeNiveauHierarchique;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
