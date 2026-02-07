package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanctionEmployeUpdateDTO {
    @NotNull(message = "Id is required")
    private Long id;
    
    @NotNull(message = "Employe is required")
    private Long employeId;

    private Long emploiEmployeId;
    
    private LocalDate dateSanction;
    private String typeEvenement;
    private BigDecimal valeurMesuree;
    private String uniteMesure;
    private Long regleId;
    private String typeSanction;
    private BigDecimal valeurSanction;
    private String uniteSanction;
    private BigDecimal montantCalcule;
    @NotNull(message = "Statut is required")
    private String statut;
    private String motif;
    private String referenceExterne;
    private Long entrepriseId;
    @NotNull(message = "Rowscn is required")
    private Integer rowscn;
}
