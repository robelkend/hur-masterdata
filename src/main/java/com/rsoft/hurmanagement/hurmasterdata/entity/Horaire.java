package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "horaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Horaire {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_horaire", nullable = false, unique = true, length = 50)
    private String codeHoraire;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Column(name = "generer_absence", nullable = false, length = 1)
    private String genererAbsence; // 'Y' or 'N', default 'Y'
    
    @Column(name = "payer_supplementaire", nullable = false, length = 1)
    private String payerSupplementaire; // 'Y' or 'N', default 'Y'
    
    @Column(name = "montant_fixe", nullable = false, length = 1)
    private String montantFixe; // 'Y' or 'N', default 'N'
    
    @Column(name = "montant_heure_sup", nullable = false, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private java.math.BigDecimal montantHeureSup;
    
    @Column(name = "coeff_jour_ferie", nullable = false, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private java.math.BigDecimal coeffJourFerie;
    
    @Column(name = "nb_heures_ref", nullable = false, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private java.math.BigDecimal nbHeuresRef;
    
    @Column(name = "coeff_dimanche", nullable = false, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private java.math.BigDecimal coeffDimanche;
    
    @Column(name = "coeff_supp_jour_ferie", nullable = false, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private java.math.BigDecimal coeffSuppJourFerie;
    
    @Column(name = "coeff_soir", nullable = false, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private java.math.BigDecimal coeffSoir;
    
    @Column(name = "coeff_supp_soir", nullable = false, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private java.math.BigDecimal coeffSuppSoir;
    
    @Column(name = "coeff_supp_off", nullable = false, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private java.math.BigDecimal coeffSuppOff;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_id", nullable = false)
    private Devise devise;
    
    @Column(name = "alterner_jour_nuit", nullable = false, length = 1)
    private String alternerJourNuit; // 'Y' or 'N', default 'N'
    
    @Enumerated(EnumType.STRING)
    @Column(name = "unite_alternance", length = 20)
    private UniteAlternance uniteAlternance;
    
    @Column(name = "nb_unite_jour", nullable = false)
    private Integer nbUniteJour; // default 0
    
    @Column(name = "heure_debut_nuit", length = 5)
    private String heureDebutNuit; // format HH:mi
    
    @Column(name = "heure_fin_nuit", length = 5)
    private String heureFinNuit; // format HH:mi
    
    @Column(name = "heure_fermeture_auto_jour", length = 5)
    private String heureFermetureAutoJour; // format HH:mi
    
    @Column(name = "heure_fermeture_auto_nuit", length = 5)
    private String heureFermetureAutoNuit; // format HH:mi
    
    @Column(name = "heure_debut", length = 5)
    private String heureDebut; // format HH:mi
    
    @Column(name = "heure_fin", length = 5)
    private String heureFin; // format HH:mi
    
    @Column(name = "detail_present", nullable = false, length = 1)
    private String detailPresent; // 'Y' or 'N', default 'Y'
    
    @Enumerated(EnumType.STRING)
    @Column(name = "shift_encours", length = 20)
    private ShiftEncours shiftEncours;
    
    @Column(name = "default_nb_hovertime")
    private Integer defaultNbHovertime;
    
    @Column(name = "debut_supplementaire", length = 5)
    private String debutSupplementaire; // format HH:mi
    
    @Column(name = "min_heure_ponctualite", length = 5)
    private String minHeurePonctualite; // format HH:mi
    
    @Column(name = "nb_minute_ponctualite")
    private Integer nbMinutePonctualite;

    @Column(name = "tolerance_retard_min", nullable = false)
    private Integer toleranceRetardMin = 5;

    @Column(name = "seuil_doublon_min", nullable = false)
    private Integer seuilDoublonMin = 2;

    @Column(name = "max_session_heures", nullable = false)
    private Integer maxSessionHeures = 16;
    
    @Column(name = "exiger_plan_nuit", nullable = false, length = 1)
    private String exigerPlanNuit; // 'Y' or 'N', default 'Y'
    
    @Column(name = "planifier_nuit_auto", nullable = false, length = 1)
    private String planifierNuitAuto; // 'Y' or 'N', default 'Y'
    
    @Column(name = "heure_fin_demi_journee", length = 5)
    private String heureFinDemiJournee; // format HH:mi
    
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
    
    public enum UniteAlternance {
        JOUR, SEMAINE, MOIS
    }
    
    public enum ShiftEncours {
        jour, soir
    }
}
