package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ref_materiel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_materiel", nullable = false, unique = true, length = 80)
    private String codeMateriel;

    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;

    @Column(name = "categorie", nullable = false, length = 50)
    private String categorie = "AUTRE";

    @Column(name = "depreciable", nullable = false, length = 1)
    private String depreciable = "Y";

    @Column(name = "duree_depreciation_mois", nullable = false)
    private Integer dureeDepreciationMois = 60;

    @Column(name = "duree_transfert_propriete_mois", nullable = false)
    private Integer dureeTransfertProprieteMois = 60;

    @Column(name = "valeur_reference", nullable = false, precision = 18, scale = 2)
    private BigDecimal valeurReference = BigDecimal.ZERO;

    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y";

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
