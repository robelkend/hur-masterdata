package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ref_formule_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefFormuleToken {

    @Id
    @Column(name = "code_element", length = 100)
    private String codeElement;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_element", nullable = false, length = 30)
    private TypeElement typeElement;

    @Column(name = "symbole", nullable = false, length = 100)
    private String symbole;

    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;

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

    public enum TypeElement {
        OPERATEUR, OPERANDE, PARENTHESE, VARIABLE
    }
}
