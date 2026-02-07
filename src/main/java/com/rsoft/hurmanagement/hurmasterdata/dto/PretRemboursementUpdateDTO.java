package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PretRemboursementUpdateDTO {
    @NotNull(message = "Id is required")
    private Long id;

    @NotNull(message = "Rowscn is required")
    private Integer rowscn;

    @NotNull(message = "Date remboursement is required")
    private LocalDate dateRemboursement;

    @NotNull(message = "Montant rembourse is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Montant rembourse must be positive")
    @Digits(integer = 13, fraction = 2, message = "Montant rembourse must have at most 13 integer digits and 2 decimal places")
    private BigDecimal montantRembourse;

    @NotNull(message = "Montant interet is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Montant interet must be non-negative")
    @Digits(integer = 13, fraction = 2, message = "Montant interet must have at most 13 integer digits and 2 decimal places")
    private BigDecimal montantInteret;

    @NotNull(message = "Montant total is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Montant total must be positive")
    @Digits(integer = 13, fraction = 2, message = "Montant total must have at most 13 integer digits and 2 decimal places")
    private BigDecimal montantTotal;

    @NotNull(message = "Origine is required")
    private String origine; // PAIE, MANUEL, AJUSTEMENT

    @Min(value = 0, message = "No payroll must be non-negative")
    private Integer noPayroll;

    private String commentaire;
}
