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
@Table(name = "supplementaire_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplementaireEmploye {

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

    @Column(name = "memo", nullable = false, columnDefinition = "TEXT")
    private String memo;

    @Column(name = "date_jour", nullable = false)
    private LocalDate dateJour;

    @Column(name = "heure_debut", nullable = false, length = 10)
    private String heureDebut; // Stored as VARCHAR, displayed as time picker

    @Column(name = "heure_fin", nullable = false, length = 10)
    private String heureFin; // Stored as VARCHAR, displayed as time picker

    @Enumerated(EnumType.STRING)
    @Column(name = "type_supplementaire", nullable = false, length = 30)
    private TypeSupplementaire typeSupplementaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_calcul", length = 30)
    private BaseCalcul baseCalcul;

    @Column(name = "montant_base", precision = 18, scale = 2)
    private BigDecimal montantBase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_id")
    private Devise devise;

    @Column(name = "montant_calcule", precision = 18, scale = 2)
    private BigDecimal montantCalcule;

    @Column(name = "automatique", nullable = false, length = 1)
    private String automatique = "N"; // 'Y' or 'N'

    @Column(name = "no_presence", nullable = false)
    private Integer noPresence = 0;

    @Column(name = "no_payroll", nullable = false)
    private Integer noPayroll = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", nullable = false, columnDefinition = "jsonb")
    private String details = "{}"; // JSONB stored as String, contains: nb_heures, nb_jours, nb_nuits, nb_offs, nb_conges, montant_*_calcule

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutSupplementaire statut = StatutSupplementaire.BROUILLON;

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

    public enum TypeSupplementaire {
        HEURE, FERIE, NUIT, WEEKEND, OFF, CONGE, AUTRE
    }

    public enum BaseCalcul {
        SALAIRE_BASE, TAUX_HORAIRE, FIXE
    }

    public enum StatutSupplementaire {
        BROUILLON, VALIDE
    }
}
