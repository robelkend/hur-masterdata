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
public class TauxChangeCreateDTO {
    
    @NotNull(message = "Date taux is required")
    private LocalDate dateTaux;
    
    @NotNull(message = "Taux is required")
    @DecimalMin(value = "0.000001", inclusive = true, message = "Taux must be positive")
    @Digits(integer = 12, fraction = 6, message = "Taux must have at most 12 integer digits and 6 decimal places")
    private BigDecimal taux;
    
    // Taux payroll is optional, will be initialized to 0 if not provided
    @DecimalMin(value = "0", inclusive = true, message = "Taux payroll must be positive or zero")
    @Digits(integer = 12, fraction = 6, message = "Taux payroll must have at most 12 integer digits and 6 decimal places")
    private BigDecimal tauxPayroll;
    
    @NotBlank(message = "Devise code is required")
    @Size(max = 50, message = "Devise code must not exceed 50 characters")
    private String codeDevise;
    
    @Size(max = 50, message = "Institution code must not exceed 50 characters")
    private String codeInstitution;
}
