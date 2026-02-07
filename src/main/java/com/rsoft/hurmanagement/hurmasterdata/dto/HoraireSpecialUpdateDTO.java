package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoraireSpecialUpdateDTO {
    @NotNull(message = "Id is required")
    private Long id;
    
    @NotNull(message = "Employe is required")
    private Long employeId;

    private Long emploiEmployeId;
    
    @NotNull(message = "Date debut is required")
    private LocalDate dateDebut;
    
    private LocalDate dateFin;
    
    private String heureDebut; // Format HH:mi
    
    private String heureFin; // Format HH:mi
    
    @NotNull(message = "Priorite is required")
    private String priorite; // MINEURE, MAJEURE
    
    @NotNull(message = "Frequence is required")
    private String frequence; // JOUR, SEMAINE, QUINZAINE, MOIS
    
    @NotNull(message = "Unite frequence is required")
    @Min(value = 0, message = "Unite frequence must be >= 0")
    private Integer uniteFreq;
    
    @NotNull(message = "Actif is required")
    private String actif; // 'Y' or 'N'

    private String duplique; // 'Y' or 'N'
    
    @NotNull(message = "Rowscn is required")
    private Integer rowscn;
}
