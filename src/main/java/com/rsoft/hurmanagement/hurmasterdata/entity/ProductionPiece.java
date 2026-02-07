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
@Table(name = "production_piece")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionPiece {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @Column(name = "date_jour", nullable = false)
    private LocalDate dateJour;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_piece_id", nullable = false)
    private TypePiece typePiece;

    @Column(name = "quantite", nullable = false, precision = 18, scale = 2)
    private BigDecimal quantite = BigDecimal.ZERO;

    @Column(name = "quantite_rejet", nullable = false, precision = 18, scale = 2)
    private BigDecimal quantiteRejet = BigDecimal.ZERO;

    @Column(name = "quantite_valide", nullable = false, precision = 18, scale = 2)
    private BigDecimal quantiteValide = BigDecimal.ZERO;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_id", nullable = false)
    private Devise devise;

    @Column(name = "prix_unitaire", nullable = false, precision = 18, scale = 2)
    private BigDecimal prixUnitaire = BigDecimal.ZERO;

    @Column(name = "montant_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(name = "payroll_id")
    private Long payrollId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emploi_employe_id")
    private EmploiEmploye emploiEmploye;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_salaire_id")
    private EmployeSalaire employeSalaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutProduction statut = StatutProduction.BROUILLON;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

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

    public enum StatutProduction {
        BROUILLON, VALIDE, PAYE, ANNULE
    }
}
