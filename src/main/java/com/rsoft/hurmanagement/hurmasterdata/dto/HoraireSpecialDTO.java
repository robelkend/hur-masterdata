package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoraireSpecialDTO {
    private Long id;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long emploiEmployeId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String heureDebut;
    private String heureFin;
    private String priorite;
    private String frequence;
    private Integer uniteFreq;
    private String actif;
    private String duplique;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
