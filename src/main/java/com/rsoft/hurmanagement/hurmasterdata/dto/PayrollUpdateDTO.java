package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PayrollUpdateDTO {
    @NotNull
    private Long id;

    @NotNull
    private Integer rowscn;

    @NotNull(message = "Regime paie is required")
    private Long regimePaieId;

    @Size(max = 120, message = "Libelle must not exceed 120 characters")
    private String libelle;
}
