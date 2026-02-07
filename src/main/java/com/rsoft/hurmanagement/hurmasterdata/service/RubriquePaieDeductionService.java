package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePaieDeductionCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePaieDeductionDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.DefinitionDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePaie;
import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePaieDeduction;
import com.rsoft.hurmanagement.hurmasterdata.repository.DefinitionDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RubriquePaieDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RubriquePaieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RubriquePaieDeductionService {
    
    private final RubriquePaieDeductionRepository repository;
    private final DefinitionDeductionRepository definitionDeductionRepository;
    private final RubriquePaieRepository rubriquePaieRepository;
    
    @Transactional(readOnly = true)
    public List<RubriquePaieDeductionDTO> findByDefinitionDeductionId(Long definitionDeductionId) {
        return repository.findByDefinitionDeductionId(definitionDeductionId).stream()
                .map(this::toDTO)
                .toList();
    }
    
    @Transactional
    public RubriquePaieDeductionDTO create(RubriquePaieDeductionCreateDTO dto, String username) {
        DefinitionDeduction definitionDeduction = definitionDeductionRepository.findById(dto.getDefinitionDeductionId())
                .orElseThrow(() -> new RuntimeException("DefinitionDeduction not found with id: " + dto.getDefinitionDeductionId()));
        
        RubriquePaie rubriquePaie = rubriquePaieRepository.findById(dto.getRubriquePaieId())
                .orElseThrow(() -> new RuntimeException("RubriquePaie not found with id: " + dto.getRubriquePaieId()));
        
        // Check if rubrique is imposable
        if (!"Y".equals(rubriquePaie.getImposable())) {
            throw new RuntimeException("Only rubriques with imposable = Y can be added");
        }

        String specialise = definitionDeduction.getSpecialise() != null ? definitionDeduction.getSpecialise() : "N";
        String taxesSpeciaux = rubriquePaie.getTaxesSpeciaux() != null ? rubriquePaie.getTaxesSpeciaux() : "N";
        if (!specialise.equals(taxesSpeciaux)) {
            throw new RuntimeException("definitionDeduction.error.taxesSpeciauxMismatch");
        }
        
        // Check for duplicate
        if (repository.existsByDefinitionDeductionIdAndRubriquePaieId(dto.getDefinitionDeductionId(), dto.getRubriquePaieId())) {
            throw new RuntimeException("This rubrique is already associated with this definition deduction");
        }
        
        RubriquePaieDeduction entity = new RubriquePaieDeduction();
        entity.setDefinitionDeduction(definitionDeduction);
        entity.setRubriquePaie(rubriquePaie);
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        RubriquePaieDeduction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RubriquePaieDeduction not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private RubriquePaieDeductionDTO toDTO(RubriquePaieDeduction entity) {
        RubriquePaieDeductionDTO dto = new RubriquePaieDeductionDTO();
        dto.setId(entity.getId());
        dto.setDefinitionDeductionId(entity.getDefinitionDeduction().getId());
        dto.setRubriquePaieId(entity.getRubriquePaie().getId());
        dto.setRubriquePaieCode(entity.getRubriquePaie().getCodeRubrique());
        dto.setRubriquePaieLibelle(entity.getRubriquePaie().getLibelle());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
