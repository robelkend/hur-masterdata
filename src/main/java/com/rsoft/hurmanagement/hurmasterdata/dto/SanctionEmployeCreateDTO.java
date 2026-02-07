package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanctionEmployeCreateDTO {
    @NotNull(message = "Employe is required")
    private Long employeId;

    private Long emploiEmployeId;
    
    private LocalDate dateSanction;
    private String typeEvenement; // RETARD, ABSENCE, AUTRE
    private BigDecimal valeurMesuree;
    private String uniteMesure; // MINUTE, HEURE, JOUR
    private Long regleId;
    private String typeSanction; // DEDUIRE_TEMPS, DEDUIRE_MONTANT, AVERTISSEMENT
    private BigDecimal valeurSanction;
    private String uniteSanction; // MINUTE, HEURE, JOUR, MONTANT
    private BigDecimal montantCalcule;
    private String statut = "NOUVEAU"; // NOUVEAU, VALIDE
    private String motif;
    private String referenceExterne;
    private Long entrepriseId;
}
