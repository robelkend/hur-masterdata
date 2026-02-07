package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.TypeSanctionCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeSanctionDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeSanctionUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeSanction;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeSanctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TypeSanctionService {
    
    private final TypeSanctionRepository repository;
    
    @Transactional(readOnly = true)
    public Page<TypeSanctionDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public TypeSanctionDTO findById(Long id) {
        TypeSanction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeSanction not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public TypeSanctionDTO create(TypeSanctionCreateDTO dto, String username) {
        if (repository.existsByCodeSanction(dto.getCodeSanction())) {
            throw new RuntimeException("TypeSanction with code " + dto.getCodeSanction() + " already exists");
        }
        
        TypeSanction entity = new TypeSanction();
        entity.setCodeSanction(dto.getCodeSanction());
        entity.setDescription(dto.getDescription());
        entity.setGravite(dto.getGravite());
        entity.setCategorie(dto.getCategorie());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public TypeSanctionDTO update(Long id, TypeSanctionUpdateDTO dto, String username) {
        TypeSanction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeSanction not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setDescription(dto.getDescription());
        entity.setGravite(dto.getGravite());
        entity.setCategorie(dto.getCategorie());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        TypeSanction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeSanction not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private TypeSanctionDTO toDTO(TypeSanction entity) {
        TypeSanctionDTO dto = new TypeSanctionDTO();
        dto.setId(entity.getId());
        dto.setCodeSanction(entity.getCodeSanction());
        dto.setDescription(entity.getDescription());
        dto.setGravite(entity.getGravite());
        dto.setCategorie(entity.getCategorie());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
