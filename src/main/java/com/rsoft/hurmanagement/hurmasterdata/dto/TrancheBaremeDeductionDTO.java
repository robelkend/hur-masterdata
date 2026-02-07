package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.DefinitionDeduction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrancheBaremeDeductionDTO {
    private Long id;
    private Long definitionDeductionId;
    private BigDecimal borneInf;
    private BigDecimal borneSup;
    private DefinitionDeduction.TypeDeduction typeDeduction;
    private BigDecimal valeur;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
