package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "type_sanction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeSanction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_sanction", nullable = false, unique = true, length = 50)
    private String codeSanction;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gravite", nullable = false, length = 20)
    private Gravite gravite;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 20)
    private Categorie categorie;
    
    // Audit fields
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_on", nullable = false)
    private OffsetDateTime createdOn;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_on")
    private OffsetDateTime updatedOn;
    
    // Optimistic concurrency control
    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
    
    public enum Gravite {
        GRAVE, MOYEN, AUCUN
    }
    
    public enum Categorie {
        SANCTION, BLAME, RETARD, ABSENCE
    }
}
