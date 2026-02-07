package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutreRevenuEmployeCreateDTO {
    private Long entrepriseId;

    @NotNull(message = "Employe is required")
    private Long employeId;

    @NotNull(message = "Type revenu is required")
    private Long typeRevenuId;

    @NotNull(message = "Date revenu is required")
    private LocalDate dateRevenu;

    private LocalDate dateEffet;

    @NotNull(message = "Devise is required")
    private Long deviseId;

    @NotNull(message = "Montant is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Montant must be positive")
    private BigDecimal montant;

    private String commentaire;

    @NotBlank(message = "Mode inclusion is required")
    @Size(max = 20, message = "Mode inclusion must be PROCHAINE_PAIE or MANUEL")
    private String modeInclusion = "PROCHAINE_PAIE";

    private Long regimePaieId;

    private LocalDate dateInclusion;

    @Size(max = 255, message = "Reference must not exceed 255 characters")
    private String reference;

    @NotBlank(message = "Statut is required")
    @Size(max = 20, message = "Statut must be BROUILLON, REJETE, VALIDE, ANNULE, or PAYE")
    private String statut = "BROUILLON";
}
