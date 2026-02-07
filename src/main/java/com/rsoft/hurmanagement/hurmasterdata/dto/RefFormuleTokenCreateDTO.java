package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RefFormuleToken;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefFormuleTokenCreateDTO {
    @NotBlank(message = "Code element is required")
    @Size(max = 100, message = "Code element must not exceed 100 characters")
    private String codeElement;
    
    @NotNull(message = "Type element is required")
    private RefFormuleToken.TypeElement typeElement;
    
    @NotBlank(message = "Symbole is required")
    @Size(max = 100, message = "Symbole must not exceed 100 characters")
    private String symbole;
    
    @NotBlank(message = "Libelle is required")
    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;
}
