package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessusParametreCreateDTO {
    @NotBlank(message = "Code processus is required")
    @Size(max = 100, message = "Code processus must not exceed 100 characters")
    private String codeProcessus;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    private String actif = "N";
    
    @NotNull(message = "Frequence is required")
    private String frequence = "JOUR";
    
    @NotNull(message = "Nombre is required")
    private Integer nombre = 1;
    
    @NotNull(message = "Marge is required")
    private Integer marge = 0;
    
    @NotNull(message = "Unite marge is required")
    private String uniteMarge = "JOUR";
    
    private Long entrepriseId;
}
