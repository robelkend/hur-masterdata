package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "interface_loading_champ")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceLoadingChamp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loading_id", nullable = false)
    private InterfaceLoading loading;
    
    @Column(name = "nom_cible", nullable = false, length = 120)
    private String nomCible;
    
    @Column(name = "nom_source", length = 120)
    private String nomSource;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_donnee", nullable = false, length = 30)
    private TypeDonnee typeDonnee = TypeDonnee.CHAR;
    
    @Column(name = "taille")
    private Integer taille;
    
    @Column(name = "format", length = 100)
    private String format;
    
    @Column(name = "position", nullable = false)
    private Integer position;
    
    @Column(name = "valeur", columnDefinition = "TEXT")
    private String valeur;

    @Column(name = "update_champ", length = 120)
    private String updateChamp;

    @Column(name = "update_valeur", columnDefinition = "TEXT")
    private String updateValeur;

    @Column(name = "update_condition", columnDefinition = "TEXT")
    private String updateCondition;
    
    @Column(name = "obligatoire", nullable = false, length = 1)
    private String obligatoire = "N"; // 'Y' or 'N'
    
    // Audit fields
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_on", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdOn;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_on", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedOn;
    
    // Optimistic concurrency control
    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
    
    public enum TypeDonnee {
        CHAR, DATE, DOUBLE, FUNCTION, EXTRA
    }
}
