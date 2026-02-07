package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UniteOrganisationnelleUpdateDTO {
    
    @NotNull(message = "Rowscn is required for concurrency control")
    private Integer rowscn;
    
    @NotBlank(message = "Nom is required")
    @Size(max = 255, message = "Nom must not exceed 255 characters")
    private String nom;
    
    @NotNull(message = "Type unite organisationnelle is required")
    private Long typeUniteOrganisationnelleId;
    
    private Long uniteParenteId;
    
    private Long responsableEmployeId;
    
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 50, message = "Telephone 1 must not exceed 50 characters")
    private String telephone1;
    
    @Size(max = 50, message = "Telephone 2 must not exceed 50 characters")
    private String telephone2;
    
    @Size(max = 20, message = "Extension telephone must not exceed 20 characters")
    private String extensionTelephone;
    
    @Pattern(regexp = "^[YN]$", message = "Actif must be 'Y' or 'N'")
    private String actif;
    
    private LocalDate dateDebutEffet;
    
    private LocalDate dateFinEffet;
}
