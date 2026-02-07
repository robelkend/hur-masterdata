package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "employe_salaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeSalaire {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emploi_id", nullable = false)
    private EmploiEmploye emploi;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regime_paie_id", nullable = false)
    private RegimePaie regimePaie;
    
    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    private BigDecimal montant;
    
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;
    
    @Column(name = "date_fin")
    private LocalDate dateFin;
    
    @Column(name = "principal", nullable = false, length = 1)
    private String principal = "N"; // 'Y' or 'N', only one per employee, linked to principal employment
    
    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "N"; // 'Y' or 'N', auto-managed
    
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
