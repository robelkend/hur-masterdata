package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PointageBrutDTO {
    private Long id;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long entrepriseId;
    private String systemeSource;
    private String idPointageSource;
    private String idAppareil;
    private String idBadge;
    private OffsetDateTime dateHeurePointage;
    private String typeEvenement;
    private String qualitePointage;
    private String motifRejet;
    private String statutTraitement;
    private Long presenceEmployeId;
    private OffsetDateTime traiteLe;
    private String traitePar;
    private OffsetDateTime importeLe;
    private String importePar;
    private Integer rowscn;
}
