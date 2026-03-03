package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PointageBrutUsageDTO {
    private String systemeSource;
    private String idAppareil;
    private String idBadge;
    private OffsetDateTime dateHeurePointage;
    private String qualitePointage;
    private String statutTraitement;
}
