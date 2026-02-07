package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payroll_employe_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollEmployeStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_employe_id", nullable = false)
    private PayrollEmploye payrollEmploye;

    @Column(name = "metric_code", nullable = false, length = 80)
    private String metricCode;

    @Column(name = "metric_label", nullable = false, length = 120)
    private String metricLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_group", nullable = false, length = 20)
    private MetricGroup metricGroup = MetricGroup.AUTRE;

    @Enumerated(EnumType.STRING)
    @Column(name = "unite_mesure", nullable = false, length = 10)
    private UniteMesure uniteMesure = UniteMesure.HEURE;

    @Column(name = "quantite", nullable = false, precision = 18, scale = 2)
    private BigDecimal quantite = BigDecimal.ZERO;

    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    private BigDecimal montant = BigDecimal.ZERO;

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

    public enum MetricGroup {
        HEURE_SUP,
        CONGE_SUP,
        FERIE_SUP,
        OFF_SUP,
        AUTRE
    }

    public enum UniteMesure {
        HEURE,
        JOUR
    }
}
