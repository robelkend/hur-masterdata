package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interface_loading")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceLoading {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code_loading", nullable = false, unique = true, length = 100)
    private String codeLoading;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private Source source = Source.FILE;
    
    @Column(name = "exclus_derniere_ligne", nullable = false, length = 1)
    private String exclusDerniereLigne = "N"; // 'Y' or 'N'
    
    @Column(name = "separateur_champ", length = 10)
    private String separateurChamp;
    
    @Column(name = "delimiteur_champ", length = 10)
    private String delimiteurChamp;
    
    @Column(name = "exclus_lignes", nullable = false)
    private Integer exclusLignes = 0;
    
    @Column(name = "table_cible", nullable = false, length = 100)
    private String tableCible;
    
    @Column(name = "table_source", length = 255)
    private String tableSource;
    
    @Column(name = "extra_clause", columnDefinition = "TEXT")
    private String extraClause;

    @Column(name = "rdb_url", length = 255)
    private String rdbUrl;

    @Column(name = "rdb_driver", length = 120)
    private String rdbDriver;

    @Column(name = "rdb_username", length = 100)
    private String rdbUsername;

    @Column(name = "rdb_password", length = 200)
    private String rdbPassword;

    @Column(name = "rdb_schema", length = 120)
    private String rdbSchema;

    @Column(name = "rdb_query", columnDefinition = "TEXT")
    private String rdbQuery;

    @Column(name = "api_base_url", length = 255)
    private String apiBaseUrl;

    @Column(name = "api_endpoint", length = 255)
    private String apiEndpoint;

    @Column(name = "api_method", length = 10)
    private String apiMethod;

    @Column(name = "api_auth_type", length = 20)
    private String apiAuthType;

    @Column(name = "api_username", length = 100)
    private String apiUsername;

    @Column(name = "api_password", length = 200)
    private String apiPassword;

    @Column(name = "api_token", length = 500)
    private String apiToken;

    @Column(name = "api_headers", columnDefinition = "TEXT")
    private String apiHeaders;

    @Column(name = "api_query_params", columnDefinition = "TEXT")
    private String apiQueryParams;

    @Column(name = "api_body", columnDefinition = "TEXT")
    private String apiBody;

    @Column(name = "api_timeout_ms")
    private Integer apiTimeoutMs;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;
    
    @OneToMany(mappedBy = "loading", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterfaceLoadingChamp> champs = new ArrayList<>();
    
    // Audit fields
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_on", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdOn;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_on", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedOn;
    
    // Optimistic concurrency control
    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
    
    public enum Source {
        FILE, RDB, API
    }
}
