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
public class FamilleMetierUpdateDTO {
    
    @NotNull(message = "Row version (rowscn) is required for optimistic concurrency control")
    private Integer rowscn;
    
    @NotBlank(message = "Libelle is required")
    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;
    
    private String description;
    
    private Long domaineId;
    
    private Long niveauQualificationId;
}
