package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fonction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fonction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_fonction", nullable = false, unique = true, length = 50)
    private String codeFonction;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
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
}
