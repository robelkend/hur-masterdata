package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payroll_employe_boni")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollEmployeBoni {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutBoni statut = StatutBoni.CALCULE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubrique_paie_id", nullable = false)
    private RubriquePaie rubriquePaie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regime_paie_id", nullable = false)
    private RegimePaie regimePaie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periode_boni_id", nullable = false)
    private PayrollPeriodeBoni periodeBoni;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(name = "montant_reference", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantReference = BigDecimal.ZERO;

    @Column(name = "diviseur", nullable = false, precision = 18, scale = 4)
    private BigDecimal diviseur = BigDecimal.ONE;

    @Column(name = "montant_boni_brut", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantBoniBrut = BigDecimal.ZERO;

    @Column(name = "montant_deductions", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantDeductions = BigDecimal.ZERO;

    @Column(name = "montant_boni_net", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantBoniNet = BigDecimal.ZERO;

    @Column(name = "formule", nullable = false, length = 255)
    private String formule = "amt.ref / bon.div";

    @Column(name = "email_envoye", nullable = false, length = 1)
    private String emailEnvoye = "N";

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

    public enum StatutBoni {
        CALCULE,
        VALIDE
    }
}
