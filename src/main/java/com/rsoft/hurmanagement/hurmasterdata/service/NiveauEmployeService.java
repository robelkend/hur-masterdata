package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.NiveauEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.NiveauEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.NiveauEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.NiveauEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.NiveauEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class NiveauEmployeService {
    
    private final NiveauEmployeRepository repository;
    
    @Transactional(readOnly = true)
    public Page<NiveauEmployeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public NiveauEmployeDTO findById(Long id) {
        NiveauEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("NiveauEmploye not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public NiveauEmployeDTO findByCodeNiveau(String codeNiveau) {
        NiveauEmploye entity = repository.findByCodeNiveau(codeNiveau)
                .orElseThrow(() -> new RuntimeException("NiveauEmploye not found with code: " + codeNiveau));
        return toDTO(entity);
    }
    
    @Transactional
    public NiveauEmployeDTO create(NiveauEmployeCreateDTO dto, String username) {
        if (repository.existsByCodeNiveau(dto.getCodeNiveau())) {
            throw new RuntimeException("NiveauEmploye with code " + dto.getCodeNiveau() + " already exists");
        }
        
        NiveauEmploye entity = new NiveauEmploye();
        entity.setCodeNiveau(dto.getCodeNiveau());
        entity.setDescription(dto.getDescription());
        entity.setNiveauHierarchique(dto.getNiveauHierarchique());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public NiveauEmployeDTO update(Long id, NiveauEmployeUpdateDTO dto, String username) {
        NiveauEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("NiveauEmploye not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setDescription(dto.getDescription());
        entity.setNiveauHierarchique(dto.getNiveauHierarchique());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        NiveauEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("NiveauEmploye not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private NiveauEmployeDTO toDTO(NiveauEmploye entity) {
        NiveauEmployeDTO dto = new NiveauEmployeDTO();
        dto.setId(entity.getId());
        dto.setCodeNiveau(entity.getCodeNiveau());
        dto.setDescription(entity.getDescription());
        dto.setNiveauHierarchique(entity.getNiveauHierarchique());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
