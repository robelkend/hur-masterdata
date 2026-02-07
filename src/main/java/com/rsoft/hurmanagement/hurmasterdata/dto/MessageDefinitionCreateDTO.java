package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDefinitionCreateDTO {
    @NotBlank(message = "Code message is required")
    @Size(max = 100, message = "Code message must not exceed 100 characters")
    private String codeMessage;
    
    @NotBlank(message = "Titre is required")
    @Size(max = 255, message = "Titre must not exceed 255 characters")
    private String titre;
    
    @NotBlank(message = "Langue is required")
    private String langue;
    
    @NotNull(message = "Frequence is required")
    private String frequence;
    
    private String emailEnvoye = "N";
    
    @NotNull(message = "Format is required")
    private String format;
    
    @NotBlank(message = "Contenu is required")
    private String contenu;
    
    private String actif = "Y";
    
    private Long entrepriseId;
    
    private List<MessageDestinataireCreateDTO> destinataires;
}
