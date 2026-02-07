package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurUpdateDTO {
    
    @NotNull(message = "Row version (rowscn) is required for optimistic concurrency control")
    private Integer rowscn;
    
    @NotBlank(message = "Identifiant is required")
    @Size(max = 100, message = "Identifiant must not exceed 100 characters")
    private String identifiant;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @NotBlank(message = "Nom is required")
    @Size(max = 255, message = "Nom must not exceed 255 characters")
    private String nom;
    
    @NotBlank(message = "Prenom is required")
    @Size(max = 255, message = "Prenom must not exceed 255 characters")
    private String prenom;
    
    @NotNull(message = "Langue is required")
    private Utilisateur.Langue langue;
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password; // Optional for update
    
    private Long entrepriseId;
}
