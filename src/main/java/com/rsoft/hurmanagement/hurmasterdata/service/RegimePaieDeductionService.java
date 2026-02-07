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
public class RegimePaieDeductionService {
    
    private final RegimePaieDeductionRepository repository;
    private final RegimePaieRepository regimePaieRepository;
    private final DefinitionDeductionRepository definitionDeductionRepository;
    
    @Transactional(readOnly = true)
    public List<RegimePaieDeductionDTO> findByRegimePaieId(Long regimePaieId) {
        return repository.findByRegimePaieId(regimePaieId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RegimePaieDeductionDTO create(RegimePaieDeductionCreateDTO dto, String username) {
        if (repository.existsByRegimePaieIdAndDeductionCodeId(dto.getRegimePaieId(), dto.getDeductionCodeId())) {
            throw new RuntimeException("RegimePaieDeduction already exists for this regime_paie and definition_deduction");
        }
        
        RegimePaie regimePaie = regimePaieRepository.findById(dto.getRegimePaieId())
                .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + dto.getRegimePaieId()));
        
        DefinitionDeduction definitionDeduction = definitionDeductionRepository.findById(dto.getDeductionCodeId())
                .orElseThrow(() -> new RuntimeException("DefinitionDeduction not found with id: " + dto.getDeductionCodeId()));
        
        RegimePaieDeduction entity = new RegimePaieDeduction();
        entity.setRegimePaie(regimePaie);
        entity.setDeductionCode(definitionDeduction);
        entity.setExclusif(dto.getExclusif() != null ? dto.getExclusif() : "Y");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        RegimePaieDeduction saved = repository.save(entity);
        return toDTO(saved);
    }
    
    @Transactional
    public RegimePaieDeductionDTO update(Long id, RegimePaieDeductionUpdateDTO dto, String username) {
        RegimePaieDeduction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RegimePaieDeduction not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setExclusif(dto.getExclusif() != null ? dto.getExclusif() : "Y");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        RegimePaieDeduction saved = repository.save(entity);
        return toDTO(saved);
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        RegimePaieDeduction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RegimePaieDeduction not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private RegimePaieDeductionDTO toDTO(RegimePaieDeduction entity) {
        RegimePaieDeductionDTO dto = new RegimePaieDeductionDTO();
        dto.setId(entity.getId());
        dto.setRegimePaieId(entity.getRegimePaie().getId());
        dto.setDeductionCodeId(entity.getDeductionCode().getId());
        dto.setDeductionCodeCode(entity.getDeductionCode().getCodeDeduction());
        dto.setDeductionCodeLibelle(entity.getDeductionCode().getLibelle());
        dto.setExclusif(entity.getExclusif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
