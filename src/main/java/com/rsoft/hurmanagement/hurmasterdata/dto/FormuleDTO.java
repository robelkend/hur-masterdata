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
public class FormuleDTO {
    private Long id;
    private String codeVariable;
    private BigDecimal valeurDefaut;
    private String actif;
    private LocalDate dateEffectif;
    private LocalDate dateFin;
    private String description;
    private String expression;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
