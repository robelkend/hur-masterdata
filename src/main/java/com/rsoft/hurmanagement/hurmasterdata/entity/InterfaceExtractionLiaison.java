package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "interface_extraction_liaison", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"requete_fille_id", "param_position"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceExtractionLiaison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requete_fille_id", nullable = false)
    private InterfaceExtractionRequete requeteFille;

    @Column(name = "param_position", nullable = false)
    private Integer paramPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private SourceType sourceType;

    @Column(name = "source_valeur", nullable = false, length = 255)
    private String sourceValeur;

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

    public enum SourceType {
        PARENT_COL, EXTERNAL, CONSTANT
    }
}
