package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "employe_identite")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeIdentite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_piece", nullable = false, length = 50)
    private TypePiece typePiece;
    
    @Column(name = "numero_piece", nullable = false, length = 100)
    private String numeroPiece;
    
    @Column(name = "date_emission")
    private LocalDate dateEmission;
    
    @Column(name = "date_expiration")
    private LocalDate dateExpiration;
    
    @Column(name = "pays_emission", length = 2)
    private String paysEmission; // ISO country code
    
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
    
    public enum TypePiece {
        NIF, CARTE_ELECTORALE, PASSEPORT, PERMIS, AUTRE
    }
}
