package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class PayrollDTO {
    private Long id;
    private Long regimePaieId;
    private String regimePaieCode;
    private String regimePaieLibelle;
    private String libelle;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
