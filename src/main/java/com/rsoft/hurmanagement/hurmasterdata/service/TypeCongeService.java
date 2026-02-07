package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.TypeCongeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeCongeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeCongeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeConge;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeCongeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeCongeService {
    
    private final TypeCongeRepository repository;
    
    @Transactional(readOnly = true)
    public Page<TypeCongeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public TypeCongeDTO findById(Long id) {
        TypeConge entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeConge not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<TypeCongeDTO> findAvailableForEmploye(Long employeId) {
        List<TypeConge> fromEmplois = repository.findDistinctByEmployeIdAndStatutEmploi(
                employeId,
                EmploiEmploye.StatutEmploi.ACTIF);
        List<TypeConge> nonAnnual = repository.findByCongeAnnuel(TypeConge.CongeAnnuel.N);

        Map<Long, TypeConge> unique = new LinkedHashMap<>();
        for (TypeConge typeConge : fromEmplois) {
            if (typeConge != null && typeConge.getId() != null) {
                unique.put(typeConge.getId(), typeConge);
            }
        }
        for (TypeConge typeConge : nonAnnual) {
            if (typeConge != null && typeConge.getId() != null) {
                unique.putIfAbsent(typeConge.getId(), typeConge);
            }
        }

        return unique.values().stream()
                .sorted(Comparator.comparing(
                        TypeConge::getCodeConge,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public TypeCongeDTO create(TypeCongeCreateDTO dto, String username) {
        if (repository.existsByCodeConge(dto.getCodeConge())) {
            throw new RuntimeException("TypeConge with code " + dto.getCodeConge() + " already exists");
        }
        
        TypeConge entity = new TypeConge();
        entity.setCodeConge(dto.getCodeConge());
        entity.setDescription(dto.getDescription());
        entity.setCongeAnnuel(dto.getCongeAnnuel());
        entity.setNbJours(dto.getNbJours());
        entity.setNbAnneeCumul(dto.getNbAnneeCumul());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public TypeCongeDTO update(Long id, TypeCongeUpdateDTO dto, String username) {
        TypeConge entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeConge not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setDescription(dto.getDescription());
        entity.setCongeAnnuel(dto.getCongeAnnuel());
        entity.setNbJours(dto.getNbJours());
        entity.setNbAnneeCumul(dto.getNbAnneeCumul());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        TypeConge entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeConge not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private TypeCongeDTO toDTO(TypeConge entity) {
        TypeCongeDTO dto = new TypeCongeDTO();
        dto.setId(entity.getId());
        dto.setCodeConge(entity.getCodeConge());
        dto.setDescription(entity.getDescription());
        dto.setCongeAnnuel(entity.getCongeAnnuel());
        dto.setNbJours(entity.getNbJours());
        dto.setNbAnneeCumul(entity.getNbAnneeCumul());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
