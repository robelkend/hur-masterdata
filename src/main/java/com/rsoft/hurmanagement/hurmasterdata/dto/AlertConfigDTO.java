package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertConfigDTO {
    private Long id;
    private String codeMessage;
    private String messageTitre;
    private String email;
    private String pushAlert;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
