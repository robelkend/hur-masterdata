package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionPieceCreateDTO {
    @NotNull(message = "Employe is required")
    private Long employeId;

    @NotNull(message = "Type piece is required")
    private Long typePieceId;

    @NotNull(message = "Date jour is required")
    private LocalDate dateJour;

    @NotNull(message = "Quantite is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Quantite must be >= 0")
    private BigDecimal quantite;

    @NotNull(message = "Quantite rejet is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Quantite rejet must be >= 0")
    private BigDecimal quantiteRejet;

    private String statut;
    private String note;
    private Long payrollId;
    private Long emploiEmployeId;
    private Long employeSalaireId;
}
