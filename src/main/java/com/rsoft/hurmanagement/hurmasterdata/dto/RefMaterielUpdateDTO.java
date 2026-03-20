package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefMaterielUpdateDTO {
    @NotNull
    private Long id;

    @NotNull
    private Integer rowscn;

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
