package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FonctionCreateDTO {
    @NotBlank(message = "Code fonction is required")
    @Size(max = 50, message = "Code fonction must not exceed 50 characters")
    private String codeFonction;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
