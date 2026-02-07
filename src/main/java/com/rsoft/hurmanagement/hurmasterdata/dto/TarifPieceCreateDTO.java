package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifPieceCreateDTO {
    @NotNull(message = "Type piece is required")
    private Long typePieceId;

    @NotNull(message = "Devise is required")
    private Long deviseId;

    @NotNull(message = "Prix unitaire is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Prix unitaire must be positive")
    @Digits(integer = 13, fraction = 2, message = "Prix unitaire must have at most 13 integer digits and 2 decimal places")
    private BigDecimal prixUnitaire;

    @NotNull(message = "Date effectif is required")
    private LocalDate dateEffectif;

    private LocalDate dateFin;

    private String actif; // 'Y' or 'N'
}
