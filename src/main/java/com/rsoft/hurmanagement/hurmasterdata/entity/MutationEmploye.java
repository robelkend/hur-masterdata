package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mutation_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MutationEmploye {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_mutation", nullable = false, length = 50)
    private TypeMutation typeMutation;
    
    @Column(name = "date_effet", nullable = false)
    private LocalDate dateEffet;
    
    @Column(name = "date_saisie", nullable = false)
    private LocalDate dateSaisie;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private StatutMutation statut = StatutMutation.BROUILLON;
    
    @Column(name = "motif", columnDefinition = "TEXT")
    private String motif;
    
    @Column(name = "reference", length = 255)
    private String reference;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "avant", nullable = false, columnDefinition = "jsonb")
    private String avant = "{}"; // JSONB stored as String
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "apres", nullable = false, columnDefinition = "jsonb")
    private String apres = "{}"; // JSONB stored as String
    
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
    
    public enum TypeMutation {
        CHG_POSTE,
        CHG_UNITE,
        PROMOTION,
        REVISION_SALAIRE,
        AJOUT_REGIME_PAIE,
        NOMINATION,
        DEMISSION,
        LICENCIEMENT,
        FIN_CONTRAT,
        ABANDON_POSTE,
        SUSPENSION,
        REINTEGRATION
    }
    
    public enum StatutMutation {
        BROUILLON,
        SOUMIS,
        REJETE,
        APPROUVE,
        APPLIQUE,
        ANNULE
    }
}
