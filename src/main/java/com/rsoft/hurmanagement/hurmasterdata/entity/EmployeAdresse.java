package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "employe_adresse")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeAdresse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_adresse", nullable = false, length = 20)
    private TypeAdresse typeAdresse;
    
    @Column(name = "ligne1", nullable = false, length = 255)
    private String ligne1;
    
    @Column(name = "ligne2", length = 255)
    private String ligne2;
    
    @Column(name = "ville", nullable = false, length = 100)
    private String ville;
    
    @Column(name = "etat", length = 100)
    private String etat;
    
    @Column(name = "code_postal", length = 20)
    private String codePostal;
    
    @Column(name = "pays", length = 2)
    private String pays; // ISO country code
    
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;
    
    @Column(name = "date_fin")
    private LocalDate dateFin;
    
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
    
    public enum TypeAdresse {
        DOMICILE, POSTALE
    }
}
