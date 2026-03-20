package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "regime_paie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegimePaie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_regime_paie", nullable = false, unique = true, length = 50)
    private String codeRegimePaie;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_remuneration", nullable = false, length = 20)
    private ModeRemuneration modeRemuneration;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "periodicite", nullable = false, length = 20)
    private Periodicite periodicite;

    @Column(name = "nb_periode_paie", nullable = false)
    private Integer nbPeriodePaie;

    @Column(name = "periode_paie_courante", nullable = false)
    private Integer periodePaieCourante;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_id", nullable = false)
    private Devise devise;
    
    @Column(name = "horaire_actif", nullable = false, length = 1)
    private String horaireActif; // 'Y' or 'N', default 'Y'
    
    @Column(name = "jours_payes")
    private Integer joursPayes;
    
    @Column(name = "supp_auto", nullable = false, length = 1)
    private String suppAuto; // 'Y' or 'N', default 'N'
    
    @Column(name = "bloquer_net_negatif", nullable = false, length = 1)
    private String bloquerNetNegatif; // 'Y' or 'N', default 'N'
    
    @Column(name = "taxe_chaque_n_paies", nullable = false)
    private Integer taxeChaqueNPaies; // default 0
    
    @Column(name = "supp_chaque_n_paies", nullable = false)
    private Integer suppChaqueNPaies; // default 0
    
    @Column(name = "supp_decalage_nb_paies", nullable = false)
    private Integer suppDecalageNbPaies; // default 0
    
    @Column(name = "auto_traitement", nullable = false, length = 1)
    private String autoTraitement; // 'Y' or 'N', default 'N'
    
    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_auto_traitement", nullable = false, length = 20)
    private NiveauAutoTraitement niveauAutoTraitement; // default 'AUCUN'
    
    @Column(name = "heures_min_jour", length = 5)
    private String heuresMinJour; // format HH:mi
    
    @Column(name = "payer_si_moins_min", nullable = false, length = 1)
    private String payerSiMoinsMin; // 'Y' or 'N', default 'Y'
    
    @Column(name = "retards_max_jour", nullable = false)
    private Integer retardsMaxJour; // default 0
    
    @Column(name = "paiement_sur_compte", nullable = false, length = 1)
    private String paiementSurCompte; // 'Y' or 'N', default 'Y'
    
    @Column(name = "taxe_sur_dernier_net_positif", nullable = false, length = 1)
    private String taxeSurDernierNetPositif; // 'Y' or 'N', default 'Y'
    
    @Column(name = "taxable", nullable = false, length = 1)
    private String taxable; // 'Y' or 'N', default 'Y'
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Employe responsable;
    
    @Column(name = "derniere_paie")
    private LocalDate dernierePaie;
    
    @Column(name = "prochaine_paie")
    private LocalDate prochainePaie;
    
    @Column(name = "dernier_prelevement")
    private LocalDate dernierPrelevement;
    
    @Column(name = "dernier_supplement")
    private LocalDate dernierSupplement;
    
    @Column(name = "prochain_supplement")
    private LocalDate prochainSupplement;
    
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
    
    public enum ModeRemuneration {
        SALAIRE, HORAIRE, JOURNALIER, PIECE, PIECE_FIXE
    }
    
    public enum Periodicite {
        JOURNALIER, HEBDO, QUINZAINE, QUINZOMADAIRE, MENSUEL, TRIMESTRIEL, SEMESTRIEL, ANNUEL
    }
    
    public enum NiveauAutoTraitement {
        AUCUN, VALIDE, POSTE
    }
}
