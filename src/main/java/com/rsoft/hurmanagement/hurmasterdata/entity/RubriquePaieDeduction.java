package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rubrique_paie_deduction", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"definition_deduction_id", "rubrique_paie_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RubriquePaieDeduction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_deduction_id", nullable = false)
    private DefinitionDeduction definitionDeduction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubrique_paie_id", nullable = false)
    private RubriquePaie rubriquePaie;
    
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
