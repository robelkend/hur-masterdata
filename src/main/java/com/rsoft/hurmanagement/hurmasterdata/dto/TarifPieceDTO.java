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
public class TarifPieceDTO {
    private Long id;
    private Long typePieceId;
    private String typePieceCode;
    private String typePieceLibelle;
    private Long deviseId;
    private String deviseCode;
    private String deviseDescription;
    private BigDecimal prixUnitaire;
    private LocalDate dateEffectif;
    private LocalDate dateFin;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
