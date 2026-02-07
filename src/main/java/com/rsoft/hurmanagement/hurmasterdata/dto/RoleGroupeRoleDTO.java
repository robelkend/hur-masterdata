package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

@Data
public class RoleGroupeRoleDTO {
    private Long id;
    private Long groupeId;
    private Long roleId;
    private String roleCode;
    private String roleLibelle;
    private String actif;
    private Integer rowscn;
}
