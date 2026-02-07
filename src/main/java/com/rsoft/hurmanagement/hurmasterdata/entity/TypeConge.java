package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "type_conge")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeConge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_conge", nullable = false, unique = true, length = 50)
    private String codeConge;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "conge_annuel", nullable = false, length = 1)
    private CongeAnnuel congeAnnuel;
    
    @Column(name = "nb_jours", nullable = true)
    @Min(value = 0, message = "Number of days must be non-negative")
    private Integer nbJours;
    
    @Column(name = "nb_annee_cumul", nullable = true)
    @Min(value = 0, message = "Number of years for accumulation must be non-negative")
    private Integer nbAnneeCumul;
    
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
    
    public enum CongeAnnuel {
        N, Y
    }
}
