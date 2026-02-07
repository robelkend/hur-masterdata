package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.ReferencePayrollCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ReferencePayrollDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ReferencePayrollUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.ReferencePayroll;
import com.rsoft.hurmanagement.hurmasterdata.repository.ReferencePayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ReferencePayrollService {
    
    private final ReferencePayrollRepository repository;
    
    @Transactional(readOnly = true)
    public Page<ReferencePayrollDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public ReferencePayrollDTO findById(Long id) {
        ReferencePayroll entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReferencePayroll not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public ReferencePayrollDTO create(ReferencePayrollCreateDTO dto, String username) {
        if (repository.existsByCodePayroll(dto.getCodePayroll())) {
            throw new RuntimeException("ReferencePayroll with code " + dto.getCodePayroll() + " already exists");
        }
        
        ReferencePayroll entity = new ReferencePayroll();
        entity.setCodePayroll(dto.getCodePayroll());
        entity.setDescription(dto.getDescription());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public ReferencePayrollDTO update(Long id, ReferencePayrollUpdateDTO dto, String username) {
        ReferencePayroll entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReferencePayroll not found with id: " + id));
        
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
        ReferencePayroll entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReferencePayroll not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private ReferencePayrollDTO toDTO(ReferencePayroll entity) {
        ReferencePayrollDTO dto = new ReferencePayrollDTO();
        dto.setId(entity.getId());
        dto.setCodePayroll(entity.getCodePayroll());
        dto.setDescription(entity.getDescription());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
