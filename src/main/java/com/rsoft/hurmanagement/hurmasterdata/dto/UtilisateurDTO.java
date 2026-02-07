package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class UtilisateurDTO {
    private Long id;
    private String identifiant;
    private String email;
    private String nom;
    private String prenom;
    private Utilisateur.Langue langue;
    private String actif;
    private LocalDate dateExpPassword;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
