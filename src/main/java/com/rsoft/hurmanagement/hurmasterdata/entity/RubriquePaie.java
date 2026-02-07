package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rubrique_paie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RubriquePaie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_rubrique", nullable = false, unique = true, length = 50)
    private String codeRubrique;
    
    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_rubrique", nullable = false, length = 20)
    private TypeRubrique typeRubrique;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_calcul", nullable = false, length = 20)
    private ModeCalcul modeCalcul;
    
    @Column(name = "boni", nullable = false, length = 1)
    private String boni; // 'Y' or 'N', default 'Y'
    
    @Column(name = "prestation", nullable = false, length = 1)
    private String prestation; // 'Y' or 'N', default 'Y'
    
    @Column(name = "imposable", nullable = false, length = 1)
    private String imposable; // 'Y' or 'N', default 'Y'

    @Column(name = "preavis", nullable = false, length = 1)
    private String preavis = "N"; // 'Y' or 'N', default 'N'

    @Column(name = "taxes_speciaux", nullable = false, length = 1)
    private String taxesSpeciaux = "N"; // 'Y' or 'N', default 'N'

    @Column(name = "soumis_cotisations", nullable = false, length = 1)
    private String soumisCotisations = "N"; // 'Y' or 'N', default 'N'

    @Column(name = "hardcoded", nullable = false, length = 1)
    private String hardcoded = "N"; // 'Y' or 'N', default 'N'
    
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
    
    public enum TypeRubrique {
        GAIN, RETENUE
    }
    
    public enum ModeCalcul {
        FIXE, HORAIRE, POURCENTAGE
    }
}
