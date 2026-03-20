package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payroll_employe_agregat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollEmployeAgregat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regime_paie_id", nullable = false)
    private RegimePaie regimePaie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periode_boni_id", nullable = false)
    private PayrollPeriodeBoni periodeBoni;

    @Column(name = "montant_salaire_base", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantSalaireBase = BigDecimal.ZERO;

    @Column(name = "montant_supplementaire", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantSupplementaire = BigDecimal.ZERO;

    @Column(name = "montant_autre_revenu", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantAutreRevenu = BigDecimal.ZERO;

    @Column(name = "montant_sanctions", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantSanctions = BigDecimal.ZERO;

    @Column(name = "nb_paie", nullable = false)
    private Integer nbPaie = 0;

    @Column(name = "no_periode", nullable = false)
    private Integer noPeriode = 1;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_on", nullable = false)
    private OffsetDateTime createdOn;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_on", nullable = false)
    private OffsetDateTime updatedOn;

    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
}
