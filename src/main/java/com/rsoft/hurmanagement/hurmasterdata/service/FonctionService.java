package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.FonctionCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.FonctionDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.FonctionUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Fonction;
import com.rsoft.hurmanagement.hurmasterdata.repository.FonctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class FonctionService {
    
    private final FonctionRepository repository;
    
    @Transactional(readOnly = true)
    public Page<FonctionDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public FonctionDTO findById(Long id) {
        Fonction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fonction not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public FonctionDTO create(FonctionCreateDTO dto, String username) {
        if (repository.existsByCodeFonction(dto.getCodeFonction())) {
            throw new RuntimeException("Fonction with code " + dto.getCodeFonction() + " already exists");
        }
        
        Fonction entity = new Fonction();
        entity.setCodeFonction(dto.getCodeFonction());
        entity.setDescription(dto.getDescription());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public FonctionDTO update(Long id, FonctionUpdateDTO dto, String username) {
        Fonction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fonction not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setDescription(dto.getDescription());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        Fonction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fonction not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private FonctionDTO toDTO(Fonction entity) {
        FonctionDTO dto = new FonctionDTO();
        dto.setId(entity.getId());
        dto.setCodeFonction(entity.getCodeFonction());
        dto.setDescription(entity.getDescription());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
