package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GroupeEmployeCreateDTO {
    @NotBlank(message = "Code groupe is required")
    @Size(max = 50, message = "Code groupe must not exceed 50 characters")
    private String codeGroupe;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
