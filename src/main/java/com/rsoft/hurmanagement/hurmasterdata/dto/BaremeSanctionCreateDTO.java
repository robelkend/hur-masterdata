package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.BaremeSanction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BaremeSanctionCreateDTO {
    private Long typeEmployeId;
    
    @NotNull
    private BaremeSanction.InfractionType infractionType;
    
    @NotNull
    private BaremeSanction.UniteInfraction uniteInfraction;
    
    @NotNull
    @Min(0)
    private Integer seuilMin;
    
    @Min(0)
    private Integer seuilMax;
    
    @NotNull
    @Min(0)
    private Integer penaliteMinutes;
    
    @NotNull
    private BaremeSanction.UnitePenalite unitePenalite;
}
