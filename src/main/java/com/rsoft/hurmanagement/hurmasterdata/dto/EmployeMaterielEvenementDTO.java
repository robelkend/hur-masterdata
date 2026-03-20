package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class EmployeMaterielEvenementDTO {
    private Long id;
    private Long employeMaterielId;
    private String typeEvenement;
    private LocalDate dateEvenement;
    private BigDecimal montant;
    private String commentaire;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
