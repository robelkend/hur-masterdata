package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "preavis_parametre")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreavisParametre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_employe_id")
    private TypeEmploye typeEmploye;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regime_paie_id")
    private RegimePaie regimePaie;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_depart", nullable = false, length = 20)
    private TypeDepart typeDepart;

    @Column(name = "anciennete_min", nullable = false)
    private Integer ancienneteMin;

    @Column(name = "anciennete_max")
    private Integer ancienneteMax;

    @Column(name = "inclure_max", nullable = false, length = 1)
    private String inclureMax = "Y"; // 'Y' or 'N'

    @Column(name = "valeur_preavis", nullable = false)
    private Integer valeurPreavis;

    @Enumerated(EnumType.STRING)
    @Column(name = "unite_preavis", nullable = false, length = 10)
    private UnitePreavis unitePreavis;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_application", nullable = false, length = 20)
    private ModeApplication modeApplication;

    @Column(name = "priorite", nullable = false)
    private Integer priorite = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y"; // 'Y' or 'N'

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

    public enum TypeDepart {
        DEMISSION, ABANDON, LICENCIEMENT, FIN_CONTRAT, RETRAITE
    }

    public enum UnitePreavis {
        JOUR, MOIS
    }

    public enum ModeApplication {
        A_EFFECTUER, A_PAYER
    }
}
