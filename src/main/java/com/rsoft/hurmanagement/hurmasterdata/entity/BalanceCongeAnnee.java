package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "balance_conge_annee",
       uniqueConstraints = @UniqueConstraint(columnNames = {"emploi_employe_id", "type_conge_id", "annee"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceCongeAnnee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balance_conge_id", nullable = false)
    private BalanceConge balanceConge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emploi_employe_id", nullable = false)
    private EmploiEmploye emploiEmploye;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_conge_id", nullable = false)
    private TypeConge typeConge;

    @Column(name = "annee", nullable = false)
    private Integer annee;

    @Column(name = "jours_acquis", nullable = false, precision = 10, scale = 2)
    private BigDecimal joursAcquis = BigDecimal.ZERO;

    @Column(name = "jours_pris", nullable = false, precision = 10, scale = 2)
    private BigDecimal joursPris = BigDecimal.ZERO;

    @Column(name = "cumul_autorise", nullable = false, length = 1)
    private String cumulAutorise = "N";

    @Column(name = "plafond_cumul", precision = 10, scale = 2)
    private BigDecimal plafondCumul;

    @Column(name = "jours_reportes", nullable = false, precision = 10, scale = 2)
    private BigDecimal joursReportes = BigDecimal.ZERO;

    @Column(name = "jours_expires", nullable = false, precision = 10, scale = 2)
    private BigDecimal joursExpires = BigDecimal.ZERO;

    @Column(name = "solde_fin_annee", nullable = false, precision = 10, scale = 2)
    private BigDecimal soldeFinAnnee = BigDecimal.ZERO;

    @Column(name = "date_cloture")
    private LocalDate dateCloture;

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
