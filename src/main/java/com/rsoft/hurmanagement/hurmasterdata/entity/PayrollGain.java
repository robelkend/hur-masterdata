package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payroll_gain")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollGain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_employe_id", nullable = false)
    private PayrollEmploye payrollEmploye;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubrique_paie_id", nullable = false)
    private RubriquePaie rubriquePaie;

    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    private BigDecimal montant = BigDecimal.ZERO;

    @Column(name = "imposable", nullable = false, length = 1)
    private String imposable = "Y";

    @Column(name = "soumis_cotisations", nullable = false, length = 1)
    private String soumisCotisations = "Y";

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private SourceGain source = SourceGain.SYSTEME;

    @Column(name = "reference_externe", length = 120)
    private String referenceExterne;

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

    public enum SourceGain {
        SYSTEME,
        MANUEL,
        IMPORT
    }
}
