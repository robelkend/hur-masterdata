package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import lombok.Data;

@Data
public class AuthUserDTO {
    private Long id;
    private String identifiant;
    private String email;
    private String nom;
    private String prenom;
    private Utilisateur.Langue langue;
    private Long entrepriseId;
    private String entrepriseNom;
}
