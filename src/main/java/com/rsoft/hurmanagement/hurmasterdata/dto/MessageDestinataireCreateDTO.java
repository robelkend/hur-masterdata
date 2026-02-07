package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDestinataireCreateDTO {
    @NotNull(message = "Type cible is required")
    private String typeCible;
    
    @NotBlank(message = "Valeur cible is required")
    private String valeurCible;
    
    @NotNull(message = "Mode envoi is required")
    private String modeEnvoi;
}
