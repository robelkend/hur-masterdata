package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "role_groupe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleGroupe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_groupe", nullable = false, unique = true, length = 100)
    private String codeGroupe;
    
    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;
    
    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "N"; // 'Y' or 'N', default 'N'

    @Column(name = "all_access", nullable = false, length = 1)
    private String allAccess = "N"; // 'Y' or 'N', default 'N'
    
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
