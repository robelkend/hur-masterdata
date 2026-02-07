package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class BalanceCongeAnneeDTO {
    private Long id;
    private Long balanceCongeId;
    private Long emploiEmployeId;
    private Long typeCongeId;
    private String typeCongeCode;
    private String typeCongeDescription;
    private Integer annee;
    private BigDecimal joursAcquis;
    private BigDecimal joursPris;
    private String cumulAutorise;
    private BigDecimal plafondCumul;
    private BigDecimal joursReportes;
    private BigDecimal joursExpires;
    private BigDecimal soldeFinAnnee;
    private LocalDate dateCloture;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
