package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class EmployeSalaireDTO {
    private Long id;
    private Long employeId;
    private Long emploiId;
    private Long regimePaieId;
    private String regimePaieCode;
    private String regimePaieDescription;
    private BigDecimal montant;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String principal;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
