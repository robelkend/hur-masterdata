package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceExtractionCreateDTO {
    @NotBlank(message = "Code extraction is required")
    @Size(max = 100, message = "Code extraction must not exceed 100 characters")
    private String codeExtraction;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @Size(max = 10, message = "Separateur must not exceed 10 characters")
    private String separateur;
    
    @Size(max = 10, message = "Encadreur must not exceed 10 characters")
    private String encadreur;
    
    private String actif = "N";
    
    private Long entrepriseId;
    
    private List<InterfaceExtractionRequeteCreateDTO> requetes;
}
