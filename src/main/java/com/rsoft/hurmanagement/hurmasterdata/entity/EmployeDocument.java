package com.rsoft.hurmanagement.hurmasterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "employe_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_document", nullable = false, length = 50)
    private TypeDocument typeDocument;
    
    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Column(name = "taille_octets")
    private Long tailleOctets;
    
    @Column(name = "storage_ref", columnDefinition = "TEXT")
    private String storageRef; // Storage path/key (S3, disk, blob)
    
    @Column(name = "hash_sha256", length = 64)
    private String hashSha256;
    
    @Column(name = "date_document")
    private LocalDate dateDocument;
    
    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;
    
    // Audit fields
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_on", nullable = false)
    private OffsetDateTime createdOn;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_on")
    private OffsetDateTime updatedOn;
    
    @Version
    @Column(name = "rowscn", nullable = false)
    private Integer rowscn;
    
    public enum TypeDocument {
        CONTRAT, CV, DIPLOME, PIECE_ID, AUTRE
    }
}
