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
public class SupplementaireEmployeDTO {
    private Long id;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long emploiEmployeId;
    private String memo;
    private LocalDate dateJour;
    private String heureDebut; // VARCHAR, displayed as time picker
    private String heureFin; // VARCHAR, displayed as time picker
    private String typeSupplementaire;
    private String baseCalcul;
    private BigDecimal montantBase;
    private Long deviseId;
    private String deviseCode;
    private String deviseDescription;
    private BigDecimal montantCalcule;
    private String automatique;
    private String details; // JSON string containing nb_heures, nb_jours, nb_nuits, nb_offs, nb_conges, montant_*_calcule
    private String statut;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
