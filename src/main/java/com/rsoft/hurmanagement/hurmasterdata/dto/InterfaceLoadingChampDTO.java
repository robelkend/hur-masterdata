package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoadingChamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceLoadingChampDTO {
    private Long id;
    private Long loadingId;
    private String nomCible;
    private String nomSource;
    private InterfaceLoadingChamp.TypeDonnee typeDonnee;
    private Integer taille;
    private String format;
    private Integer position;
    private String valeur;
    private String updateChamp;
    private String updateValeur;
    private String updateCondition;
    private String obligatoire;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
