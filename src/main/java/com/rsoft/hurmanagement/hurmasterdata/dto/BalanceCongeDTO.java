package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class BalanceCongeDTO {
    private Long id;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private Long emploiEmployeId;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long typeCongeId;
    private String typeCongeCode;
    private String typeCongeDescription;
    private BigDecimal soldeActuel;
    private BigDecimal soldeDisponible;
    private LocalDate derniereMiseAJour;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
