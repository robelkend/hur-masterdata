package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RubriquePaieDeductionCreateDTO {
    @NotNull(message = "Definition deduction is required")
    private Long definitionDeductionId;
    
    @NotNull(message = "Rubrique paie is required")
    private Long rubriquePaieId;
}
