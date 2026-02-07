package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "presence_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceEmploye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(name = "date_jour", nullable = false)
    private LocalDate dateJour;

    @Column(name = "date_depart")
    private LocalDate dateDepart;

    @Column(name = "heure_arrivee", nullable = false, length = 10)
    private String heureArrivee;

    @Column(name = "heure_depart", length = 10)
    private String heureDepart;

    @Column(name = "nuit_planifiee", nullable = false, length = 1)
    private String nuitPlanifiee = "N";

    @Column(name = "heure_debut_prevue", length = 10)
    private String heureDebutPrevue;

    @Column(name = "heure_fin_prevue", length = 10)
    private String heureFinPrevue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_employe_id")
    private TypeEmploye typeEmploye;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regime_paie_id")
    private RegimePaie regimePaie;

    @Column(name = "id_horaire")
    private Long idHoraire;

    @Column(name = "horaire_special", nullable = false, length = 1)
    private String horaireSpecial = "N";

    @Column(name = "automatique", nullable = false, length = 1)
    private String automatique = "N";

    @Column(name = "generer_supplementaire", nullable = false, length = 1)
    private String genererSupplementaire = "Y";

    @Column(name = "supplementaire_genere", nullable = false, length = 1)
    private String supplementaireGenere = "N";

    @Column(name = "generer_boni", nullable = false, length = 1)
    private String genererBoni = "Y";

    @Column(name = "boni_genere", nullable = false, length = 1)
    private String boniGenere = "N";

    @Enumerated(EnumType.STRING)
    @Column(name = "source_saisie", nullable = false, length = 20)
    private SourceSaisie sourceSaisie = SourceSaisie.MANUEL;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_presence", nullable = false, length = 20)
    private StatutPresence statutPresence = StatutPresence.BROUILLON;

    @Column(name = "nb_heures_sup", nullable = false, precision = 10, scale = 2)
    private BigDecimal nbHeuresSup = BigDecimal.ZERO;

    @Column(name = "no_supplementaire", nullable = false)
    private Integer noSupplementaire = 0;

    @Column(name = "fermeture_manuelle", nullable = false, length = 1)
    private String fermetureManuelle = "Y";

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_erreur", length = 20)
    private TypeErreur typeErreur;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", nullable = false, columnDefinition = "jsonb")
    private String details = "{}";

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_on", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdOn;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_on", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedOn;

    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;

    public enum SourceSaisie {
        MANUEL, IMPORT, BADGE, API
    }

    public enum StatutPresence {
        BROUILLON, VALIDE
    }

    public enum TypeErreur {
        INVALIDE,
        VALIDE,
        HEURE,
        NUIT,
        JOUT,
        POINTAGE_UNIQUE,
        OUT_MANQUANT,
        IN_MANQUANT,
        DOUBLON_POINTAGE,
        AMBIGU_18H,
        AMBIGU_6H,
        DUREE_TROP_LONGUE,
        DUREE_TROP_COURTE,
        PRESENCE_JOUR_PLANIF_NUIT,
        PRESENCE_NUIT_PLANIF_JOUR,
        HORS_PLAGE,
        AUTO_CLOSE_APPLIED
    }
}
