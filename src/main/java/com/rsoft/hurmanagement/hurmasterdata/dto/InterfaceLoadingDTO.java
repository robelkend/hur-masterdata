package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoading;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceLoadingDTO {
    private Long id;
    private String codeLoading;
    private String description;
    private InterfaceLoading.Source source;
    private String exclusDerniereLigne;
    private String separateurChamp;
    private String delimiteurChamp;
    private Integer exclusLignes;
    private String tableCible;
    private String tableSource;
    private String extraClause;
    private String rdbUrl;
    private String rdbDriver;
    private String rdbUsername;
    private String rdbPassword;
    private String rdbSchema;
    private String rdbQuery;
    private String apiBaseUrl;
    private String apiEndpoint;
    private String apiMethod;
    private String apiAuthType;
    private String apiUsername;
    private String apiPassword;
    private String apiToken;
    private String apiHeaders;
    private String apiQueryParams;
    private String apiBody;
    private Integer apiTimeoutMs;
    private Long entrepriseId;
    private String entrepriseCode;
    private String entrepriseNom;
    private List<InterfaceLoadingChampDTO> champs;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
