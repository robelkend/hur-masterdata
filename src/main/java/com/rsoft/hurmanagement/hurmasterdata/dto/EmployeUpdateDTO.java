package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeUpdateDTO {
    @NotNull(message = "Rowscn is required for concurrency control")
    private Integer rowscn;
    
    @Size(max = 50, message = "Matricule interne must not exceed 50 characters")
    private String matriculeInterne;
    
    @NotBlank(message = "Nom is required")
    @Size(max = 255, message = "Nom must not exceed 255 characters")
    private String nom;
    
    @NotBlank(message = "Prenom is required")
    @Size(max = 255, message = "Prenom must not exceed 255 characters")
    private String prenom;

    @Size(max = 1, message = "Nomme must be Y or N")
    private String nomme;
    
    private LocalDate dateNaissance;
    
    @Size(max = 2, message = "Pays naissance must be ISO country code (2 characters)")
    private String paysNaissance;
    
    @Size(max = 2, message = "Pays habitation must be ISO country code (2 characters)")
    private String paysHabitation;
    
    @Size(max = 1, message = "Sexe must be M or F")
    private String sexe;
    
    @Size(max = 50, message = "Etat civil must not exceed 50 characters")
    private String etatCivil;
    
    @Size(max = 2, message = "Nationalite must be ISO country code (2 characters)")
    private String nationalite;
    
    @Size(max = 2, message = "Langue must be en, fr, es, or ht")
    private String langue;
    
    @Email(message = "Courriel must be a valid email address")
    @Size(max = 255, message = "Courriel must not exceed 255 characters")
    private String courriel;
    
    @Size(max = 50, message = "Telephone1 must not exceed 50 characters")
    private String telephone1;
    
    @Size(max = 50, message = "Telephone2 must not exceed 50 characters")
    private String telephone2;
    
    private String photo; // Base64 or URL
}
