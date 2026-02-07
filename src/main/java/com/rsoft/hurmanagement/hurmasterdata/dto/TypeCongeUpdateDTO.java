package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.TypeConge;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TypeCongeUpdateDTO {
    @NotNull(message = "Rowscn is required for concurrency control")
    private Integer rowscn;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @NotNull(message = "Conge annuel is required")
    private TypeConge.CongeAnnuel congeAnnuel;
    
    @Min(value = 0, message = "Number of days must be non-negative")
    private Integer nbJours;
    
    @Min(value = 0, message = "Number of years for accumulation must be non-negative")
    private Integer nbAnneeCumul;
}
