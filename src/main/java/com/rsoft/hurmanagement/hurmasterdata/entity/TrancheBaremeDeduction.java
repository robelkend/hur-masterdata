package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tranche_bareme_deduction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrancheBaremeDeduction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_deduction_id", nullable = false)
    private DefinitionDeduction definitionDeduction;
    
    @Column(name = "borne_inf", nullable = false, precision = 15, scale = 2)
    private BigDecimal borneInf; // Inclusive
    
    @Column(name = "borne_sup", precision = 15, scale = 2)
    private BigDecimal borneSup; // Nullable = dernière tranche
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_deduction", nullable = false, length = 20)
    private DefinitionDeduction.TypeDeduction typeDeduction = DefinitionDeduction.TypeDeduction.POURCENTAGE;
    
    @Column(name = "valeur", nullable = false, precision = 15, scale = 2)
    private BigDecimal valeur;
    
    // Audit fields
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_on", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdOn;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_on", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedOn;
    
    // Optimistic concurrency control
    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
}
