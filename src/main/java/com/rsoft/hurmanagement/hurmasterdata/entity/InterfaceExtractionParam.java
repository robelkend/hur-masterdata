package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "interface_extraction_param", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"requete_id", "position"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceExtractionParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requete_id", nullable = false)
    private InterfaceExtractionRequete requete;

    @Column(name = "nom_param", nullable = false, length = 120)
    private String nomParam;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_param", nullable = false, length = 30)
    private TypeParam typeParam;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "obligatoire", nullable = false, length = 1)
    private String obligatoire = "Y"; // 'Y' or 'N'

    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y"; // 'Y' or 'N'

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

    public enum TypeParam {
        STRING, INTEGER, DECIMAL, DATE, BOOLEAN
    }
}
