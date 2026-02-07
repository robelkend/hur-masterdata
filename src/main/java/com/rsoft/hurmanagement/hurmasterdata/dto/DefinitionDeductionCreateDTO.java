package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.DefinitionDeduction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefinitionDeductionCreateDTO {
    
    @NotBlank(message = "Code deduction is required")
    @Size(max = 50, message = "Code deduction must not exceed 50 characters")
    private String codeDeduction;
    
    @NotBlank(message = "Libelle is required")
    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;
    
    private String description;
    
    @NotNull(message = "Type deduction is required")
    private DefinitionDeduction.TypeDeduction typeDeduction = DefinitionDeduction.TypeDeduction.POURCENTAGE;
    
    @NotNull(message = "Base limite is required")
    private DefinitionDeduction.BaseLimite baseLimite = DefinitionDeduction.BaseLimite.FIXE;
    
    private Long entiteId;
    
    @NotNull(message = "Arrondir is required")
    private DefinitionDeduction.Arrondir arrondir;
    
    @NotNull(message = "Valeur is required")
    private BigDecimal valeur = BigDecimal.ZERO;
    
    @NotNull(message = "Valeur couvert is required")
    private BigDecimal valeurCouvert = BigDecimal.ZERO;
    
    private DefinitionDeduction.Frequence frequence;
    
    private BigDecimal pctHorsCalcul = BigDecimal.ZERO;
    
    private BigDecimal minPrelevement = BigDecimal.ZERO;
    
    private BigDecimal maxPrelevement = BigDecimal.ZERO;
    
    private String probatoire = "Y";
    
    private String specialise = "N";
    
    private List<TrancheBaremeDeductionCreateDTO> tranches;
}
