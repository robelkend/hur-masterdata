package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.JourConge;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JourCongeUpdateDTO {
    
    @NotNull(message = "Row version (rowscn) is required for optimistic concurrency control")
    private Integer rowscn;
    
    @NotNull(message = "Type is required")
    private JourConge.Type type;
    
    @NotNull(message = "Date conge is required")
    private LocalDate dateConge;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @NotNull(message = "Mi journee is required")
    private JourConge.MiJournee miJournee;
    
    @NotNull(message = "Actif is required")
    private JourConge.Actif actif;
}
