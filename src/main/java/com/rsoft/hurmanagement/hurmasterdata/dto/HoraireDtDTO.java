package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class HoraireDtDTO {
    private Long id;
    private Long horaireId;
    private Integer jour;
    private String jourLibelle; // "Lundi", "Mardi", etc.
    private String heureDebutJour;
    private String heureFinJour;
    private String heureDebutNuit;
    private String heureFinNuit;
    private String heureDebutPause;
    private String heureFinPause;
    private String exigerPresence;
    private String heureFermetureAuto;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
