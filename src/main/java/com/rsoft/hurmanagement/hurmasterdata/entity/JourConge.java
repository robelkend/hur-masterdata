package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "jour_ferie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JourConge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private Type type;
    
    @Column(name = "date_conge", nullable = false)
    private LocalDate dateConge;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "mi_journee", nullable = false, length = 1)
    private MiJournee miJournee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "actif", nullable = false, length = 1)
    private Actif actif;
    
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
    
    public enum Type {
        FIXE, PAQUE, CARNAVAL, AUTRE
    }
    
    public enum MiJournee {
        Y, N
    }
    
    public enum Actif {
        Y, N
    }
}
