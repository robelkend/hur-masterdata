package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "message_destinataire")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDestinataire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private MessageDefinition message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_cible", nullable = false, length = 20)
    private TypeCible typeCible;

    @Column(name = "valeur_cible", nullable = false, length = 255)
    private String valeurCible;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_envoi", nullable = false, length = 20)
    private ModeEnvoi modeEnvoi;

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

    public enum TypeCible {
        EMPLOYE, GROUPE, SERVICE, EMAIL
    }

    public enum ModeEnvoi {
        EMAIL, NOTIFICATION
    }
}
