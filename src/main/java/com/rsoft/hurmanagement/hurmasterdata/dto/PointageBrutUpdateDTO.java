package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PointageBrutUpdateDTO {
    @NotNull(message = "Row version is required")
    private Integer rowscn;
    private Long employeId;
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
    private Long noPresence;
    private OffsetDateTime traiteLe;
    private String traitePar;
    private String importePar;
}
