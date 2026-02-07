package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.AuditAccesDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.AuditAcces;
import com.rsoft.hurmanagement.hurmasterdata.repository.AuditAccesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditAccesService {
    
    private final AuditAccesRepository repository;
    
    @Transactional(readOnly = true)
    public Page<AuditAccesDTO> findAll(Pageable pageable) {
        return repository.findByOrderByDateEvenementDesc(pageable).map(this::toDTO);
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
