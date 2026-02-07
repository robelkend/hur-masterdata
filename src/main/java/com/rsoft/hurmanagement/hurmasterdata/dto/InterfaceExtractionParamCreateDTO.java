package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceExtractionParamCreateDTO {
    private String nomParam;
    private InterfaceExtractionParam.TypeParam typeParam;
    private Integer position;
    private String obligatoire;
    private String actif;
}
