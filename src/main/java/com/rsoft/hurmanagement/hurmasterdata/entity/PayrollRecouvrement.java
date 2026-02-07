package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payroll_recouvrement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRecouvrement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_employe_id", nullable = false)
    private PayrollEmploye payrollEmploye;

    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_recouvrement", nullable = false, length = 20)
    private TypeRecouvrement typeRecouvrement = TypeRecouvrement.PRET;

    @Column(name = "reference_no", length = 120)
    private String referenceNo;

    @Column(name = "montant_periode", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantPeriode = BigDecimal.ZERO;

    @Column(name = "montant_interet", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantInteret = BigDecimal.ZERO;

    @Column(name = "montant_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(name = "solde_avant", precision = 18, scale = 2)
    private BigDecimal soldeAvant;

    @Column(name = "solde_apres", precision = 18, scale = 2)
    private BigDecimal soldeApres;

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

    public enum TypeRecouvrement {
        PRET,
        AVANCE,
        AUTRE
    }
}
