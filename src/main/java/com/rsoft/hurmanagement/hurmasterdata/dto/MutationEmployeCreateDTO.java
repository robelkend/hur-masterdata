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
public class MutationEmployeCreateDTO {
    @NotNull(message = "Employe is required")
    private Long employeId;
    
    private Long entrepriseId;
    
    @NotNull(message = "Type mutation is required")
    private String typeMutation; // CHG_POSTE, CHG_UNITE, PROMOTION, REVISION_SALAIRE, DEMISSION, LICENCIEMENT, FIN_CONTRAT, ABANDON_POSTE, SUSPENSION, REINTEGRATION
    
    @NotNull(message = "Date effet is required")
    private LocalDate dateEffet;
    
    private LocalDate dateSaisie;
    
    private String statut = "BROUILLON"; // BROUILLON, SOUMIS, REJETE, APPROUVE, APPLIQUE, ANNULE
    
    private String motif;
    
    private String reference;
    
    // For form data (not stored in avant/apres directly, but used to generate them)
    private Long emploiEmployeAvantId; // For DEMISSION, LICENCIEMENT, etc.
    private Long emploiEmployeApresId; // For REINTEGRATION
    private Long salaireEmployeId; // For REVISION_SALAIRE
    private BigDecimal montantSalaire; // For REVISION_SALAIRE (apres)
    private BigDecimal tauxSupplementaire; // For REVISION_SALAIRE (apres)
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
