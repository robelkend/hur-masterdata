package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "horaire_special")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoraireSpecial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emploi_employe_id")
    private EmploiEmploye emploiEmploye;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "heure_debut", length = 5)
    private String heureDebut; // Format HH:mi

    @Column(name = "heure_fin", length = 5)
    private String heureFin; // Format HH:mi

    @Enumerated(EnumType.STRING)
    @Column(name = "priorite", nullable = false, length = 20)
    private Priorite priorite;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequence", nullable = false, length = 20)
    private Frequence frequence;

    @Column(name = "unite_freq", nullable = false)
    private Integer uniteFreq = 1;

    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y"; // 'Y' or 'N'

    @Column(name = "duplique", nullable = false, length = 1)
    private String duplique = "N"; // 'Y' or 'N'

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

    public enum Priorite {
        MINEURE, MAJEURE
    }

    public enum Frequence {
        AUCUN, JOUR, SEMAINE, QUINZAINE, MOIS
    }
}
