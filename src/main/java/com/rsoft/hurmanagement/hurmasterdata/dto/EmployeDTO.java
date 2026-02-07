package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class EmployeDTO {
    private Long id;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private String codeEmploye;
    private String matriculeInterne;
    private String nom;
    private String prenom;
    private String nomme;
    private Boolean nominationEligible;
    private LocalDate dateNaissance;
    private String paysNaissance;
    private String paysHabitation;
    private String sexe;
    private String etatCivil;
    private String nationalite;
    private String langue;
    private String courriel;
    private String telephone1;
    private String telephone2;
    private String photo;
    private LocalDate dateEmbauche;
    private LocalDate datePremiereEmbauche;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
    
    // Related data (can be loaded on demand)
    private List<EmployeAdresseDTO> adresses;
    private List<EmployeIdentiteDTO> identites;
    private List<EmployeContactDTO> contacts;
    private List<EmployeDocumentDTO> documents;
    private List<EmployeNoteDTO> notes;
    private List<CoordonneeBancaireEmployeDTO> coordonneesBancaires;
    private List<AssuranceEmployeDTO> assurances;
    private List<EmploiEmployeDTO> emplois;
    private List<EmployeSalaireDTO> salaires;
}
