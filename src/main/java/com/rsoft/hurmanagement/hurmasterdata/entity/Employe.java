package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "employe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;
    
    @Column(name = "code_employe", nullable = false, unique = true, length = 50)
    private String codeEmploye;
    
    @Column(name = "matricule_interne", length = 50)
    private String matriculeInterne;
    
    @Column(name = "nom", nullable = false, length = 255)
    private String nom;
    
    @Column(name = "prenom", nullable = false, length = 255)
    private String prenom;

    @Column(name = "nomme", nullable = false, length = 1)
    private String nomme = "N"; // 'Y' or 'N'
    
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;
    
    @Column(name = "pays_naissance", length = 2)
    private String paysNaissance;
    
    @Column(name = "pays_habitation", length = 2)
    private String paysHabitation;
    
    @Column(name = "sexe", length = 1)
    private String sexe; // 'M' or 'F'
    
    @Column(name = "etat_civil", length = 50)
    private String etatCivil;
    
    @Column(name = "nationalite", length = 2)
    private String nationalite;
    
    @Column(name = "langue", length = 2)
    private String langue; // 'en', 'fr', 'es', 'ht'
    
    @Column(name = "courriel", length = 255)
    private String courriel;
    
    @Column(name = "telephone1", length = 50)
    private String telephone1;
    
    @Column(name = "telephone2", length = 50)
    private String telephone2;
    
    @Column(name = "photo", columnDefinition = "TEXT")
    private String photo; // Base64 or URL
    
    @Column(name = "date_embauche")
    private LocalDate dateEmbauche; // Auto-filled during nomination

    @Column(name = "date_premiere_embauche")
    private LocalDate datePremiereEmbauche; // Filled only on first nomination
    
    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "N"; // 'Y' or 'N', auto-managed
    
    // One-to-many relationships
    @ToString.Exclude
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeAdresse> adresses;
    
    @ToString.Exclude
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeIdentite> identites;
    
    @ToString.Exclude
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeContact> contacts;
    
    @ToString.Exclude
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeDocument> documents;
    
    @ToString.Exclude
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeNote> notes;
    
    @ToString.Exclude
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoordonneeBancaireEmploye> coordonneesBancaires;
    
    @ToString.Exclude
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssuranceEmploye> assurances;
    
    @ToString.Exclude
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmploiEmploye> emplois;
    
    @ToString.Exclude
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeSalaire> salaires;
    
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
