package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.AuditAccesDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.AuditAcces;
import com.rsoft.hurmanagement.hurmasterdata.repository.AuditAccesRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
@RequiredArgsConstructor
public class AuditAccesService {
    
    private final AuditAccesRepository repository;
    private final EntrepriseRepository entrepriseRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Transactional(readOnly = true)
    public Page<AuditAccesDTO> findAll(Pageable pageable) {
        // #region agent log
        try { Files.writeString(Path.of("c:\\My Datas\\Robelkend\\hurmagement\\dev\\rhp\\hurmanagementnew\\.cursor\\debug.log"),
                "{\"location\":\"AuditAccesService.java:27\",\"message\":\"findAll called\",\"data\":{},\"timestamp\":"+System.currentTimeMillis()+",\"runId\":\"run1\",\"hypothesisId\":\"H1\"}\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);} catch (Exception ignored) {}
        // #endregion agent log
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<AuditAccesDTO> findByDateRange(OffsetDateTime dateDebut, OffsetDateTime dateFin, Pageable pageable) {
        return repository.findByDateRange(dateDebut, dateFin, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<AuditAccesDTO> findByUtilisateur(String utilisateur, Pageable pageable) {
        return repository.findByUtilisateurOrderByDateEvenementDesc(utilisateur, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<AuditAccesDTO> findByTypeEvenement(String typeEvenement, Pageable pageable) {
        return repository.findByTypeEvenementOrderByDateEvenementDesc(typeEvenement, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<AuditAccesDTO> findByResultat(String resultat, Pageable pageable) {
        return repository.findByResultatOrderByDateEvenementDesc(resultat, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<AuditAccesDTO> findByEntrepriseId(Long entrepriseId, Pageable pageable) {
        return repository.findByEntrepriseIdOrderByDateEvenementDesc(entrepriseId, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AuditAccesDTO> findByFilters(OffsetDateTime dateDebut,
                                             OffsetDateTime dateFin,
                                             String utilisateur,
                                             AuditAcces.TypeEvenement typeEvenement,
                                             AuditAcces.Resultat resultat,
                                             Long entrepriseId,
                                             Pageable pageable) {
        // #region agent log
        try { Files.writeString(Path.of("c:\\My Datas\\Robelkend\\hurmagement\\dev\\rhp\\hurmanagementnew\\.cursor\\debug.log"),
                "{\"location\":\"AuditAccesService.java:60\",\"message\":\"findByFilters\",\"data\":{\"hasDateDebut\":"+(dateDebut!=null)+",\"hasDateFin\":"+(dateFin!=null)+",\"utilisateurProvided\":"+(utilisateur!=null)+",\"typeEvenement\":\""+(typeEvenement!=null?typeEvenement.name():"")+"\",\"resultat\":\""+(resultat!=null?resultat.name():"")+"\",\"hasEntrepriseId\":"+(entrepriseId!=null)+"},\"timestamp\":"+System.currentTimeMillis()+",\"runId\":\"run1\",\"hypothesisId\":\"H2\"}\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);} catch (Exception ignored) {}
        // #endregion agent log
        // #region agent log
        try {
            Object[] typeInfo = (Object[]) entityManager.createNativeQuery(
                    "select data_type, udt_name from information_schema.columns " +
                    "where table_name = 'audit_acces' and column_name = 'utilisateur'")
                    .getSingleResult();
            String dataType = typeInfo[0] != null ? typeInfo[0].toString() : "";
            String udtName = typeInfo[1] != null ? typeInfo[1].toString() : "";
            Files.writeString(Path.of("c:\\My Datas\\Robelkend\\hurmagement\\dev\\rhp\\hurmanagementnew\\.cursor\\debug.log"),
                    "{\"location\":\"AuditAccesService.java:76\",\"message\":\"utilisateur column type\",\"data\":{\"dataType\":\""+dataType+"\",\"udtName\":\""+udtName+"\"},\"timestamp\":"+System.currentTimeMillis()+",\"runId\":\"run1\",\"hypothesisId\":\"H1\"}\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ex) {
            try { Files.writeString(Path.of("c:\\My Datas\\Robelkend\\hurmagement\\dev\\rhp\\hurmanagementnew\\.cursor\\debug.log"),
                    "{\"location\":\"AuditAccesService.java:82\",\"message\":\"utilisateur column type error\",\"data\":{\"error\":\""+ex.getClass().getSimpleName()+"\"},\"timestamp\":"+System.currentTimeMillis()+",\"runId\":\"run1\",\"hypothesisId\":\"H1\"}\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);} catch (Exception ignored) {}
        }
        // #endregion agent log
        // #region agent log
        try {
            Object[] typeCheck = (Object[]) entityManager.createNativeQuery(
                    "select udt_name from information_schema.columns " +
                    "where table_name = 'audit_acces' and column_name = 'utilisateur'")
                    .getSingleResult();
            String pgType = typeCheck[0] != null ? typeCheck[0].toString() : "";
            String isBytea = String.valueOf("bytea".equalsIgnoreCase(pgType));
            Files.writeString(Path.of("c:\\My Datas\\Robelkend\\hurmagement\\dev\\rhp\\hurmanagementnew\\.cursor\\debug.log"),
                    "{\"location\":\"AuditAccesService.java:94\",\"message\":\"pg_typeof(utilisateur) check\",\"data\":{\"pgType\":\""+pgType+"\",\"isBytea\":\""+isBytea+"\"},\"timestamp\":"+System.currentTimeMillis()+",\"runId\":\"run1\",\"hypothesisId\":\"H2\"}\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ex) {
            try { Files.writeString(Path.of("c:\\My Datas\\Robelkend\\hurmagement\\dev\\rhp\\hurmanagementnew\\.cursor\\debug.log"),
                    "{\"location\":\"AuditAccesService.java:100\",\"message\":\"pg_typeof(utilisateur) check error\",\"data\":{\"error\":\""+ex.getClass().getSimpleName()+"\"},\"timestamp\":"+System.currentTimeMillis()+",\"runId\":\"run1\",\"hypothesisId\":\"H2\"}\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);} catch (Exception ignored) {}
        }
        // #endregion agent log
        return repository.findWithFilters(dateDebut, dateFin, utilisateur, typeEvenement, resultat, entrepriseId, pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public AuditAccesDTO findById(Long id) {
        AuditAcces entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AuditAcces not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public AuditAccesDTO create(AuditAcces entity) {
        AuditAcces saved = repository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public AuditAccesDTO createFromDto(AuditAccesDTO dto) {
        AuditAcces entity = new AuditAcces();
        entity.setDateEvenement(dto.getDateEvenement() != null ? dto.getDateEvenement() : OffsetDateTime.now());
        if (dto.getEntrepriseId() != null) {
            entity.setEntreprise(entrepriseRepository.findById(dto.getEntrepriseId()).orElse(null));
        }
        entity.setUtilisateur(dto.getUtilisateur());
        if (dto.getTypeEvenement() != null) {
            entity.setTypeEvenement(AuditAcces.TypeEvenement.valueOf(dto.getTypeEvenement()));
        }
        if (dto.getResultat() != null) {
            entity.setResultat(AuditAcces.Resultat.valueOf(dto.getResultat()));
        }
        entity.setResourceType(dto.getResourceType());
        entity.setResourceCode(dto.getResourceCode());
        entity.setActionCode(dto.getActionCode());
        entity.setCibleType(dto.getCibleType());
        entity.setCibleId(dto.getCibleId());
        entity.setIpAddress(dto.getIpAddress());
        entity.setUserAgent(dto.getUserAgent());
        entity.setSessionId(dto.getSessionId());
        entity.setRequestId(dto.getRequestId());
        entity.setDureeMs(dto.getDureeMs());
        entity.setDetails(dto.getDetails() != null ? dto.getDetails() : "{}");
        AuditAcces saved = repository.save(entity);
        return toDTO(saved);
    }
    
    private AuditAccesDTO toDTO(AuditAcces entity) {
        AuditAccesDTO dto = new AuditAccesDTO();
        dto.setId(entity.getId());
        dto.setDateEvenement(entity.getDateEvenement());
        dto.setEntrepriseId(entity.getEntreprise() != null ? entity.getEntreprise().getId() : null);
        dto.setEntrepriseCode(entity.getEntreprise() != null ? entity.getEntreprise().getCodeEntreprise() : null);
        dto.setEntrepriseNom(entity.getEntreprise() != null ? entity.getEntreprise().getNomEntreprise() : null);
        dto.setUtilisateur(entity.getUtilisateur());
        dto.setTypeEvenement(entity.getTypeEvenement().name());
        dto.setResultat(entity.getResultat().name());
        dto.setResourceType(entity.getResourceType());
        dto.setResourceCode(entity.getResourceCode());
        dto.setActionCode(entity.getActionCode());
        dto.setCibleType(entity.getCibleType());
        dto.setCibleId(entity.getCibleId());
        dto.setIpAddress(entity.getIpAddress());
        dto.setUserAgent(entity.getUserAgent());
        dto.setSessionId(entity.getSessionId());
        dto.setRequestId(entity.getRequestId());
        dto.setDureeMs(entity.getDureeMs());
        dto.setDetails(entity.getDetails());
        return dto;
    }
}
