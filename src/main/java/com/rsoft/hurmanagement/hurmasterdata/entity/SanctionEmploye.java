package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sanction_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanctionEmploye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emploi_employe_id")
    private EmploiEmploye emploiEmploye;

    @Column(name = "date_sanction")
    private LocalDate dateSanction;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_evenement", length = 20)
    private TypeEvenement typeEvenement;

    @Column(name = "valeur_mesuree", precision = 10, scale = 2)
    private BigDecimal valeurMesuree;

    @Enumerated(EnumType.STRING)
    @Column(name = "unite_mesure", length = 10)
    private UniteMesure uniteMesure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regle_id")
    private BaremeSanction regle;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_sanction", length = 20)
    private TypeSanction typeSanction;

    @Column(name = "valeur_sanction", precision = 10, scale = 2)
    private BigDecimal valeurSanction;

    @Enumerated(EnumType.STRING)
    @Column(name = "unite_sanction", length = 10)
    private UniteSanction uniteSanction;

    @Column(name = "montant_calcule", precision = 10, scale = 2)
    private BigDecimal montantCalcule;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutSanction statut = StatutSanction.NOUVEAU;

    @Column(name = "motif", columnDefinition = "TEXT")
    private String motif;

    @Column(name = "reference_externe", length = 255)
    private String referenceExterne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

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
        RETARD, ABSENCE, AUTRE
    }

    public enum UniteMesure {
        MINUTE, HEURE, JOUR
    }

    public enum TypeSanction {
        DEDUIRE_TEMPS, DEDUIRE_MONTANT, AVERTISSEMENT
    }

    public enum UniteSanction {
        MINUTE, HEURE, JOUR, MONTANT
    }

    public enum StatutSanction {
        NOUVEAU, VALIDE
    }
}
