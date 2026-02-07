package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "conge_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongeEmploye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emploi_employe_id")
    private EmploiEmploye emploiEmploye;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_conge_id", nullable = false)
    private TypeConge typeConge;

    @Column(name = "date_debut_plan", nullable = false)
    private LocalDate dateDebutPlan;

    @Column(name = "date_fin_plan", nullable = false)
    private LocalDate dateFinPlan;

    @Column(name = "date_debut_reel")
    private LocalDate dateDebutReel;

    @Column(name = "date_fin_reel")
    private LocalDate dateFinReel;

    @Column(name = "motif", columnDefinition = "TEXT")
    private String motif;

    @Column(name = "reference", length = 120)
    private String reference;

    @Column(name = "approbateur", length = 255)
    private String approbateur;

    @Column(name = "date_decision", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime dateDecision;

    @Column(name = "commentaire_decision", columnDefinition = "TEXT")
    private String commentaireDecision;

    @Column(name = "nb_jours_plan", nullable = false, precision = 10, scale = 2)
    private BigDecimal nbJoursPlan = BigDecimal.ZERO;

    @Column(name = "nb_jours_reel", nullable = false, precision = 10, scale = 2)
    private BigDecimal nbJoursReel = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutConge statut = StatutConge.BROUILLON;

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

    public enum StatutConge {
        BROUILLON, SOUMIS, APPROUVE, EN_COURS, TERMINE, ANNULE, REJETE, FINALISE
    }
}
