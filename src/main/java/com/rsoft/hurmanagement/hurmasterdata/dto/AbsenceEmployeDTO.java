package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class AbsenceEmployeDTO {
    private Long id;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long emploiEmployeId;
    private String typeEvenement;
    private LocalDate dateJour;
    private String heureDebut;
    private String heureFin;
    private String uniteMesure;
    private BigDecimal quantite;
    private Long deviseId;
    private String deviseCode;
    private String deviseDescription;
    private BigDecimal montantEquivalent;
    private Long payrollId;
    private String justificatif;
    private String motif;
    private String statut;
    private String source;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
