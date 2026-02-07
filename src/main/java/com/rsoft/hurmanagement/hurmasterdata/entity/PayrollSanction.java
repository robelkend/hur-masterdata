package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payroll_sanction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollSanction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_employe_id", nullable = false)
    private PayrollEmploye payrollEmploye;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_sanction", nullable = false, length = 20)
    private TypeSanction typeSanction;

    @Column(name = "date_jour")
    private LocalDate dateJour;

    @Column(name = "quantite_minute", precision = 18, scale = 2)
    private BigDecimal quantiteMinute;

    @Column(name = "montant", precision = 18, scale = 2)
    private BigDecimal montant;

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

    public enum TypeSanction {
        RETARD,
        ABSENCE,
        AUTRE
    }
}
