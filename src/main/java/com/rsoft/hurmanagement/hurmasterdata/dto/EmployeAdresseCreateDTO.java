package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeAdresseCreateDTO {
    @NotNull(message = "Type adresse is required")
    private String typeAdresse; // DOMICILE, POSTALE
    
    @NotBlank(message = "Ligne1 is required")
    @Size(max = 255, message = "Ligne1 must not exceed 255 characters")
    private String ligne1;
    
    @Size(max = 255, message = "Ligne2 must not exceed 255 characters")
    private String ligne2;
    
    @NotBlank(message = "Ville is required")
    @Size(max = 100, message = "Ville must not exceed 100 characters")
    private String ville;
    
    @Size(max = 100, message = "Etat must not exceed 100 characters")
    private String etat;
    
    @Size(max = 20, message = "Code postal must not exceed 20 characters")
    private String codePostal;
    
    @Size(max = 2, message = "Pays must be ISO country code (2 characters)")
    private String pays;
    
    @NotNull(message = "Date debut is required")
    private LocalDate dateDebut;
    
    private LocalDate dateFin;
    
    private String actif = "Y"; // 'Y' or 'N'
}
