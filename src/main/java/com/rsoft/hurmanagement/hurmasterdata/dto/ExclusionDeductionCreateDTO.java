package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExclusionDeductionCreateDTO {
    @NotNull
    private Long typeEmployeId;
    @NotNull
    private Long definitionDeductionId;
    private String actif;
}
