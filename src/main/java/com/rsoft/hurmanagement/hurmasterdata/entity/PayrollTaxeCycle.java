package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payroll_taxe_cycle",
        uniqueConstraints = @UniqueConstraint(name = "uk_payroll_taxe_cycle_employe_regime",
                columnNames = {"employe_id", "regime_paie_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollTaxeCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regime_paie_id", nullable = false)
    private RegimePaie regimePaie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dernier_payroll_taxe_id")
    private Payroll dernierPayrollTaxe;

    @Column(name = "dernier_taxe_date_fin")
    private LocalDate dernierTaxeDateFin;

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
}
