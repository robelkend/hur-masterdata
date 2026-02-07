package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
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
public class AppRoleService {
    
    private final AppRoleRepository repository;
    
    @Transactional(readOnly = true)
    public Page<AppRoleDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public AppRoleDTO findById(Long id) {
        AppRole entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AppRole not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<AppRoleDTO> findAllForDropdown() {
        return repository.findAllByOrderByCodeRoleAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public AppRoleDTO create(AppRoleCreateDTO dto, String username) {
        // Check if code already exists
        if (repository.existsByCodeRole(dto.getCodeRole())) {
            throw new RuntimeException("Code role already exists: " + dto.getCodeRole());
        }
        
        AppRole entity = new AppRole();
        entity.setCodeRole(dto.getCodeRole());
        entity.setLibelle(dto.getLibelle());
        entity.setActif("N"); // Default inactive
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public AppRoleDTO update(Long id, AppRoleUpdateDTO dto, String username) {
        AppRole entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AppRole not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Check if code changed and already exists
        if (!entity.getCodeRole().equals(dto.getCodeRole()) && 
            repository.existsByCodeRole(dto.getCodeRole())) {
            throw new RuntimeException("Code role already exists: " + dto.getCodeRole());
        }
        
        entity.setCodeRole(dto.getCodeRole());
        entity.setLibelle(dto.getLibelle());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        AppRole entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AppRole not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    @Transactional
    public AppRoleDTO toggleActif(Long id, Integer rowscn, String username) {
        AppRole entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AppRole not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setActif("Y".equals(entity.getActif()) ? "N" : "Y");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    private AppRoleDTO toDTO(AppRole entity) {
        AppRoleDTO dto = new AppRoleDTO();
        dto.setId(entity.getId());
        dto.setCodeRole(entity.getCodeRole());
        dto.setLibelle(entity.getLibelle());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
