package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payroll_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollEmploye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(name = "montant_salaire_base", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantSalaireBase = BigDecimal.ZERO;

    @Column(name = "montant_supplementaire", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantSupplementaire = BigDecimal.ZERO;

    @Column(name = "montant_autre_revenu", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantAutreRevenu = BigDecimal.ZERO;

    @Column(name = "montant_brut", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantBrut = BigDecimal.ZERO;

    @Column(name = "montant_deductions", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantDeductions = BigDecimal.ZERO;

    @Column(name = "montant_recouvrements", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantRecouvrements = BigDecimal.ZERO;

    @Column(name = "montant_sanctions", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantSanctions = BigDecimal.ZERO;

    @Column(name = "montant_net_a_payer", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantNetAPayer = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false, length = 20)
    private ModePaiement modePaiement = ModePaiement.VIREMENT;

    @Column(name = "no_cheque", length = 50)
    private String noCheque;

    @Column(name = "libelle_banque", length = 120)
    private String libelleBanque;

    @Column(name = "no_compte", length = 120)
    private String noCompte;

    @Column(name = "type_compte", length = 120)
    private String typeCompte;

    @Column(name = "email_envoye", nullable = false, length = 1)
    private String emailEnvoye = "N";

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

    public enum ModePaiement {
        VIREMENT,
        CHEQUE,
        ESPECES
    }
}
