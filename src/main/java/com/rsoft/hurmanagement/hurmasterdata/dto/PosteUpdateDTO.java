package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.Poste;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PosteUpdateDTO {
    
    @NotNull(message = "Row version (rowscn) is required for optimistic concurrency control")
    private Integer rowscn;
    
    @NotNull(message = "Type salaire is required")
    private Poste.TypeSalaire typeSalaire;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @NotBlank(message = "Devise code is required")
    @Size(max = 50, message = "Devise code must not exceed 50 characters")
    private String codeDevise;
    
    @NotNull(message = "Salaire min is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Salaire min must be positive")
    @Digits(integer = 13, fraction = 2, message = "Salaire min must have at most 13 integer digits and 2 decimal places")
    private BigDecimal salaireMin;
    
    @NotNull(message = "Salaire max is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Salaire max must be positive")
    @Digits(integer = 13, fraction = 2, message = "Salaire max must have at most 13 integer digits and 2 decimal places")
    private BigDecimal salaireMax;
    
    @NotNull(message = "Nb jour semaine is required")
    @Min(value = 1, message = "Nb jour semaine must be at least 1")
    @Max(value = 7, message = "Nb jour semaine must be at most 7")
    private Integer nbJourSemaine;
}
