package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "type_revenu", 
       uniqueConstraints = @UniqueConstraint(name = "uk_type_revenu_entreprise_code", 
                                             columnNames = {"entreprise_id", "code_revenu"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeRevenu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;
    
    @Column(name = "code_revenu", nullable = false, length = 50)
    private String codeRevenu;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubrique_paie_id", nullable = false)
    private RubriquePaie rubriquePaie;
    
    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y"; // 'Y' or 'N'
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formule_id")
    private Formule formule;
    
    @Column(name = "ajouter_sal_base", nullable = false, length = 1)
    private String ajouterSalBase = "N"; // 'Y' or 'N'

    @Column(name = "hardcoded", nullable = false, length = 1)
    private String hardcoded = "N"; // 'Y' or 'N', default 'N'
    
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_on", nullable = false)
    private OffsetDateTime createdOn;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_on")
    private OffsetDateTime updatedOn;
    
    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
    
}
