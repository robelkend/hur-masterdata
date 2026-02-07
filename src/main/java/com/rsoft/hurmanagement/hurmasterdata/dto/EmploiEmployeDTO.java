package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class EmploiEmployeDTO {
    private Long id;
    private Long employeId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motifFin;
    private String statutEmploi;
    private LocalDate dateFinStatut;
    private String typeContrat;
    private String tempsTravail;
    private Long typeEmployeId;
    private String typeEmployeDescription;
    private Long uniteOrganisationnelleId;
    private String uniteOrganisationnelleCode;
    private String uniteOrganisationnelleNom;
    private Long posteId;
    private String posteCode;
    private String posteDescription;
    private Long horaireId;
    private String horaireCode;
    private String horaireDescription;
    private String horaireGenererAbsence;
    private BigDecimal tauxSupplementaire;
    private Long fonctionId;
    private String fonctionCode;
    private String fonctionDescription;
    private Long gestionnaireId;
    private String gestionnaireCode;
    private String gestionnaireNom;
    private String gestionnairePrenom;
    private Long typeCongeId;
    private String typeCongeCode;
    private String typeCongeDescription;
    private String typeCongeAnnuel;
    private Integer jourOff1;
    private Integer jourOff2;
    private Integer jourOff3;
    private String enConge;
    private String enProbation;
    private String principal;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
