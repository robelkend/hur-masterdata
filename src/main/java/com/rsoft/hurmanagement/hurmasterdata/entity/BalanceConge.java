package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "balance_conge",
       uniqueConstraints = @UniqueConstraint(columnNames = {"emploi_employe_id", "employe_id", "type_conge_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emploi_employe_id")
    private EmploiEmploye emploiEmploye;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_conge_id", nullable = false)
    private TypeConge typeConge;

    @Column(name = "solde_actuel", nullable = false, precision = 10, scale = 2)
    private BigDecimal soldeActuel = BigDecimal.ZERO;

    @Column(name = "solde_disponible", nullable = false, precision = 10, scale = 2)
    private BigDecimal soldeDisponible = BigDecimal.ZERO;

    @Column(name = "derniere_mise_a_jour")
    private LocalDate derniereMiseAJour;

    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y";

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
}
