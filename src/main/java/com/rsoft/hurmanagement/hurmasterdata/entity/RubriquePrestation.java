package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rubrique_prestation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RubriquePrestation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_prestation", nullable = false, unique = true, length = 50)
    private String codePrestation;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "prelevement", nullable = false, length = 1)
    private Prelevement prelevement;

    @Column(name = "hardcoded", nullable = false, length = 1)
    private String hardcoded = "N"; // 'Y' or 'N', default 'N'
    
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
    
    public enum Prelevement {
        N, Y
    }
}
