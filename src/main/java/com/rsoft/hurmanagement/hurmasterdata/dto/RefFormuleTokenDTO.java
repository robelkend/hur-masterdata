package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RefFormuleToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefFormuleTokenDTO {
    private String codeElement;
    private RefFormuleToken.TypeElement typeElement;
    private String symbole;
    private String libelle;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
