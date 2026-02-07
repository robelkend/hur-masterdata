package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePrestation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RubriquePrestationUpdateDTO {
    @NotNull(message = "Rowscn is required for concurrency control")
    private Integer rowscn;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @NotNull(message = "Prelevement is required")
    private RubriquePrestation.Prelevement prelevement;

    private String hardcoded; // 'Y' or 'N', keep existing if null
}
