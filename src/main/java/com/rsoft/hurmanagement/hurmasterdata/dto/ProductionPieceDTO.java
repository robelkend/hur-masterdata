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
public class ProductionPieceDTO {
    private Long id;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private LocalDate dateJour;
    private Long typePieceId;
    private String typePieceCode;
    private String typePieceLibelle;
    private BigDecimal quantite;
    private BigDecimal quantiteRejet;
    private BigDecimal quantiteValide;
    private Long deviseId;
    private String deviseCode;
    private String deviseDescription;
    private BigDecimal prixUnitaire;
    private BigDecimal montantTotal;
    private Long payrollId;
    private Long emploiEmployeId;
    private Long employeSalaireId;
    private String statut;
    private String note;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
