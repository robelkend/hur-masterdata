package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "autre_revenu_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutreRevenuEmploye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_revenu_id", nullable = false)
    private TypeRevenu typeRevenu;

    @Column(name = "date_revenu", nullable = false)
    private LocalDate dateRevenu;

    @Column(name = "date_effet")
    private LocalDate dateEffet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_id", nullable = false)
    private Devise devise;

    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    private BigDecimal montant;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_inclusion", nullable = false, length = 20)
    private ModeInclusion modeInclusion = ModeInclusion.PROCHAINE_PAIE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regime_paie_id")
    private RegimePaie regimePaie;

    @Column(name = "date_inclusion")
    private LocalDate dateInclusion;

    @Column(name = "reference", length = 255)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutAutreRevenu statut = StatutAutreRevenu.BROUILLON;

    @Column(name = "payroll_no", nullable = false)
    private Integer payrollNo = 0;

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

    public enum ModeInclusion {
        PROCHAINE_PAIE, MANUEL
    }

    public enum StatutAutreRevenu {
        BROUILLON, REJETE, VALIDE, ANNULE, PAYE
    }
}
