package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "definition_deduction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefinitionDeduction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_deduction", nullable = false, unique = true, length = 50)
    private String codeDeduction;
    
    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_deduction", nullable = false, length = 20)
    private TypeDeduction typeDeduction = TypeDeduction.POURCENTAGE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "base_limite", nullable = false, length = 20)
    private BaseLimite baseLimite = BaseLimite.FIXE;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entite_id")
    private InstitutionTierse entite;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "arrondir", nullable = false, length = 20)
    private Arrondir arrondir;
    
    @Column(name = "valeur", nullable = false, precision = 15, scale = 2)
    private BigDecimal valeur = BigDecimal.ZERO;
    
    @Column(name = "valeur_couvert", nullable = false, precision = 15, scale = 2)
    private BigDecimal valeurCouvert = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "frequence", length = 20)
    private Frequence frequence;
    
    @Column(name = "pct_hors_calcul", precision = 5, scale = 2)
    private BigDecimal pctHorsCalcul = BigDecimal.ZERO;
    
    @Column(name = "min_prelevement", precision = 15, scale = 2)
    private BigDecimal minPrelevement = BigDecimal.ZERO;
    
    @Column(name = "max_prelevement", precision = 15, scale = 2)
    private BigDecimal maxPrelevement = BigDecimal.ZERO;
    
    @Column(name = "probatoire", nullable = false, length = 1)
    private String probatoire = "Y"; // 'Y' or 'N'
    
    @Column(name = "specialise", nullable = false, length = 1)
    private String specialise = "N"; // 'Y' or 'N'
    
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
    
    public enum TypeDeduction {
        PLAT, POURCENTAGE
    }
    
    public enum BaseLimite {
        FIXE, ANNUEL
    }
    
    public enum Arrondir {
        UNITE, DIXIEME, CENTIEME, MILLIEME
    }
    
    public enum Frequence {
        AUCUN, JOURNALIER, HEBDOMADAIRE, QUINZAINE, QUIZOMADAIRE, MENSUEL
    }
}
