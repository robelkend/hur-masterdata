package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionLiaison;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceExtractionLiaisonDTO {
    private Long id;
    private Long requeteFilleId;
    private Integer paramPosition;
    private InterfaceExtractionLiaison.SourceType sourceType;
    private String sourceValeur;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
