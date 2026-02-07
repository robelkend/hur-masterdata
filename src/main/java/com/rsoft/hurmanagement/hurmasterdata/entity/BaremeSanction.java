package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bareme_sanction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaremeSanction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_employe_id", nullable = false)
    private TypeEmploye typeEmploye;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "infraction_type", nullable = false, length = 50)
    private InfractionType infractionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "unite_infraction", nullable = false, length = 50)
    private UniteInfraction uniteInfraction;
    
    @Column(name = "seuil_min", nullable = false)
    private Integer seuilMin;
    
    @Column(name = "seuil_max")
    private Integer seuilMax;
    
    @Column(name = "penalite_minutes", nullable = false)
    private Integer penaliteMinutes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "unite_penalite", nullable = false, length = 50)
    private UnitePenalite unitePenalite;
    
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
    
    public enum InfractionType {
        RETARD, ABSENCE
    }
    
    public enum UniteInfraction {
        MINUTE, HEURE, JOUR
    }
    
    public enum UnitePenalite {
        MINUTE, HEURE, JOUR
    }
}
