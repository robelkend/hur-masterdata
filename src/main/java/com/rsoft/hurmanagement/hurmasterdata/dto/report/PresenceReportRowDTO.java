package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceReportRowDTO {
    private Integer rang;
    private String codeEmploye;
    private String nomEmploye;
    private LocalDate dateDebut;
    private String heureDebut;
    private LocalDate dateFin;
    private String heureFin;
    private String nuit; // OUI/NON
    private String horaireSpecialNuit; // OUI/NON
    private String off; // OUI/NON
    private String ferie; // OUI/NON
    private String absence; // OUI/NON
    private BigDecimal nbHeures;
    private String refSup; // OFF/FERIE/CONGE/ABS/...
}
