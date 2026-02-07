package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.DomaineCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.DomaineDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.DomaineUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Domaine;
import com.rsoft.hurmanagement.hurmasterdata.repository.DomaineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class DomaineService {
    
    private final DomaineRepository repository;
    
    @Transactional(readOnly = true)
    public Page<DomaineDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public DomaineDTO findById(Long id) {
        Domaine entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Domaine not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public DomaineDTO create(DomaineCreateDTO dto, String username) {
        Domaine entity = new Domaine();
        entity.setNom(dto.getNom());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public DomaineDTO update(Long id, DomaineUpdateDTO dto, String username) {
        Domaine entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Domaine not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setNom(dto.getNom());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        Domaine entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Domaine not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    @Transactional(readOnly = true)
    public java.util.List<DomaineDTO> findAllForDropdown() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private DomaineDTO toDTO(Domaine entity) {
        DomaineDTO dto = new DomaineDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
