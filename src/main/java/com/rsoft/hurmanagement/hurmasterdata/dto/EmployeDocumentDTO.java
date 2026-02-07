package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class EmployeDocumentDTO {
    private Long id;
    private Long employeId;
    private String typeDocument;
    private String nomFichier;
    private String mimeType;
    private Long tailleOctets;
    private String storageRef;
    private String hashSha256;
    private LocalDate dateDocument;
    private String commentaire;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
