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
public class TypePieceUpdateDTO {
    @NotNull(message = "Rowscn is required")
    private Integer rowscn;

    @NotNull(message = "Entreprise is required")
    private Long entrepriseId;

    @NotBlank(message = "Libelle is required")
    @Size(max = 255, message = "Libelle must not exceed 255 characters")
    private String libelle;

    private String actif; // 'Y' or 'N'
}
