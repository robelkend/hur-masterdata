package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefMaterielCreateDTO {
    @NotBlank
    private String codeMateriel;

    @NotBlank
    private String libelle;

    private String categorie;
    private String depreciable;
    private Integer dureeDepreciationMois;
    private Integer dureeTransfertProprieteMois;
    private BigDecimal valeurReference;
    private String actif;
}
