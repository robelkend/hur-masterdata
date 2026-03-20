package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "pointage_brut")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointageBrut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @Column(name = "systeme_source", nullable = false, length = 30)
    private String systemeSource = "HORODATEUR";

    @Column(name = "id_pointage_source", length = 80)
    private String idPointageSource;

    @Column(name = "id_appareil", length = 50)
    private String idAppareil;

    @Column(name = "id_badge", length = 50)
    private String idBadge;

    @Column(name = "date_heure_pointage", nullable = false)
    private OffsetDateTime dateHeurePointage;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_evenement", nullable = false, length = 10)
    private TypeEvenement typeEvenement = TypeEvenement.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "qualite_pointage", nullable = false, length = 20)
    private QualitePointage qualitePointage = QualitePointage.BRUT;

    @Column(name = "motif_rejet", length = 120)
    private String motifRejet;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_traitement", nullable = false, length = 20)
    private StatutTraitement statutTraitement = StatutTraitement.BRUT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presence_employe_id")
    private PresenceEmploye presenceEmploye;

    @Column(name = "traite_le")
    private OffsetDateTime traiteLe;

    @Column(name = "traite_par", length = 100)
    private String traitePar;

    @Column(name = "importe_le", nullable = false)
    private OffsetDateTime importeLe = OffsetDateTime.now();

    @Column(name = "importe_par", nullable = false, length = 100)
    private String importePar = "SYSTEM";

    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;

    public enum TypeEvenement {
        IN, OUT, UNKNOWN
    }

    public enum QualitePointage {
        BRUT, OK, DUPLICAT, SUSPECT, REJETE
    }

    public enum StatutTraitement {
        BRUT, PRET, UTILISE, IGNORE, ERREUR
    }
}
