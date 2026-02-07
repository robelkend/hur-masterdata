package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.PlanAssurance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanAssuranceDTO {
    private Long id;
    private String codePlan;
    private String libelle;
    private String description;
    private String codePayroll;
    private String referencePayrollDescription;
    private PlanAssurance.TypePrelevement typePrelevement;
    private BigDecimal valeur;
    private BigDecimal valeurCouverte;
    private String codeInstitution;
    private String compagnieAssuranceNom;
    private PlanAssurance.Categorie categorie;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
