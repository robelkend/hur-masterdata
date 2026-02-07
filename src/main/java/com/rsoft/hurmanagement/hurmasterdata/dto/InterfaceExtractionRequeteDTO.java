package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionRequete;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceExtractionRequeteDTO {
    private Long id;
    private Long interfaceExtractionId;
    private String scriptSql;
    private Long parentId;
    private Integer ordreExecution;
    private InterfaceExtractionRequete.TypeRequete typeRequete;
    private String actif;
    private List<InterfaceExtractionParamDTO> params;
    private List<InterfaceExtractionLiaisonDTO> liaisons;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
