package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePrestation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RubriquePrestationCreateDTO {
    @NotBlank(message = "Code prestation is required")
    @Size(max = 50, message = "Code prestation must not exceed 50 characters")
    private String codePrestation;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @NotNull(message = "Prelevement is required")
    private RubriquePrestation.Prelevement prelevement;

    private String hardcoded; // 'Y' or 'N', default 'N' if null
}
