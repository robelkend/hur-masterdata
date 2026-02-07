package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.FamilleMetierCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.FamilleMetierDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.FamilleMetierUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Domaine;
import com.rsoft.hurmanagement.hurmasterdata.entity.FamilleMetier;
import com.rsoft.hurmanagement.hurmasterdata.entity.NiveauQualification;
import com.rsoft.hurmanagement.hurmasterdata.repository.DomaineRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.FamilleMetierRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.NiveauQualificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class FamilleMetierService {
    
    private final FamilleMetierRepository repository;
    private final DomaineRepository domaineRepository;
    private final NiveauQualificationRepository niveauQualificationRepository;
    
    @Transactional(readOnly = true)
    public Page<FamilleMetierDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public FamilleMetierDTO findById(Long id) {
        FamilleMetier entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("FamilleMetier not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public FamilleMetierDTO create(FamilleMetierCreateDTO dto, String username) {
        if (repository.existsByCodeFamilleMetier(dto.getCodeFamilleMetier())) {
            throw new RuntimeException("FamilleMetier with code " + dto.getCodeFamilleMetier() + " already exists");
        }
        
        FamilleMetier entity = new FamilleMetier();
        entity.setCodeFamilleMetier(dto.getCodeFamilleMetier());
        entity.setLibelle(dto.getLibelle());
        entity.setDescription(dto.getDescription());
        
        if (dto.getDomaineId() != null) {
            Domaine domaine = domaineRepository.findById(dto.getDomaineId())
                    .orElseThrow(() -> new RuntimeException("Domaine not found with id: " + dto.getDomaineId()));
            entity.setDomaine(domaine);
        }
        
        if (dto.getNiveauQualificationId() != null) {
            NiveauQualification niveauQualification = niveauQualificationRepository.findById(dto.getNiveauQualificationId())
                    .orElseThrow(() -> new RuntimeException("NiveauQualification not found with id: " + dto.getNiveauQualificationId()));
            entity.setNiveauQualification(niveauQualification);
        }
        
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public FamilleMetierDTO update(Long id, FamilleMetierUpdateDTO dto, String username) {
        FamilleMetier entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("FamilleMetier not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setLibelle(dto.getLibelle());
        entity.setDescription(dto.getDescription());
        
        if (dto.getDomaineId() != null) {
            Domaine domaine = domaineRepository.findById(dto.getDomaineId())
                    .orElseThrow(() -> new RuntimeException("Domaine not found with id: " + dto.getDomaineId()));
            entity.setDomaine(domaine);
        } else {
            entity.setDomaine(null);
        }
        
        if (dto.getNiveauQualificationId() != null) {
            NiveauQualification niveauQualification = niveauQualificationRepository.findById(dto.getNiveauQualificationId())
                    .orElseThrow(() -> new RuntimeException("NiveauQualification not found with id: " + dto.getNiveauQualificationId()));
            entity.setNiveauQualification(niveauQualification);
        } else {
            entity.setNiveauQualification(null);
        }
        
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        FamilleMetier entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("FamilleMetier not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    @Transactional(readOnly = true)
    public java.util.List<FamilleMetierDTO> findAllForDropdown() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private FamilleMetierDTO toDTO(FamilleMetier entity) {
        FamilleMetierDTO dto = new FamilleMetierDTO();
        dto.setId(entity.getId());
        dto.setCodeFamilleMetier(entity.getCodeFamilleMetier());
        dto.setLibelle(entity.getLibelle());
        dto.setDescription(entity.getDescription());
        
        if (entity.getDomaine() != null) {
            dto.setDomaineId(entity.getDomaine().getId());
            dto.setDomaineNom(entity.getDomaine().getNom());
        }
        
        if (entity.getNiveauQualification() != null) {
            dto.setNiveauQualificationId(entity.getNiveauQualification().getId());
            dto.setNiveauQualificationNom(entity.getNiveauQualification().getDescription());
            dto.setNiveauQualificationNiveau(entity.getNiveauQualification().getNiveauHierarchique());
        }
        
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
