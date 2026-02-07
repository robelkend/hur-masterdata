package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaremeSanctionService {
    
    private final BaremeSanctionRepository repository;
    private final TypeEmployeRepository typeEmployeRepository;
    
    @Transactional(readOnly = true)
    public List<BaremeSanctionDTO> findByTypeEmployeId(Long typeEmployeId) {
        return repository.findByTypeEmployeId(typeEmployeId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public BaremeSanctionDTO create(BaremeSanctionCreateDTO dto, String username) {
        TypeEmploye typeEmploye = typeEmployeRepository.findById(dto.getTypeEmployeId())
                .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + dto.getTypeEmployeId()));
        
        // Validate seuil_max >= seuil_min if provided
        if (dto.getSeuilMax() != null && dto.getSeuilMax() < dto.getSeuilMin()) {
            throw new RuntimeException("seuil_max must be greater than or equal to seuil_min");
        }
        
        BaremeSanction entity = new BaremeSanction();
        entity.setTypeEmploye(typeEmploye);
        entity.setInfractionType(dto.getInfractionType());
        entity.setUniteInfraction(dto.getUniteInfraction());
        entity.setSeuilMin(dto.getSeuilMin());
        entity.setSeuilMax(dto.getSeuilMax());
        entity.setPenaliteMinutes(dto.getPenaliteMinutes());
        entity.setUnitePenalite(dto.getUnitePenalite());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        BaremeSanction saved = repository.save(entity);
        return toDTO(saved);
    }
    
    @Transactional
    public BaremeSanctionDTO update(Long id, BaremeSanctionUpdateDTO dto, String username) {
        BaremeSanction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("BaremeSanction not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Validate seuil_max >= seuil_min if provided
        if (dto.getSeuilMax() != null && dto.getSeuilMax() < dto.getSeuilMin()) {
            throw new RuntimeException("seuil_max must be greater than or equal to seuil_min");
        }
        
        entity.setInfractionType(dto.getInfractionType());
        entity.setUniteInfraction(dto.getUniteInfraction());
        entity.setSeuilMin(dto.getSeuilMin());
        entity.setSeuilMax(dto.getSeuilMax());
        entity.setPenaliteMinutes(dto.getPenaliteMinutes());
        entity.setUnitePenalite(dto.getUnitePenalite());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        BaremeSanction saved = repository.save(entity);
        return toDTO(saved);
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        BaremeSanction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("BaremeSanction not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private BaremeSanctionDTO toDTO(BaremeSanction entity) {
        BaremeSanctionDTO dto = new BaremeSanctionDTO();
        dto.setId(entity.getId());
        dto.setTypeEmployeId(entity.getTypeEmploye().getId());
        dto.setInfractionType(entity.getInfractionType());
        dto.setUniteInfraction(entity.getUniteInfraction());
        dto.setSeuilMin(entity.getSeuilMin());
        dto.setSeuilMax(entity.getSeuilMax());
        dto.setPenaliteMinutes(entity.getPenaliteMinutes());
        dto.setUnitePenalite(entity.getUnitePenalite());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
