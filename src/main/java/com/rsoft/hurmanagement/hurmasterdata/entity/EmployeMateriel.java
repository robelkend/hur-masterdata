package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "employe_materiel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "materiel_id", nullable = false)
    private RefMateriel materiel;

    @Column(name = "numero_serie", length = 150)
    private String numeroSerie;

    @Column(name = "date_attribution", nullable = false)
    private LocalDate dateAttribution;

    @Column(name = "date_fin_prevue")
    private LocalDate dateFinPrevue;

    @Column(name = "valeur_attribution", nullable = false, precision = 18, scale = 2)
    private BigDecimal valeurAttribution = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutMateriel statut = StatutMateriel.ATTRIBUE;

    @Column(name = "date_transfert_propriete")
    private LocalDate dateTransfertPropriete;

    @Column(name = "date_restitution_effective")
    private LocalDate dateRestitutionEffective;

    @Column(name = "valeur_residuelle_calculee", precision = 18, scale = 2)
    private BigDecimal valeurResiduelleCalculee;

    @Column(name = "observations", length = 500)
    private String observations;

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

    public enum StatutMateriel {
        ATTRIBUE,
        RESTITUE,
        TRANSFERE_EMPLOYE,
        PERDU,
        ENDOMMAGE,
        FACTURE,
        CLOTURE
    }
}
