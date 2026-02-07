package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceExtractionParamDTO {
    private Long id;
    private Long requeteId;
    private String nomParam;
    private InterfaceExtractionParam.TypeParam typeParam;
    private Integer position;
    private String obligatoire;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
