package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExclusionDeductionUpdateDTO {
    @NotNull
    private Long id;
    @NotNull
    private Long typeEmployeId;
    @NotNull
    private Long definitionDeductionId;
    private String actif;
    @NotNull
    private Integer rowscn;
}
