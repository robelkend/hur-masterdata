package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "entreprise")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entreprise {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_entreprise", nullable = false, unique = true, length = 50)
    private String codeEntreprise;
    
    @Column(name = "nom_entreprise", nullable = false, length = 255)
    private String nomEntreprise;
    
    @Column(name = "nom_legal", length = 255)
    private String nomLegal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devise_id", nullable = false)
    private Devise devise;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "mois_debut_annee_fiscale", nullable = false, length = 50)
    private MoisDebutAnneeFiscale moisDebutAnneeFiscale;
    
    @Column(name = "annee_fiscale_courante", length = 20)
    private String anneeFiscaleCourante;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "secteur_activite", length = 50)
    private SecteurActivite secteurActivite;
    
    @Column(name = "etat", length = 100)
    private String etat;
    
    @Column(name = "ville", length = 100)
    private String ville;
    
    @Column(name = "adresse", columnDefinition = "TEXT")
    private String adresse;
    
    @Column(name = "telephone1", length = 50)
    private String telephone1;
    
    @Column(name = "telephone2", length = 50)
    private String telephone2;
    
    @Column(name = "fax", length = 50)
    private String fax;
    
    @Column(name = "courriel", length = 255)
    private String courriel;
    
    @Column(name = "conge_cumule", nullable = false, length = 1)
    private String congeCumule; // 'Y' or 'N', default 'N'
    
    @Column(name = "conge_apres_annees", nullable = false)
    private Integer congeApresAnnees; // default 0
    
    @Column(name = "nb_annees_cumul_accepte", nullable = false)
    private Integer nbAnneesCumulAccepte; // default 0
    
    @Column(name = "generer_absence_dans_jours", nullable = false)
    private Integer genererAbsenceDansJours; // default 1
    
    @Column(name = "date_conge_genere")
    private LocalDate dateCongeGenere;
    
    @Column(name = "auto_activer_conge", nullable = false, length = 1)
    private String autoActiverConge; // 'Y' or 'N', default 'N'
    
    @Column(name = "auto_fermer_conge", nullable = false, length = 1)
    private String autoFermerConge; // 'Y' or 'N', default 'N'
    
    @Column(name = "matricule_assureur_defaut", length = 100)
    private String matriculeAssureurDefaut;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_mere_id")
    private Entreprise entrepriseMere;
    
    @Column(name = "actif", nullable = false, length = 1)
    private String actif; // 'Y' or 'N', default 'Y'
    
    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;
    
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
    
    public enum MoisDebutAnneeFiscale {
        JANVIER, FEVRIER, MARS, AVRIL, MAI, JUIN, JUILLET, AOUT, SEPTEMBRE, OCTOBRE, NOVEMBRE, DECEMBRE
    }
    
    public enum SecteurActivite {
        BANQUE, ASSURANCE, CROISIERE, DIVERTISSEMENT, TRANSPORT, AGROALIMENTAIRE, PETROLIER, COSMETIQUE, CONSTRUCTION, INDUSTRIE, TCI, HOTELERIE, GASTRONOMIE, COURRIER, PUBLICITE, COMMERCE, SANTE, EDUCATION, GOUVERNEMENT, AUTRE
    }
}
