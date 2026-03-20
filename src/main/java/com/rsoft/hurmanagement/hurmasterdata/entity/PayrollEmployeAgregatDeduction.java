package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payroll_employe_agregat_deduction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollEmployeAgregatDeduction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_employe_agregat_id", nullable = false)
    private PayrollEmployeAgregat payrollEmployeAgregat;

    @Column(name = "code_deduction", nullable = false, length = 120)
    private String codeDeduction;

    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 20)
    private PayrollDeduction.CategorieDeduction categorie = PayrollDeduction.CategorieDeduction.AUTRE;

    @Column(name = "base_montant", nullable = false, precision = 18, scale = 2)
    private BigDecimal baseMontant = BigDecimal.ZERO;

    @Column(name = "taux", precision = 18, scale = 4)
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

    @Column(name = "updated_on", nullable = false)
    private OffsetDateTime updatedOn;

    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
}
