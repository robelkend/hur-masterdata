package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "param_generation_code_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParamGenerationCodeEmploye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_employe_id")
    private TypeEmploye typeEmploye;

    @Column(name = "date_effectif", nullable = false)
    private LocalDate dateEffectif;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_generation", nullable = false, length = 20)
    private ModeGeneration modeGeneration;

    @Column(name = "valeur_depart")
    private Integer valeurDepart;

    @Column(name = "valeur_courante")
    private Integer valeurCourante;

    @Column(name = "pas_incrementation", nullable = false)
    private Integer pasIncrementation = 1;

    @Column(name = "longueur_min")
    private Integer longueurMin;

    @Column(name = "padding_char", nullable = false, length = 1)
    private String paddingChar = "0";

    @Column(name = "prefixe_fixe", length = 50)
    private String prefixeFixe;

    @Column(name = "suffixe_fixe", length = 50)
    private String suffixeFixe;

    @Column(name = "pattern", columnDefinition = "TEXT")
    private String pattern;

    @Column(name = "majuscules", nullable = false, length = 1)
    private String majuscules = "Y"; // 'Y' or 'N'

    @Column(name = "enlever_accents", nullable = false, length = 1)
    private String enleverAccents = "Y"; // 'Y' or 'N'

    @Column(name = "options", nullable = false, columnDefinition = "jsonb")
    private String options = "{}"; // JSONB stored as String

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

    public enum ModeGeneration {
        SEQUENCE, PATTERN
    }
}
