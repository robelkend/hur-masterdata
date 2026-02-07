package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.NiveauQualificationCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.NiveauQualificationDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.NiveauQualificationUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.NiveauQualification;
import com.rsoft.hurmanagement.hurmasterdata.repository.NiveauQualificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NiveauQualificationService {
    
    private final NiveauQualificationRepository repository;
    
    @Transactional(readOnly = true)
    public Page<NiveauQualificationDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public NiveauQualificationDTO findById(Long id) {
        NiveauQualification entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("NiveauQualification not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public NiveauQualificationDTO findByCodeNiveau(String codeNiveau) {
        NiveauQualification entity = repository.findByCodeNiveau(codeNiveau)
                .orElseThrow(() -> new RuntimeException("NiveauQualification not found with code: " + codeNiveau));
        return toDTO(entity);
    }
    
    @Transactional
    public NiveauQualificationDTO create(NiveauQualificationCreateDTO dto, String username) {
        if (repository.existsByCodeNiveau(dto.getCodeNiveau())) {
            throw new RuntimeException("NiveauQualification with code " + dto.getCodeNiveau() + " already exists");
        }
        
        NiveauQualification entity = new NiveauQualification();
        entity.setCodeNiveau(dto.getCodeNiveau());
        entity.setDescription(dto.getDescription());
        entity.setNiveauHierarchique(dto.getNiveauHierarchique());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public NiveauQualificationDTO update(Long id, NiveauQualificationUpdateDTO dto, String username) {
        NiveauQualification entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("NiveauQualification not found with id: " + id));
        
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
        NiveauQualification entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("NiveauQualification not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    @Transactional(readOnly = true)
    public List<NiveauQualificationDTO> findAllForDropdown() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private NiveauQualificationDTO toDTO(NiveauQualification entity) {
        NiveauQualificationDTO dto = new NiveauQualificationDTO();
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
