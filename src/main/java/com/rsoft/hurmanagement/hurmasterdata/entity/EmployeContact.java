package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "employe_contact")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeContact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @Column(name = "nom", nullable = false, length = 255)
    private String nom;
    
    @Column(name = "prenom", length = 255)
    private String prenom;
    
    @Column(name = "lien", length = 50)
    private String lien; // CONJOINT, PERE, MERE, PARENT, AMI, FILS, FILLE, FRERE, SOEUR, AUTRE
    
    @Column(name = "telephone1", length = 50)
    private String telephone1;
    
    @Column(name = "telephone2", length = 50)
    private String telephone2;
    
    @Column(name = "courriel", length = 255)
    private String courriel;
    
    @Column(name = "priorite", nullable = false)
    private Integer priorite = 1;
    
    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y"; // 'Y' or 'N'
    
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
}
