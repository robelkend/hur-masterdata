package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "employe_materiel_evenement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeMaterielEvenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_materiel_id", nullable = false)
    private EmployeMateriel employeMateriel;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_evenement", nullable = false, length = 30)
    private TypeEvenement typeEvenement;

    @Column(name = "date_evenement", nullable = false)
    private LocalDate dateEvenement;

    @Column(name = "montant", precision = 18, scale = 2)
    private BigDecimal montant;

    @Column(name = "commentaire", length = 500)
    private String commentaire;

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

    public enum TypeEvenement {
        ATTRIBUTION,
        RESTITUTION,
        TRANSFERT,
        PERTE,
        DETERIORATION,
        FACTURATION,
        ANNULATION
    }
}
