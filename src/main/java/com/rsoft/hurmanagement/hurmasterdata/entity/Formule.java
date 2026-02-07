package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "formule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Formule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_variable", nullable = false, unique = true, length = 80)
    private String codeVariable;

    @Column(name = "valeur_defaut", precision = 15, scale = 2)
    private BigDecimal valeurDefaut;

    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y"; // 'Y' or 'N'

    @Column(name = "date_effectif", nullable = false)
    private LocalDate dateEffectif;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "expression", columnDefinition = "TEXT", nullable = false)
    private String expression;

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
}
