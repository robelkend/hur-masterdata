package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class EntrepriseDTO {
    private Long id;
    private String codeEntreprise;
    private String nomEntreprise;
    private String nomLegal;
    private Long deviseId;
    private String deviseCode;
    private String deviseDescription;
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
    private String entrepriseMereCode;
    private String entrepriseMereNom;
    private String actif;
    private String logoUrl;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
