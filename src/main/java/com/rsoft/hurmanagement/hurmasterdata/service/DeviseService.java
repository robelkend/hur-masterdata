package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.DeviseCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.DeviseDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.DeviseUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Devise;
import com.rsoft.hurmanagement.hurmasterdata.repository.DeviseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class DeviseService {
    
    private final DeviseRepository repository;
    
    @Transactional(readOnly = true)
    public Page<DeviseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public DeviseDTO findById(Long id) {
        Devise entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public DeviseDTO create(DeviseCreateDTO dto, String username) {
        if (repository.existsByCodeDevise(dto.getCodeDevise())) {
            throw new RuntimeException("Devise with code " + dto.getCodeDevise() + " already exists");
        }
        
        Devise entity = new Devise();
        entity.setCodeDevise(dto.getCodeDevise());
        entity.setDescription(dto.getDescription());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public DeviseDTO update(Long id, DeviseUpdateDTO dto, String username) {
        Devise entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + id));
        
        // Optimistic concurrency check
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
        Devise entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    @Transactional(readOnly = true)
    public java.util.List<DeviseDTO> findAllForDropdown() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private DeviseDTO toDTO(Devise entity) {
        DeviseDTO dto = new DeviseDTO();
        dto.setId(entity.getId());
        dto.setCodeDevise(entity.getCodeDevise());
        dto.setDescription(entity.getDescription());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
