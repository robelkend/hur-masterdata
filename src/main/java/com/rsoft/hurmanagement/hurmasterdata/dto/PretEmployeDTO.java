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
public class PretEmployeDTO {
    private Long id;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private LocalDate datePret;
    private Long deviseId;
    private String deviseCode;
    private String deviseDescription;
    private BigDecimal montantPret;
    private BigDecimal montantSubvention;
    private String periodicite;
    private String preleverDansPayroll;
    private String prelevementPartiel;
    private Integer nbPrevu;
    private BigDecimal montantPeriode;
    private BigDecimal montantVerse;
    private Integer frequenceNbPeriodicites;
    private Integer frequenceCompteur;
    private LocalDate premierPrelevement;
    private LocalDate dernierPrelevement;
    private String typeInteret;
    private BigDecimal tauxInteret;
    private String avance;
    private String libelle;
    private String note;
    private Integer ordre;
    private Long regimePaieId;
    private String regimePaieCode;
    private String regimePaieDescription;
    private Long typeRevenuId;
    private String typeRevenuCode;
    private String typeRevenuDescription;
    private String statut;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
    
    // Calculated field
    private BigDecimal montantRestant; // montantPret - montantVerse
}
