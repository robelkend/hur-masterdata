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
@Table(name = "absence_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceEmploye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    @ToString.Exclude
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    @ToString.Exclude
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emploi_employe_id")
    @ToString.Exclude
    private EmploiEmploye emploiEmploye;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_evenement", nullable = false, length = 20)
    private TypeEvenement typeEvenement;

    @Column(name = "date_jour", nullable = false)
    private LocalDate dateJour;

    @Column(name = "heure_debut", length = 5)
    private String heureDebut;

    @Column(name = "heure_fin", length = 5)
    private String heureFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "unite_mesure", length = 20)
    private UniteMesure uniteMesure;

    @Column(name = "quantite", precision = 10, scale = 2)
    private BigDecimal quantite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_id")
    @ToString.Exclude
    private Devise devise;

    @Column(name = "montant_equivalent", precision = 18, scale = 2)
    private BigDecimal montantEquivalent;

    @Column(name = "payroll_id")
    private Long payrollId;

    @Column(name = "justificatif", nullable = false, length = 1)
    private String justificatif = "N";

    @Column(name = "motif", length = 80)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutAbsence statut = StatutAbsence.BROUILLON;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private SourceAbsence source = SourceAbsence.MANUEL;

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

    public enum TypeEvenement {
        ABSENCE, RETARD
    }

    public enum UniteMesure {
        MINUTE, HEURE, JOUR
    }

    public enum StatutAbsence {
        BROUILLON, VALIDE, ANNULE
    }

    public enum SourceAbsence {
        MANUEL, SYSTEME, IMPORT
    }
}
