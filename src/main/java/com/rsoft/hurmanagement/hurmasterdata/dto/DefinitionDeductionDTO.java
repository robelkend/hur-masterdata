package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.DefinitionDeduction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefinitionDeductionDTO {
    private Long id;
    private String codeDeduction;
    private String libelle;
    private String description;
    private DefinitionDeduction.TypeDeduction typeDeduction;
    private DefinitionDeduction.BaseLimite baseLimite;
    private Long entiteId;
    private String entiteCode;
    private String entiteNom;
    private DefinitionDeduction.Arrondir arrondir;
    private BigDecimal valeur;
    private BigDecimal valeurCouvert;
    private DefinitionDeduction.Frequence frequence;
    private BigDecimal pctHorsCalcul;
    private BigDecimal minPrelevement;
    private BigDecimal maxPrelevement;
    private String probatoire;
    private String specialise;
    private List<TrancheBaremeDeductionDTO> tranches;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
