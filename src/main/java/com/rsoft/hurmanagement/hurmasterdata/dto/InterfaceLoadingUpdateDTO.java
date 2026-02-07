package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoading;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceLoadingUpdateDTO {
    
    @NotNull(message = "ID is required")
    private Long id;
    
    @NotBlank(message = "Code loading is required")
    @Size(max = 100, message = "Code loading must not exceed 100 characters")
    private String codeLoading;
    
    private String description;
    
    @NotNull(message = "Source is required")
    private InterfaceLoading.Source source;
    
    private String exclusDerniereLigne;
    
    @Size(max = 10, message = "Separateur champ must not exceed 10 characters")
    private String separateurChamp;
    
    @Size(max = 10, message = "Delimiteur champ must not exceed 10 characters")
    private String delimiteurChamp;
    
    @NotNull(message = "Exclus lignes is required")
    private Integer exclusLignes;
    
    @NotBlank(message = "Table cible is required")
    @Size(max = 100, message = "Table cible must not exceed 100 characters")
    private String tableCible;
    
    @Size(max = 100, message = "Table source must not exceed 100 characters")
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
    
    private List<InterfaceLoadingChampUpdateDTO> champs;
    
    @NotNull(message = "Row version is required")
    private Integer rowscn;
}
