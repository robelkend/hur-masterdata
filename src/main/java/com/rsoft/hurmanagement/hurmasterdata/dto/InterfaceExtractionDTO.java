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
public class InterfaceExtractionDTO {
    private Long id;
    private String codeExtraction;
    private String description;
    private String separateur;
    private String encadreur;
    private String actif;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private List<InterfaceExtractionRequeteDTO> requetes;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
