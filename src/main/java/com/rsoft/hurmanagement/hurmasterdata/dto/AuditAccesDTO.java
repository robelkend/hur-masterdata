package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditAccesDTO {
    private Long id;
    private OffsetDateTime dateEvenement;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private String utilisateur;
    private String typeEvenement;
    private String resultat;
    private String resourceType;
    private String resourceCode;
    private String actionCode;
    private String cibleType;
    private String cibleId;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String requestId;
    private Integer dureeMs;
    private String details; // JSON string
}
