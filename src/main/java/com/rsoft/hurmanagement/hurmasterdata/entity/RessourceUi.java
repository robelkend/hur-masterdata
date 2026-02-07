package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ressource_ui")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RessourceUi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_resource", nullable = false, unique = true, length = 255)
    private String codeResource;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_resource", nullable = false, length = 50)
    private TypeResource typeResource;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private RessourceUi parent;
    
    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;
    
    @Column(name = "est_menu", nullable = false, length = 1)
    private String estMenu = "N"; // 'Y' or 'N', default 'N'
    
    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "N"; // 'Y' or 'N', default 'N'
    
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
    
    public enum TypeResource {
        WINDOW, SECTION, COMPONENT, FIELD
    }
}
