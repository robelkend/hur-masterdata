package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RolePermission;
import lombok.Data;

@Data
public class RolePermissionCreateDTO {
    private Long ressourceId;
    private Long actionId;
    private RolePermission.Effet effet;
    private Boolean heritageDescendant;
    private String actif;
}
