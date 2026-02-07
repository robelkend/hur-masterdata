package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDefinitionDTO {
    private Long idMessage;
    private String codeMessage;
    private String titre;
    private String langue;
    private String frequence;
    private String emailEnvoye;
    private String format;
    private String contenu;
    private String actif;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private List<MessageDestinataireDTO> destinataires;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
