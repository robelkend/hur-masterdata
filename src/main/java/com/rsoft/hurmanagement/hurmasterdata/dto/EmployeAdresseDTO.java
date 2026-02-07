package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class EmployeAdresseDTO {
    private Long id;
    private Long employeId;
    private String typeAdresse;
    private String ligne1;
    private String ligne2;
    private String ville;
    private String etat;
    private String codePostal;
    private String pays;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
