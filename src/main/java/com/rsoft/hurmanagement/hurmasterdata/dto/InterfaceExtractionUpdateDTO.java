package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceExtractionUpdateDTO {
    private Long id;
    private String codeExtraction;
    private String description;
    private String separateur;
    private String encadreur;
    private String actif;
    private Long entrepriseId;
    private List<InterfaceExtractionRequeteUpdateDTO> requetes;
    private Integer rowscn;
}
