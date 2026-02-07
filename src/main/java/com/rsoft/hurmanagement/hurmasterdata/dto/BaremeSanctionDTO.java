package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.BaremeSanction;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class BaremeSanctionDTO {
    private Long id;
    private Long typeEmployeId;
    private BaremeSanction.InfractionType infractionType;
    private BaremeSanction.UniteInfraction uniteInfraction;
    private Integer seuilMin;
    private Integer seuilMax;
    private Integer penaliteMinutes;
    private BaremeSanction.UnitePenalite unitePenalite;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
