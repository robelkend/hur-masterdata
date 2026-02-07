package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "processus_parametre")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessusParametre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_processus", nullable = false, unique = true, length = 120)
    private String codeProcessus;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y";

    @Column(name = "derniere_execution_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime derniereExecutionAt;

    @Column(name = "prochaine_execution_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime prochaineExecutionAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequence", nullable = false, length = 20)
    private Frequence frequence = Frequence.HEURE;

    @Column(name = "nombre", nullable = false)
    private Integer nombre = 1;

    @Column(name = "marge")
    private Integer marge = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "unite_marge", length = 20)
    private UniteMarge uniteMarge = UniteMarge.JOUR;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private Statut statut = Statut.REUSSI;

    @Column(name = "derniere_erreur", columnDefinition = "TEXT")
    private String derniereErreur;

    @Column(name = "nb_echecs_consecutifs", nullable = false)
    private Integer nbEchecsConsecutifs = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

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

    public enum Frequence {
        MINUTE, HEURE, JOUR, SEMAINE, MOIS, ANNEE
    }

    public enum UniteMarge {
        MINUTE, HEURE, JOUR
    }

    public enum Statut {
        EN_EXECUTION, REUSSI, ERREUR, SUSPENDU, PRET
    }
}
