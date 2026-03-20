package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceReportHeaderDTO {
    private Long entrepriseId;
    private String codeEntreprise;
    private String nomEntreprise;
    private String nomLegal;
    private String adresse;
    private String ville;
    private String etat;
    private String pays;
    private String codePostal;
    private String telephone1;
    private String telephone2;
    private String telephone3;
    private String fax;
    private String courriel;
    private String logoUrl;
    private String generatedAt;
    private String criteriaSummary;
}
