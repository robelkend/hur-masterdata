package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrestationDepartDetailDTO {
    private Long id;
    private String rubriquePrestation;
    private String libelle;
    private String categorie;
    private BigDecimal montantBase;
    private BigDecimal taux;
    private BigDecimal montant;
    private Integer ordreAffichage;
}
