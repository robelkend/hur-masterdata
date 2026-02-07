package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "type_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeEmploye {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Column(name = "pause_debut", length = 5)
    private String pauseDebut; // HH:mm format
    
    @Column(name = "pause_fin", length = 5)
    private String pauseFin; // HH:mm format
    
    @Column(name = "payer_absence", length = 1)
    private String payerAbsence; // 'Y' or 'N', default 'Y'
    
    @Column(name = "payer_absence_motivee", length = 1)
    private String payerAbsenceMotivee; // 'Y' or 'N', default 'Y'
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_id")
    private Devise devise;
    
    @Column(name = "salaire_minimum", precision = 15, scale = 2)
    private BigDecimal salaireMinimum; // default 0
    
    @Column(name = "salaire_maximum", precision = 15, scale = 2)
    private BigDecimal salaireMaximum; // default 0
    
    @Column(name = "ajouter_bonus_apres_nb_minute_presence")
    private Integer ajouterBonusApresNbMinutePresence;
    
    @Column(name = "pourcentage_jour_bonus", precision = 5, scale = 2)
    private BigDecimal pourcentageJourBonus;
    
    @Column(name = "generer_prestation", length = 1)
    private String genererPrestation; // 'Y' or 'N', default 'Y'
    
    @Column(name = "base_calcul_boni")
    private Integer baseCalculBoni; // 1-12 (months)
    
    @Column(name = "supplementaire", nullable = false, length = 1)
    private String supplementaire; // 'Y' or 'N', default 'Y', cannot be empty
    
    @Enumerated(EnumType.STRING)
    @Column(name = "base_calcul_supplementaire", length = 50)
    private BaseCalculSupplementaire baseCalculSupplementaire;
    
    @Column(name = "calculer_supplementaire_apres")
    private Integer calculerSupplementaireApres;
    
    @Column(name = "probation", length = 1)
    private String probation; // 'Y' or 'N', default 'Y'
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_management", length = 50)
    private StatutManagement statutManagement; // MANAGER or NON_MANAGER, default NON_MANAGER
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "famille_metier_id")
    private FamilleMetier familleMetier;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "niveau_employe_id")
    private NiveauEmploye niveauEmploye;
    
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
    
    public enum BaseCalculSupplementaire {
        JOURNALIER, HEBDOMADAIRE, QUINZAINE, MENSUEL
    }
    
    public enum StatutManagement {
        MANAGER, NON_MANAGER
    }
}
