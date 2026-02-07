package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "niveau_qualification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NiveauQualification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_niveau", nullable = false, unique = true, length = 50)
    private String codeNiveau;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Column(name = "niveau_hierarchique", nullable = false)
    private Integer niveauHierarchique;
    
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
