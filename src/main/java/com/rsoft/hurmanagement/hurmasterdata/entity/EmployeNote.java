package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "employe_note")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeNote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_note", nullable = false, length = 50)
    private TypeNote typeNote;
    
    @Column(name = "titre", length = 255)
    private String titre;
    
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "confidentiel", nullable = false, length = 1)
    private String confidentiel = "N"; // 'Y' or 'N'
    
    @Column(name = "envoye", nullable = false, length = 1)
    private String envoye = "N"; // 'Y' or 'N'
    
    // Audit fields
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_on", nullable = false)
    private OffsetDateTime createdOn;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_on")
    private OffsetDateTime updatedOn;
    
    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
    
    public enum TypeNote {
        BLAME, REMARQUE, PLAINTE, MESSAGE, AUTRE
    }
}
