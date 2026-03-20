package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoniDeleteRequestDTO {
    @NotNull
    private Long periodeBoniId;

    @NotNull
    private Long rubriquePaieId;

    private Long entrepriseId;

    private Long regimePaieId;

    private Long employeId;
}
