package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceEmployeDTO {
    private Long id;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private LocalDate dateJour;
    private LocalDate dateDepart;
    private String heureArrivee;
    private String heureDepart;
    private String nuitPlanifiee;
    private String heureDebutPrevue;
    private String heureFinPrevue;
    private Long typeEmployeId;
    private Long regimePaieId;
    private Long idHoraire;
    private String horaireSpecial;
    private String automatique;
    private String genererSupplementaire;
    private String supplementaireGenere;
    private String genererBoni;
    private String boniGenere;
    private String sourceSaisie;
    private String statutPresence;
    private BigDecimal nbHeuresSup;
    private Integer cumulPauseMin;
    private Integer noSupplementaire;
    private String fermetureManuelle;
    private String commentaire;
    private String typeErreur;
    private String details;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
