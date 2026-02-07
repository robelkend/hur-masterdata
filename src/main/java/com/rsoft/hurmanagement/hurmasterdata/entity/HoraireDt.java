package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "horaire_dt", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"horaire_id", "jour"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoraireDt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horaire_id", nullable = false)
    private Horaire horaire;
    
    @Column(name = "jour", nullable = false)
    private Integer jour; // 1=Lundi, 2=Mardi, 3=Mercredi, 4=Jeudi, 5=Vendredi, 6=Samedi, 7=Dimanche
    
    @Column(name = "heure_debut_jour", length = 5)
    private String heureDebutJour; // format HH:mi
    
    @Column(name = "heure_fin_jour", length = 5)
    private String heureFinJour; // format HH:mi
    
    @Column(name = "heure_debut_nuit", length = 5)
    private String heureDebutNuit; // format HH:mi
    
    @Column(name = "heure_fin_nuit", length = 5)
    private String heureFinNuit; // format HH:mi
    
    @Column(name = "exiger_presence", nullable = false, length = 1)
    private String exigerPresence; // 'Y' or 'N', default 'N'
    
    @Column(name = "heure_fermeture_auto", nullable = false, length = 1)
    private String heureFermetureAuto; // 'Y' or 'N', default 'N'
    
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
}
