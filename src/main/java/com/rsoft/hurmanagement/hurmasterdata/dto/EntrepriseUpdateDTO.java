package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EntrepriseUpdateDTO {
    @NotNull
    private Integer rowscn;
    
    private String nomEntreprise;
    
    private String nomLegal;
    
    private Long deviseId;
    
    private Entreprise.MoisDebutAnneeFiscale moisDebutAnneeFiscale;
    
    private String anneeFiscaleCourante;
    
    private Entreprise.SecteurActivite secteurActivite;
    
    private String etat;
    
    private String ville;
    
    private String adresse;

    private String codePostal;

    private String pays;
    
    private String telephone1;
    
    private String telephone2;

    private String telephone3;
    
    private String fax;
    
    @Email
    private String courriel;
    
    private String congeCumule;
    
    private Integer congeApresAnnees;
    
    private Integer nbAnneesCumulAccepte;
    
    private Integer genererAbsenceDansJours;
    
    private LocalDate dateCongeGenere;
    
    private String autoActiverConge;
    
    private String autoFermerConge;
    
    private String matriculeAssureurDefaut;
    
    private Long entrepriseMereId;
    
    private String actif;
    
    private String logoUrl;
}
