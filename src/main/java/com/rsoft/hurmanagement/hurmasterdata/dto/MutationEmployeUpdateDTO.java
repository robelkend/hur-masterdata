package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MutationEmployeUpdateDTO {
    @NotNull(message = "Id is required")
    private Long id;
    
    @NotNull(message = "Employe is required")
    private Long employeId;
    
    private Long entrepriseId;
    
    @NotNull(message = "Type mutation is required")
    private String typeMutation;
    
    @NotNull(message = "Date effet is required")
    private LocalDate dateEffet;
    
    private LocalDate dateSaisie;
    
    @NotNull(message = "Statut is required")
    private String statut;
    
    private String motif;
    
    private String reference;
    
    private String avant;
    
    private String apres;
    
    @NotNull(message = "Rowscn is required")
    private Integer rowscn;
    
    // For form data (not stored in avant/apres directly, but used to generate them)
    private Long emploiEmployeAvantId;
    private Long emploiEmployeApresId;
    private Long salaireEmployeId;
    private BigDecimal montantSalaire;
    private BigDecimal tauxSupplementaire;
    private Long typeEmployeId;
    private Long uniteOrganisationnelleId;
    private Long posteId;
    private String typeContrat;
    private String tempsTravail;
    private Long horaireId;
    private Long fonctionId;
    private Long typeCongeId;
    private Long gestionnaireId;
    private Long regimePaieId;
    // Add other fields as needed based on mutation type
}
