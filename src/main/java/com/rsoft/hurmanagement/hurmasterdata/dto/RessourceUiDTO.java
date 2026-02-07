package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.RessourceUi;
import lombok.Data;

@Data
public class RessourceUiDTO {
    private Long id;
    private String codeResource;
    private String libelle;
    private RessourceUi.TypeResource typeResource;
    private Long parentId;
    private String estMenu;
    private String actif;
}
