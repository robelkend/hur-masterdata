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
public class RoleGroupeService {
    
    private final RoleGroupeRepository repository;
    
    @Transactional(readOnly = true)
    public Page<RoleGroupeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public RoleGroupeDTO findById(Long id) {
        RoleGroupe entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoleGroupe not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<RoleGroupeDTO> findAllForDropdown() {
        return repository.findAllByOrderByCodeGroupeAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RoleGroupeDTO create(RoleGroupeCreateDTO dto, String username) {
        // Check if code already exists
        if (repository.existsByCodeGroupe(dto.getCodeGroupe())) {
            throw new RuntimeException("Code groupe already exists: " + dto.getCodeGroupe());
        }
        
        RoleGroupe entity = new RoleGroupe();
        entity.setCodeGroupe(dto.getCodeGroupe());
        entity.setLibelle(dto.getLibelle());
        entity.setActif("N"); // Default inactive
        entity.setAllAccess(dto.getAllAccess() == null ? "N" : dto.getAllAccess());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public RoleGroupeDTO update(Long id, RoleGroupeUpdateDTO dto, String username) {
        RoleGroupe entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoleGroupe not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Check if code changed and already exists
        if (!entity.getCodeGroupe().equals(dto.getCodeGroupe()) && 
            repository.existsByCodeGroupe(dto.getCodeGroupe())) {
            throw new RuntimeException("Code groupe already exists: " + dto.getCodeGroupe());
        }
        
        entity.setCodeGroupe(dto.getCodeGroupe());
        entity.setLibelle(dto.getLibelle());
        entity.setAllAccess(dto.getAllAccess() == null ? "N" : dto.getAllAccess());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        RoleGroupe entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoleGroupe not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    @Transactional
    public RoleGroupeDTO toggleActif(Long id, Integer rowscn, String username) {
        RoleGroupe entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoleGroupe not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setActif("Y".equals(entity.getActif()) ? "N" : "Y");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    private RoleGroupeDTO toDTO(RoleGroupe entity) {
        RoleGroupeDTO dto = new RoleGroupeDTO();
        dto.setId(entity.getId());
        dto.setCodeGroupe(entity.getCodeGroupe());
        dto.setLibelle(entity.getLibelle());
        dto.setActif(entity.getActif());
        dto.setAllAccess(entity.getAllAccess());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
