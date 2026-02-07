package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionLiaison;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceExtractionLiaisonCreateDTO {
    private Integer paramPosition;
    private InterfaceExtractionLiaison.SourceType sourceType;
    private String sourceValeur;
}
