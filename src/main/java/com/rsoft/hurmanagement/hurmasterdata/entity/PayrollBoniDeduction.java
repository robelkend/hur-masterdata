package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payroll_boni_deduction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollBoniDeduction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_boni_id", nullable = false)
    private PayrollEmployeBoni payrollBoni;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(name = "code_deduction", nullable = false, length = 120)
    private String codeDeduction;

    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;

    @Column(name = "base_montant", nullable = false, precision = 18, scale = 2)
    private BigDecimal baseMontant = BigDecimal.ZERO;

    @Column(name = "taux", nullable = false, precision = 18, scale = 4)
    private BigDecimal taux = BigDecimal.ZERO;

    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    private BigDecimal montant = BigDecimal.ZERO;

    @Column(name = "montant_couvert", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantCouvert = BigDecimal.ZERO;

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
}
