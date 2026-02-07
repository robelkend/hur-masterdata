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
public class PretEmployeUpdateDTO {
    @NotNull(message = "Id is required")
    private Long id;

    @NotNull(message = "Rowscn is required")
    private Integer rowscn;

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
    private BigDecimal montantSubvention;

    @NotNull(message = "Periodicite is required")
    private String periodicite;

    @Pattern(regexp = "^[YN]$", message = "Prelever dans payroll must be 'Y' or 'N'")
    private String preleverDansPayroll;

    @Pattern(regexp = "^[YN]$", message = "Prelevement partiel must be 'Y' or 'N'")
    private String prelevementPartiel;

    @NotNull(message = "Nb prevu is required")
    @Min(value = 1, message = "Nb prevu must be at least 1")
    private Integer nbPrevu;

    @NotNull(message = "Montant periode is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Montant periode must be positive")
    @Digits(integer = 13, fraction = 2, message = "Montant periode must have at most 13 integer digits and 2 decimal places")
    private BigDecimal montantPeriode;

    @Min(value = 1, message = "Frequence nb periodicites must be at least 1")
    private Integer frequenceNbPeriodicites;

    private LocalDate premierPrelevement;

    private String typeInteret;

    @DecimalMin(value = "0.00", inclusive = true, message = "Taux interet must be non-negative")
    @Digits(integer = 6, fraction = 4, message = "Taux interet must have at most 6 integer digits and 4 decimal places")
    private BigDecimal tauxInteret;

    @Pattern(regexp = "^[YN]$", message = "Avance must be 'Y' or 'N'")
    private String avance;

    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;

    @NotBlank(message = "Note is required")
    private String note;

    @Min(value = 1, message = "Ordre must be at least 1")
    private Integer ordre;

    private Long regimePaieId;

    private Long typeRevenuId;
}
