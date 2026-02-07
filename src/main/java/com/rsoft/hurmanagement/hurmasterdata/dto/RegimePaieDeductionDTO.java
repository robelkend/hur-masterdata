package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class RegimePaieDeductionDTO {
    private Long id;
    private Long regimePaieId;
    private Long deductionCodeId;
    private String deductionCodeCode;
    private String deductionCodeLibelle;
    private String exclusif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
