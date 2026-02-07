package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.JourCongeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.JourCongeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.JourCongeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.JourConge;
import com.rsoft.hurmanagement.hurmasterdata.repository.JourCongeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class JourCongeService {
    
    private final JourCongeRepository repository;
    
    @Transactional(readOnly = true)
    public Page<JourCongeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public JourCongeDTO findById(Long id) {
        JourConge entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("JourConge not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public JourCongeDTO create(JourCongeCreateDTO dto, String username) {
        JourConge entity = new JourConge();
        entity.setType(dto.getType());
        entity.setDateConge(dto.getDateConge());
        entity.setDescription(dto.getDescription());
        entity.setMiJournee(dto.getMiJournee());
        entity.setActif(dto.getActif());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public JourCongeDTO update(Long id, JourCongeUpdateDTO dto, String username) {
        JourConge entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("JourConge not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setType(dto.getType());
        entity.setDateConge(dto.getDateConge());
        entity.setDescription(dto.getDescription());
        entity.setMiJournee(dto.getMiJournee());
        entity.setActif(dto.getActif());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        JourConge entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("JourConge not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private JourCongeDTO toDTO(JourConge entity) {
        JourCongeDTO dto = new JourCongeDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setDateConge(entity.getDateConge());
        dto.setDescription(entity.getDescription());
        dto.setMiJournee(entity.getMiJournee());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
