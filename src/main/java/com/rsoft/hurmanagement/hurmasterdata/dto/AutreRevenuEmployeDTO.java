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
public class AutreRevenuEmployeDTO {
    private Long id;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long typeRevenuId;
    private String typeRevenuCode;
    private String typeRevenuDescription;
    private LocalDate dateRevenu;
    private LocalDate dateEffet;
    private Long deviseId;
    private String deviseCode;
    private String deviseDescription;
    private BigDecimal montant;
    private String commentaire;
    private String modeInclusion;
    private Long regimePaieId;
    private String regimePaieCode;
    private String regimePaieDescription;
    private LocalDate dateInclusion;
    private String reference;
    private String statut;
    private Integer payrollNo;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
