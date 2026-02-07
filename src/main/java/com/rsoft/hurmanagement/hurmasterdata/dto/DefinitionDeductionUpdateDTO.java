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
public class DefinitionDeductionUpdateDTO {
    
    @NotNull(message = "Row version (rowscn) is required for optimistic concurrency control")
    private Integer rowscn;
    
    @NotBlank(message = "Libelle is required")
    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;
    
    private String description;
    
    @NotNull(message = "Type deduction is required")
    private DefinitionDeduction.TypeDeduction typeDeduction;
    
    @NotNull(message = "Base limite is required")
    private DefinitionDeduction.BaseLimite baseLimite;
    
    private Long entiteId;
    
    @NotNull(message = "Arrondir is required")
    private DefinitionDeduction.Arrondir arrondir;
    
    @NotNull(message = "Valeur is required")
    private BigDecimal valeur;
    
    @NotNull(message = "Valeur couvert is required")
    private BigDecimal valeurCouvert;
    
    private DefinitionDeduction.Frequence frequence;
    
    private BigDecimal pctHorsCalcul;
    
    private BigDecimal minPrelevement;
    
    private BigDecimal maxPrelevement;
    
    @NotBlank(message = "Probatoire is required")
    private String probatoire;
    
    @NotBlank(message = "Specialise is required")
    private String specialise;
    
    private List<TrancheBaremeDeductionUpdateDTO> tranches;
}
