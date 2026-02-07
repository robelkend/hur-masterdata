package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TauxChangeDTO {
    private Long id;
    private LocalDate dateTaux;
    private BigDecimal taux;
    private BigDecimal tauxPayroll;
    private String codeDevise;
    private String deviseDescription;
    private String codeInstitution;
    private String institutionNom;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
