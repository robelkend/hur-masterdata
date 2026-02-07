package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParamGenerationCodeEmployeCreateDTO {
    private Long entrepriseId;
    private Long typeEmployeId;
    
    @NotNull(message = "Date effectif is required")
    private LocalDate dateEffectif;
    
    private LocalDate dateFin;
    
    @NotNull(message = "Mode generation is required")
    private String modeGeneration;
    
    private Integer valeurDepart;
    private Integer valeurCourante;
    private Integer pasIncrementation = 1;
    private Integer longueurMin;
    private String paddingChar = "0";
    private String prefixeFixe;
    private String suffixeFixe;
    private String pattern;
    private String majuscules = "Y";
    private String enleverAccents = "Y";
    private String options = "{}"; // JSON string
    private String actif = "Y";
}
