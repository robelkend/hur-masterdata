package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.TypeRevenuCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeRevenuDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeRevenuUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.Formule;
import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePaie;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeRevenu;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.FormuleRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RubriquePaieRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeRevenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TypeRevenuService {
    
    private final TypeRevenuRepository repository;
    private final EntrepriseRepository entrepriseRepository;
    private final FormuleRepository formuleRepository;
    private final RubriquePaieRepository rubriquePaieRepository;
    
    @Transactional(readOnly = true)
    public Page<TypeRevenuDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<TypeRevenuDTO> findAllWithFilters(Long entrepriseId, Long rubriquePaieId, String actif, Pageable pageable) {
        return repository.findAllWithFilters(entrepriseId, rubriquePaieId, actif, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public TypeRevenuDTO findById(Long id) {
        TypeRevenu entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeRevenu not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public TypeRevenuDTO create(TypeRevenuCreateDTO dto, String username) {
        try {
            // Check uniqueness: code_revenu must be unique per entreprise
            if (repository.existsByCodeRevenuAndEntrepriseId(dto.getCodeRevenu(), dto.getEntrepriseId())) {
                throw new RuntimeException("TypeRevenu with code " + dto.getCodeRevenu() + " already exists for this entreprise");
            }
            
            TypeRevenu entity = new TypeRevenu();
            
            // Set entreprise if provided
            if (dto.getEntrepriseId() != null) {
                Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                        .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
                entity.setEntreprise(entreprise);
            }
            
            entity.setCodeRevenu(dto.getCodeRevenu());
            entity.setDescription(dto.getDescription());
            
            RubriquePaie rubriquePaie = rubriquePaieRepository.findById(dto.getRubriquePaieId())
                    .orElseThrow(() -> new RuntimeException("typeRevenu.error.rubriquePaieNotFound"));
            entity.setRubriquePaie(rubriquePaie);
            
            entity.setActif(dto.getActif());
            
            // Set formule if provided
            if (dto.getFormuleId() != null) {
                Formule formule = formuleRepository.findById(dto.getFormuleId())
                        .orElseThrow(() -> new RuntimeException("Formule not found with id: " + dto.getFormuleId()));
                entity.setFormule(formule);
            } else {
                entity.setFormule(null);
            }
            
            entity.setAjouterSalBase(dto.getAjouterSalBase() != null ? dto.getAjouterSalBase() : "N");
            entity.setHardcoded(dto.getHardcoded() != null ? dto.getHardcoded() : "N");
            entity.setCreatedBy(username);
            entity.setCreatedOn(OffsetDateTime.now());
            entity.setRowscn(1);
            
            return toDTO(repository.save(entity));
        } catch (RuntimeException e) {
            log.error("TypeRevenu create failed (codeRevenu={}): {}", dto.getCodeRevenu(), e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public TypeRevenuDTO update(Long id, TypeRevenuUpdateDTO dto, String username) {
        try {
            TypeRevenu entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("TypeRevenu not found with id: " + id));
            
            if (!entity.getRowscn().equals(dto.getRowscn())) {
                throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
            }
            
            // Check uniqueness: code_revenu must be unique per entreprise (excluding current record)
            if (repository.existsByCodeRevenuAndEntrepriseIdExcludingId(dto.getCodeRevenu(), dto.getEntrepriseId(), id)) {
                throw new RuntimeException("TypeRevenu with code " + dto.getCodeRevenu() + " already exists for this entreprise");
            }
            
            // Set entreprise if provided
            if (dto.getEntrepriseId() != null) {
                Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                        .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
                entity.setEntreprise(entreprise);
            } else {
                entity.setEntreprise(null);
            }
            
            entity.setCodeRevenu(dto.getCodeRevenu());
            entity.setDescription(dto.getDescription());
            
            RubriquePaie rubriquePaie = rubriquePaieRepository.findById(dto.getRubriquePaieId())
                    .orElseThrow(() -> new RuntimeException("typeRevenu.error.rubriquePaieNotFound"));
            entity.setRubriquePaie(rubriquePaie);
            
            entity.setActif(dto.getActif());
            
            // Set formule if provided
            if (dto.getFormuleId() != null) {
                Formule formule = formuleRepository.findById(dto.getFormuleId())
                        .orElseThrow(() -> new RuntimeException("Formule not found with id: " + dto.getFormuleId()));
                entity.setFormule(formule);
            } else {
                entity.setFormule(null);
            }
            
            entity.setAjouterSalBase(dto.getAjouterSalBase() != null ? dto.getAjouterSalBase() : "N");
            entity.setHardcoded(dto.getHardcoded() != null ? dto.getHardcoded() : entity.getHardcoded());
            entity.setUpdatedBy(username);
            entity.setUpdatedOn(OffsetDateTime.now());
            entity.setRowscn(entity.getRowscn() + 1);
            
            return toDTO(repository.save(entity));
        } catch (RuntimeException e) {
            log.error("TypeRevenu update failed (id={}): {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        TypeRevenu entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeRevenu not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        if ("Y".equalsIgnoreCase(entity.getHardcoded())) {
            throw new RuntimeException("typeRevenu.error.cannotDeleteHardcoded");
        }
        
        repository.delete(entity);
    }
    
    private TypeRevenuDTO toDTO(TypeRevenu entity) {
        TypeRevenuDTO dto = new TypeRevenuDTO();
        dto.setId(entity.getId());
        
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        
        dto.setCodeRevenu(entity.getCodeRevenu());
        dto.setDescription(entity.getDescription());
        if (entity.getRubriquePaie() != null) {
            dto.setRubriquePaieId(entity.getRubriquePaie().getId());
            dto.setRubriquePaieCode(entity.getRubriquePaie().getCodeRubrique());
            dto.setRubriquePaieLibelle(entity.getRubriquePaie().getLibelle());
        }
        dto.setActif(entity.getActif());
        
        if (entity.getFormule() != null) {
            dto.setFormuleId(entity.getFormule().getId());
            dto.setFormuleCodeVariable(entity.getFormule().getCodeVariable());
            dto.setFormuleDescription(entity.getFormule().getDescription());
        }
        
        dto.setAjouterSalBase(entity.getAjouterSalBase());
        dto.setHardcoded(entity.getHardcoded());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
