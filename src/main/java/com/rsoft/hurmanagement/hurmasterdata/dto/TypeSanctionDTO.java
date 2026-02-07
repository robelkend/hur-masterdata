package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.TypeSanction;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TypeSanctionDTO {
    private Long id;
    private String codeSanction;
    private String description;
    private TypeSanction.Gravite gravite;
    private TypeSanction.Categorie categorie;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
