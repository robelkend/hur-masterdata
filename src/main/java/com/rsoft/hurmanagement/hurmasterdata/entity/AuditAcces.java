package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_acces")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditAcces {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_evenement", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime dateEvenement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @Column(name = "utilisateur", length = 100)
    private String utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_evenement", nullable = false, length = 20)
    private TypeEvenement typeEvenement;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultat", nullable = false, length = 20)
    private Resultat resultat = Resultat.SUCCESS;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "resource_code", length = 255)
    private String resourceCode;

    @Column(name = "action_code", length = 50)
    private String actionCode;

    @Column(name = "cible_type", length = 50)
    private String cibleType;

    @Column(name = "cible_id", length = 255)
    private String cibleId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "request_id", length = 255)
    private String requestId;

    @Column(name = "duree_ms")
    private Integer dureeMs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", nullable = false, columnDefinition = "jsonb")
    private String details = "{}"; // JSONB stored as String

    public enum TypeEvenement {
        LOGIN, LOGOUT, PAGE_VIEW, ACTION, API_CALL, FAIL_LOGIN
    }

    public enum Resultat {
        SUCCESS, FAIL, DENY
    }
}
