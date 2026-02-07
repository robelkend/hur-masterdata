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
public class PretRemboursementDTO {
    private Long id;
    private Long pretEmployeId;
    private LocalDate dateRemboursement;
    private BigDecimal montantRembourse;
    private BigDecimal montantInteret;
    private BigDecimal montantTotal;
    private String origine; // PAIE, MANUEL, AJUSTEMENT
    private Integer noPayroll;
    private String statut;
    private String commentaire;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
