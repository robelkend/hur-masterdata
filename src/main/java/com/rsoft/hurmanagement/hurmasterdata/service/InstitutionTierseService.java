package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.InstitutionTierseCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.InstitutionTierseDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.InstitutionTierseUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.InstitutionTierse;
import com.rsoft.hurmanagement.hurmasterdata.repository.InstitutionTierseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class InstitutionTierseService {
    
    private final InstitutionTierseRepository repository;
    
    @Transactional(readOnly = true)
    public Page<InstitutionTierseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public InstitutionTierseDTO findById(Long id) {
        InstitutionTierse entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("InstitutionTierse not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public InstitutionTierseDTO create(InstitutionTierseCreateDTO dto, String username) {
        if (repository.existsByCodeInstitution(dto.getCodeInstitution())) {
            throw new RuntimeException("InstitutionTierse with code " + dto.getCodeInstitution() + " already exists");
        }
        
        InstitutionTierse entity = new InstitutionTierse();
        entity.setCodeInstitution(dto.getCodeInstitution());
        entity.setNom(dto.getNom());
        entity.setReference(dto.getReference());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public InstitutionTierseDTO update(Long id, InstitutionTierseUpdateDTO dto, String username) {
        InstitutionTierse entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("InstitutionTierse not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setNom(dto.getNom());
        entity.setReference(dto.getReference());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        InstitutionTierse entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("InstitutionTierse not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private InstitutionTierseDTO toDTO(InstitutionTierse entity) {
        InstitutionTierseDTO dto = new InstitutionTierseDTO();
        dto.setId(entity.getId());
        dto.setCodeInstitution(entity.getCodeInstitution());
        dto.setNom(entity.getNom());
        dto.setReference(entity.getReference());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
