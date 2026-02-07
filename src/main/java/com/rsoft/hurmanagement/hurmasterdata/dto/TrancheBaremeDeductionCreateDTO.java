package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.DefinitionDeduction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrancheBaremeDeductionCreateDTO {
    
    @NotNull(message = "Borne inf is required")
    private BigDecimal borneInf;
    
    private BigDecimal borneSup;
    
    @NotNull(message = "Type deduction is required")
    private DefinitionDeduction.TypeDeduction typeDeduction = DefinitionDeduction.TypeDeduction.POURCENTAGE;
    
    @NotNull(message = "Valeur is required")
    private BigDecimal valeur;
}
