package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "emploi_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploiEmploye {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;
    
    @Column(name = "date_fin")
    private LocalDate dateFin;
    
    @Column(name = "motif_fin", columnDefinition = "TEXT")
    private String motifFin;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_emploi", nullable = false, length = 20)
    private StatutEmploi statutEmploi = StatutEmploi.NOUVEAU; // Default to NOUVEAU
    
    @Column(name = "date_fin_statut")
    private LocalDate dateFinStatut;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_contrat", length = 50)
    private TypeContrat typeContrat;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "temps_travail", length = 20)
    private TempsTravail tempsTravail;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_employe_id", nullable = false)
    private TypeEmploye typeEmploye;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unite_organisationnelle_id", nullable = false)
    private UniteOrganisationnelle uniteOrganisationnelle;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id")
    private Poste poste;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horaire_id")
    private Horaire horaire;
    
    @Column(name = "taux_supplementaire", nullable = false, precision = 18, scale = 2)
    private BigDecimal tauxSupplementaire = BigDecimal.ZERO;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fonction_id")
    private Fonction fonction;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestionnaire_id")
    private Employe gestionnaire;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_conge_id")
    private TypeConge typeConge;
    
    @Column(name = "jour_off_1")
    private Integer jourOff1; // 1-7 (Monday-Sunday)
    
    @Column(name = "jour_off_2")
    private Integer jourOff2;
    
    @Column(name = "jour_off_3")
    private Integer jourOff3;
    
    @Column(name = "en_conge", nullable = false, length = 1)
    private String enConge = "N"; // 'Y' or 'N'
    
    @Column(name = "en_probation", nullable = false, length = 1)
    private String enProbation = "N"; // 'Y' or 'N'
    
    @Column(name = "principal", nullable = false, length = 1)
    private String principal = "N"; // 'Y' or 'N', only one per employee
    
    // Audit fields
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
    
    // One-to-many relationship
    @ToString.Exclude
    @OneToMany(mappedBy = "emploi", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<EmployeSalaire> salaires;
    
    public enum StatutEmploi {
        NOUVEAU, ACTIF, SUSPENDU, TERMINE, LICENCIE, ABANDONNE
    }
    
    public enum TypeContrat {
        PERMANENT, TEMPORAIRE, STAGE, CONSULTANT
    }
    
    public enum TempsTravail {
        TEMPS_PLEIN, TEMPS_PARTIEL
    }
}
