package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "coordonnee_bancaire_employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordonneeBancaireEmploye {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banque_id", nullable = false)
    private InstitutionTierse banque;
    
    @Column(name = "numero_compte", nullable = false, length = 100)
    private String numeroCompte;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 20)
    private CategorieCompte categorie;
    
    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "N"; // 'Y' or 'N'
    
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
    
    public enum CategorieCompte {
        FRAIS, PRINCIPAL
    }
}
