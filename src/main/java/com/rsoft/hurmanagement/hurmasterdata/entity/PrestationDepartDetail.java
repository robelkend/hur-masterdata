package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "prestation_depart_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestationDepartDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_depart_id", nullable = false)
    private PrestationDepart prestationDepart;

    @Column(name = "rubrique_prestation", nullable = false, length = 120)
    private String rubriquePrestation;

    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 20)
    private CategoriePrestationDepart categorie;

    @Column(name = "montant_base", nullable = false, precision = 18, scale = 2)
    private BigDecimal montantBase = BigDecimal.ZERO;

    @Column(name = "taux", nullable = false, precision = 18, scale = 4)
    private BigDecimal taux = BigDecimal.ZERO;

    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    private BigDecimal montant = BigDecimal.ZERO;

    @Column(name = "ordre_affichage", nullable = false)
    private Integer ordreAffichage = 0;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_on", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdOn;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_on", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedOn;

    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
}
