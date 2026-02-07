package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormuleCreateDTO {
    @NotBlank(message = "Code variable is required")
    @Size(max = 80, message = "Code variable must not exceed 80 characters")
    private String codeVariable;
    
    private BigDecimal valeurDefaut;
    
    private String actif = "Y";
    
    @NotNull(message = "Date effectif is required")
    private LocalDate dateEffectif;
    
    private LocalDate dateFin;
    
    private String description;

    @NotBlank(message = "Expression is required")
    private String expression;
}
