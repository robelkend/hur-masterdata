package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "role_permission", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"role_id", "ressource_id", "action_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private AppRole role;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ressource_id", nullable = false)
    private RessourceUi ressource;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private PermissionAction action;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "effet", nullable = false, length = 10)
    private Effet effet;
    
    @Column(name = "heritage_descendant", nullable = false)
    private Boolean heritageDescendant = true;
    
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
    
    public enum Effet {
        ALLOW, DENY
    }
}
