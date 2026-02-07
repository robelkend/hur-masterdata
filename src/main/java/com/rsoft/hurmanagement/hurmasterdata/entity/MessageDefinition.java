package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "message_definition")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message")
    private Long idMessage;

    @Column(name = "code_message", nullable = false, unique = true, length = 100)
    private String codeMessage;

    @Column(name = "titre", nullable = false, length = 255)
    private String titre;

    @Column(name = "langue", nullable = false, length = 10)
    private String langue; // en, fr, cr, es

    @Enumerated(EnumType.STRING)
    @Column(name = "frequence", nullable = false, length = 20)
    private Frequence frequence;

    @Column(name = "email_envoye", nullable = false, length = 1)
    private String emailEnvoye = "N"; // 'Y' or 'N'

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 20)
    private Format format;

    @Column(name = "contenu", nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "actif", nullable = false, length = 1)
    private String actif = "Y"; // 'Y' or 'N'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MessageDestinataire> destinataires = new ArrayList<>();

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

    public enum Frequence {
        SERVICE, UNE, JOUR, SEMAINE, MOIS, EVENEMENT
    }

    public enum Format {
        TEXT, HTML
    }
}
