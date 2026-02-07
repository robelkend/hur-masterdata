package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.PlanAssurance;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanAssuranceUpdateDTO {
    
    @NotNull(message = "Row version (rowscn) is required for optimistic concurrency control")
    private Integer rowscn;
    
    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @Size(max = 50, message = "Code payroll must not exceed 50 characters")
    private String codePayroll;
    
    @NotNull(message = "Type prelevement is required")
    private PlanAssurance.TypePrelevement typePrelevement;
    
    @NotNull(message = "Valeur is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Valeur must be positive")
    @Digits(integer = 13, fraction = 2, message = "Valeur must have at most 13 integer digits and 2 decimal places")
    private BigDecimal valeur;
    
    @NotNull(message = "Valeur couverte is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Valeur couverte must be positive")
    @Digits(integer = 13, fraction = 2, message = "Valeur couverte must have at most 13 integer digits and 2 decimal places")
    private BigDecimal valeurCouverte;
    
    @NotBlank(message = "Code institution is required")
    @Size(max = 50, message = "Code institution must not exceed 50 characters")
    private String codeInstitution;
    
    @NotNull(message = "Categorie is required")
    private PlanAssurance.Categorie categorie;
}
