package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

@Data
public class GroupeRoleUtilisateurDTO {
    private Long id;
    private Long utilisateurId;
    private Long groupeId;
    private String groupeCode;
    private String groupeLibelle;
    private String estPrimaire;
    private Integer rowscn;
}
