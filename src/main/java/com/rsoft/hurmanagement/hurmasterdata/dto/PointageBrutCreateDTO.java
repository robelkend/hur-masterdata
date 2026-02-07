package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PointageBrutCreateDTO {
    private Long employeId;
    private String systemeSource;
    private String idPointageSource;
    private String idAppareil;
    private String idBadge;

    @NotNull(message = "Date heure pointage is required")
    private OffsetDateTime dateHeurePointage;

    private String typeEvenement;
    private String qualitePointage;
    private String motifRejet;
    private String statutTraitement;
    private Long presenceEmployeId;
    private OffsetDateTime traiteLe;
    private String traitePar;
    private String importePar;
}
