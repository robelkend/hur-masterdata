package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "unite_organisationnelle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniteOrganisationnelle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(name = "nom", nullable = false, length = 255)
    private String nom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_unite_organisationnelle_id", nullable = false)
    private TypeUniteOrganisationnelle typeUniteOrganisationnelle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unite_parente_id")
    private UniteOrganisationnelle uniteParente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_employe_id")
    private Employe responsableEmploye;
    
    // Contact fields
    @Column(name = "email", length = 255)
    private String email;
    
    @Column(name = "telephone_1", length = 50)
    private String telephone1;
    
    @Column(name = "telephone_2", length = 50)
    private String telephone2;
    
    @Column(name = "extension_telephone", length = 20)
    private String extensionTelephone;
    
    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y";
    
    @Column(name = "date_debut_effet")
    private LocalDate dateDebutEffet;
    
    @Column(name = "date_fin_effet")
    private LocalDate dateFinEffet;
    
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
}
