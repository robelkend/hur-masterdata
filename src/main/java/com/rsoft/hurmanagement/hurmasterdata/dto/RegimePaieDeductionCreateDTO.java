package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

@Data
public class RegimePaieDeductionCreateDTO {
    private Long regimePaieId;
    private Long deductionCodeId;
    private String exclusif;
}
