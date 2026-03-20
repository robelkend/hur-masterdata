package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ref_categorie_materiel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefCategorieMateriel {

    @Id
    @Column(name = "code_categorie", nullable = false, length = 50)
    private String codeCategorie;

    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;

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
