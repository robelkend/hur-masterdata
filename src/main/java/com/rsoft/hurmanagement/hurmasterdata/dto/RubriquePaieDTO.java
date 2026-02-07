package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePaie;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class RubriquePaieDTO {
    private Long id;
    private String codeRubrique;
    private String libelle;
    private RubriquePaie.TypeRubrique typeRubrique;
    private RubriquePaie.ModeCalcul modeCalcul;
    private String boni;
    private String prestation;
    private String imposable;
    private String preavis;
    private String taxesSpeciaux;
    private String soumisCotisations;
    private String hardcoded;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
