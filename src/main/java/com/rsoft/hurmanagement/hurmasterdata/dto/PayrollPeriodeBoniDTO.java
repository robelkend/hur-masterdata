package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollPeriodeBoni;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class PayrollPeriodeBoniDTO {
    private Long id;
    private String code;
    private String libelle;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private PayrollPeriodeBoni.Statut statut;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
