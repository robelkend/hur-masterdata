package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "poste")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Poste {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_poste", nullable = false, unique = true, length = 50)
    private String codePoste;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_salaire", nullable = false, length = 20)
    private TypeSalaire typeSalaire;
    
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "code_devise", referencedColumnName = "code_devise", nullable = false)
    private Devise devise;
    
    @Column(name = "salaire_min", nullable = false, precision = 15, scale = 2)
    private BigDecimal salaireMin;
    
    @Column(name = "salaire_max", nullable = false, precision = 15, scale = 2)
    private BigDecimal salaireMax;
    
    @Column(name = "nb_jour_semaine", nullable = false)
    private Integer nbJourSemaine;
    
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
    
    public enum TypeSalaire {
        FIXE, HORAIRE, JOURNALIER, PIECE, PIECE_FIXE
    }
}
