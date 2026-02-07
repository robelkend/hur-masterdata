package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RolePermission;
import lombok.Data;

@Data
public class RolePermissionDTO {
    private Long id;
    private Long roleId;
    private Long ressourceId;
    private String ressourceCode;
    private String ressourceLibelle;
    private Long actionId;
    private String actionCode;
    private String actionLibelle;
    private RolePermission.Effet effet;
    private Boolean heritageDescendant;
    private String actif;
    private Integer rowscn;
}
