package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "plan_assurance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanAssurance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_plan", nullable = false, unique = true, length = 50)
    private String codePlan;
    
    @Column(name = "libelle", length = 255)
    private String libelle;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "code_payroll", referencedColumnName = "code_payroll", nullable = true)
    private ReferencePayroll referencePayroll;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_prelevement", nullable = false, length = 20)
    private TypePrelevement typePrelevement;
    
    @Column(name = "valeur", nullable = false, precision = 15, scale = 2)
    private BigDecimal valeur;
    
    @Column(name = "valeur_couverte", nullable = false, precision = 15, scale = 2)
    private BigDecimal valeurCouverte;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "code_institution", referencedColumnName = "code_institution", nullable = false)
    private InstitutionTierse compagnieAssurance;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 20)
    private Categorie categorie;
    
    // Audit fields
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_on", nullable = false)
    private OffsetDateTime createdOn;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_on")
    private OffsetDateTime updatedOn;
    
    // Optimistic concurrency control
    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
    
    public enum TypePrelevement {
        PLAT, POURCENTAGE
    }
    
    public enum Categorie {
        MEDICAL, EMPLOI, VIEILLESSE, AUTRE
    }
}
