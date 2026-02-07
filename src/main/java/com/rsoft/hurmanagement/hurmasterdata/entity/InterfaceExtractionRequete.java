package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interface_extraction_requete")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceExtractionRequete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_extraction_id", nullable = false)
    private InterfaceExtraction interfaceExtraction;

    @Column(name = "script_sql", nullable = false, columnDefinition = "TEXT")
    private String scriptSql;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private InterfaceExtractionRequete parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InterfaceExtractionRequete> enfants = new ArrayList<>();

    @Column(name = "ordre_execution", nullable = false)
    private Integer ordreExecution = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_requete", nullable = false, length = 30)
    private TypeRequete typeRequete = TypeRequete.PRINCIPALE;

    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y"; // 'Y' or 'N'

    @OneToMany(mappedBy = "requete", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InterfaceExtractionParam> params = new ArrayList<>();

    @OneToMany(mappedBy = "requeteFille", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InterfaceExtractionLiaison> liaisons = new ArrayList<>();

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

    public enum TypeRequete {
        PRINCIPALE, SOUS_REQUETE, POST_TRAITEMENT
    }
}
