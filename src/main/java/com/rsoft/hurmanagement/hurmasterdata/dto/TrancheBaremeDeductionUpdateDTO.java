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
public class TrancheBaremeDeductionUpdateDTO {
    
    private Long id; // null for new, existing id for update
    
    @NotNull(message = "Row version (rowscn) is required for optimistic concurrency control")
    private Integer rowscn; // 0 for new, existing rowscn for update
    
    @NotNull(message = "Borne inf is required")
    private BigDecimal borneInf;
    
    private BigDecimal borneSup;
    
    @NotNull(message = "Type deduction is required")
    private DefinitionDeduction.TypeDeduction typeDeduction;
    
    @NotNull(message = "Valeur is required")
    private BigDecimal valeur;
}
