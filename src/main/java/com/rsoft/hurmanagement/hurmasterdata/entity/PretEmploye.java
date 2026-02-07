package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pret_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PretEmploye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(name = "date_pret", nullable = false)
    private LocalDate datePret;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_id", nullable = false)
    private Devise devise;

    @Column(name = "montant_pret", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantPret = BigDecimal.ZERO;

    @Column(name = "montant_subvention", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantSubvention = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicite", nullable = false, length = 50)
    private PeriodicitePret periodicite;

    @Column(name = "prelever_dans_payroll", nullable = false, length = 1)
    private String preleverDansPayroll = "Y";

    @Column(name = "prelevement_partiel", nullable = false, length = 1)
    private String prelevementPartiel = "N";

    @Column(name = "nb_prevu", nullable = false)
    private Integer nbPrevu = 1;

    @Column(name = "montant_periode", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantPeriode = BigDecimal.ZERO;

    @Column(name = "montant_verse", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantVerse = BigDecimal.ZERO;

    @Column(name = "frequence_nb_periodicites", nullable = false)
    private Integer frequenceNbPeriodicites = 1;

    @Column(name = "frequence_compteur", nullable = false)
    private Integer frequenceCompteur = 0;

    @Column(name = "premier_prelevement")
    private LocalDate premierPrelevement;

    @Column(name = "dernier_prelevement")
    private LocalDate dernierPrelevement;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_interet", length = 50)
    private TypeInteret typeInteret;

    @Column(name = "taux_interet", precision = 10, scale = 4)
    private BigDecimal tauxInteret = BigDecimal.ZERO;

    @Column(name = "avance", nullable = false, length = 1)
    private String avance = "N";

    @Column(name = "libelle", length = 255)
    private String libelle;

    @Column(name = "note", nullable = false, columnDefinition = "TEXT")
    private String note;

    @Column(name = "ordre", nullable = false)
    private Integer ordre = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regime_paie_id")
    private RegimePaie regimePaie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_revenu_id")
    private TypeRevenu typeRevenu;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private StatutPret statut = StatutPret.BROUILLON;

    @OneToMany(mappedBy = "pretEmploye", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PretRemboursement> remboursements = new ArrayList<>();

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

    public enum PeriodicitePret {
        PAIE,
        JOURNALIER,
        HEBDO,
        QUINZAINE,
        QUINZOMADAIRE,
        TRIMESTRIEL,
        SEMESTRIEL,
        ANNUEL
    }

    public enum TypeInteret {
        PLAT,
        POURCENTAGE
    }

    public enum StatutPret {
        BROUILLON,
        EN_COURS,
        TERMINE,
        ANNULE,
        SUSPENDU
    }
}
