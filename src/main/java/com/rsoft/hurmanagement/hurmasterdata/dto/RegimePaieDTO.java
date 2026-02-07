package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaie;
import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class RegimePaieDTO {
    private Long id;
    private String codeRegimePaie;
    private String description;
    private RegimePaie.ModeRemuneration modeRemuneration;
    private RegimePaie.Periodicite periodicite;
    
    // Devise
    private Long deviseId;
    private String deviseCode;
    private String deviseDescription;
    
    private String horaireActif;
    private Integer joursPayes;
    private String suppAuto;
    private String bloquerNetNegatif;
    private Integer taxeChaqueNPaies;
    private Integer suppChaqueNPaies;
    private Integer suppDecalageNbPaies;
    private String autoTraitement;
    private RegimePaie.NiveauAutoTraitement niveauAutoTraitement;
    private String heuresMinJour;
    private String payerSiMoinsMin;
    private Integer retardsMaxJour;
    private String paiementSurCompte;
    private String taxeSurDernierNetPositif;
    private String taxable;
    
    // Responsable
    private Long responsableId;
    private String responsableCodeEmploye;
    private String responsableNom;
    private String responsablePrenom;
    
    // Dates
    private LocalDate dernierePaie;
    private LocalDate prochainePaie;
    private LocalDate dernierPrelevement;
    private LocalDate dernierSupplement;
    private LocalDate prochainSupplement;
    
    // Audit fields
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
