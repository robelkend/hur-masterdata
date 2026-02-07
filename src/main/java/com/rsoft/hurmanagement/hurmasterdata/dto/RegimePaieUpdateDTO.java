package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaie;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegimePaieUpdateDTO {
    private Integer rowscn;
    private String description;
    private RegimePaie.ModeRemuneration modeRemuneration;
    private RegimePaie.Periodicite periodicite;
    private Long deviseId;
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
    private Long responsableId;
    private LocalDate dernierePaie;
    private LocalDate prochainePaie;
    private LocalDate dernierPrelevement;
    private LocalDate dernierSupplement;
    private LocalDate prochainSupplement;
}
