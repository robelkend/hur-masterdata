package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pret_remboursement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PretRemboursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pret_employe_id", nullable = false)
    private PretEmploye pretEmploye;

    @Column(name = "date_remboursement", nullable = false)
    private LocalDate dateRemboursement;

    @Column(name = "montant_rembourse", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantRembourse;

    @Column(name = "montant_interet", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantInteret;

    @Column(name = "montant_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "origine", nullable = false, length = 50)
    private OrigineRemboursement origine;

    @Column(name = "no_payroll", nullable = false)
    private Integer noPayroll = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutRemboursement statut = StatutRemboursement.BROUILLON;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

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

    public enum OrigineRemboursement {
        PAIE,
        MANUEL,
        AJUSTEMENT
    }

    public enum StatutRemboursement {
        BROUILLON,
        PAYE
    }
}
