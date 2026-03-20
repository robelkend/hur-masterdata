package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefCategorieMaterielCreateDTO {
    @NotBlank
    private String codeCategorie;

    @NotBlank
    private String libelle;
}
