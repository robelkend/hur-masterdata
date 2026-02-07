package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.TypeUniteOrganisationnelleCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeUniteOrganisationnelleDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeUniteOrganisationnelleUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeUniteOrganisationnelle;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeUniteOrganisationnelleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TypeUniteOrganisationnelleService {
    
    private final TypeUniteOrganisationnelleRepository repository;
    
    @Transactional(readOnly = true)
    public Page<TypeUniteOrganisationnelleDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public TypeUniteOrganisationnelleDTO findById(Long id) {
        TypeUniteOrganisationnelle entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeUniteOrganisationnelle not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public java.util.List<TypeUniteOrganisationnelleDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Transactional
    public TypeUniteOrganisationnelleDTO create(TypeUniteOrganisationnelleCreateDTO dto, String username) {
        if (repository.existsByCode(dto.getCode())) {
            throw new RuntimeException("TypeUniteOrganisationnelle with code " + dto.getCode() + " already exists");
        }
        
        TypeUniteOrganisationnelle entity = new TypeUniteOrganisationnelle();
        entity.setCode(dto.getCode());
        entity.setLibelle(dto.getLibelle());
        entity.setNiveauHierarchique(dto.getNiveauHierarchique());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public TypeUniteOrganisationnelleDTO update(Long id, TypeUniteOrganisationnelleUpdateDTO dto, String username) {
        TypeUniteOrganisationnelle entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeUniteOrganisationnelle not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setLibelle(dto.getLibelle());
        entity.setNiveauHierarchique(dto.getNiveauHierarchique());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        TypeUniteOrganisationnelle entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeUniteOrganisationnelle not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private TypeUniteOrganisationnelleDTO toDTO(TypeUniteOrganisationnelle entity) {
        TypeUniteOrganisationnelleDTO dto = new TypeUniteOrganisationnelleDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setLibelle(entity.getLibelle());
        dto.setNiveauHierarchique(entity.getNiveauHierarchique());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
