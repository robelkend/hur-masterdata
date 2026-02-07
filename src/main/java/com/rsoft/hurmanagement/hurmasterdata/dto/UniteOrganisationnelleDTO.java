package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class UniteOrganisationnelleDTO {
    private Long id;
    private String code;
    private String nom;
    
    // Type Unite Organisationnelle
    private Long typeUniteOrganisationnelleId;
    private String typeUniteOrganisationnelleCode;
    private String typeUniteOrganisationnelleLibelle;
    
    // Unite Parente
    private Long uniteParenteId;
    private String uniteParenteCode;
    private String uniteParenteNom;
    
    // Responsable Employe
    private Long responsableEmployeId;
    private String responsableEmployeCode;
    private String responsableEmployeNom;
    private String responsableEmployePrenom;
    
    // Contact fields
    private String email;
    private String telephone1;
    private String telephone2;
    private String extensionTelephone;
    
    // Status
    private String actif;
    
    // Dates
    private LocalDate dateDebutEffet;
    private LocalDate dateFinEffet;
    
    // Audit fields
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
