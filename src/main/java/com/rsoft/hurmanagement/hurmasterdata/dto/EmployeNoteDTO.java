package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class EmployeNoteDTO {
    private Long id;
    private Long employeId;
    private String typeNote;
    private String titre;
    private String note;
    private String confidentiel;
    private String envoye;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
