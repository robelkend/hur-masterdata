package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AbsenceEmployeCreateDTO {
    @NotNull
    private Long employeId;
    private Long emploiEmployeId;
    @NotNull
    private String typeEvenement;
    @NotNull
    private LocalDate dateJour;
    @Size(max = 5)
    private String heureDebut;
    @Size(max = 5)
    private String heureFin;
    private String uniteMesure;
    private BigDecimal quantite;
    private BigDecimal montantEquivalent;
    private Long payrollId;
    private String justificatif;
    @Size(max = 80)
    private String motif;
    private String source;
}
