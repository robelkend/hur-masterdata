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
public class PretEmployeCreateDTO {
    private Long entrepriseId;

    @NotNull(message = "Employe is required")
    private Long employeId;

    @NotNull(message = "Date pret is required")
    private LocalDate datePret;

    @NotNull(message = "Devise is required")
    private Long deviseId;

    @NotNull(message = "Montant pret is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Montant pret must be positive")
    @Digits(integer = 13, fraction = 2, message = "Montant pret must have at most 13 integer digits and 2 decimal places")
    private BigDecimal montantPret;

    @NotNull(message = "Montant subvention is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Montant subvention must be non-negative")
    @Digits(integer = 13, fraction = 2, message = "Montant subvention must have at most 13 integer digits and 2 decimal places")
    private BigDecimal montantSubvention = BigDecimal.ZERO;

    @NotNull(message = "Periodicite is required")
    private String periodicite; // PAIE, JOURNALIER, HEBDO, QUINZAINE, QUINZOMADAIRE, TRIMESTRIEL, SEMESTRIEL, ANNUEL

    @Pattern(regexp = "^[YN]$", message = "Prelever dans payroll must be 'Y' or 'N'")
    private String preleverDansPayroll = "Y";

    @Pattern(regexp = "^[YN]$", message = "Prelevement partiel must be 'Y' or 'N'")
    private String prelevementPartiel = "N";

    @NotNull(message = "Nb prevu is required")
    @Min(value = 1, message = "Nb prevu must be at least 1")
    private Integer nbPrevu = 1;

    @NotNull(message = "Montant periode is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Montant periode must be positive")
    @Digits(integer = 13, fraction = 2, message = "Montant periode must have at most 13 integer digits and 2 decimal places")
    private BigDecimal montantPeriode;

    @Min(value = 1, message = "Frequence nb periodicites must be at least 1")
    private Integer frequenceNbPeriodicites = 1;

    private LocalDate premierPrelevement; // NULL = au prochain payroll

    private String typeInteret; // PLAT, POURCENTAGE

    @DecimalMin(value = "0.00", inclusive = true, message = "Taux interet must be non-negative")
    @Digits(integer = 6, fraction = 4, message = "Taux interet must have at most 6 integer digits and 4 decimal places")
    private BigDecimal tauxInteret = BigDecimal.ZERO;

    @Pattern(regexp = "^[YN]$", message = "Avance must be 'Y' or 'N'")
    private String avance = "N";

    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;

    @NotBlank(message = "Note is required")
    private String note;

    @Min(value = 1, message = "Ordre must be at least 1")
    private Integer ordre = 1;

    private Long regimePaieId;

    private Long typeRevenuId;
}
