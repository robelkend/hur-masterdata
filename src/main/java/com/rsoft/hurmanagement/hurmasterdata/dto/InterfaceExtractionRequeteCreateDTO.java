package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionRequete;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceExtractionRequeteCreateDTO {
    private String scriptSql;
    private Long parentId;
    private Integer ordreExecution;
    private InterfaceExtractionRequete.TypeRequete typeRequete;
    private String actif;
    private List<InterfaceExtractionParamCreateDTO> params;
    private List<InterfaceExtractionLiaisonCreateDTO> liaisons;
}
