package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.ExclusionDeductionCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ExclusionDeductionDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ExclusionDeductionUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.DefinitionDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.ExclusionDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.DefinitionDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.ExclusionDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExclusionDeductionService {

    private final ExclusionDeductionRepository repository;
    private final TypeEmployeRepository typeEmployeRepository;
    private final DefinitionDeductionRepository definitionDeductionRepository;

    @Transactional(readOnly = true)
    public List<ExclusionDeductionDTO> findByTypeEmployeId(Long typeEmployeId) {
        return repository.findByTypeEmployeId(typeEmployeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExclusionDeductionDTO create(ExclusionDeductionCreateDTO dto, String username) {
        if (repository.existsByTypeEmployeIdAndDefinitionDeductionId(dto.getTypeEmployeId(), dto.getDefinitionDeductionId())) {
            throw new RuntimeException("Exclusion already exists for this type employe and definition deduction");
        }
        TypeEmploye typeEmploye = typeEmployeRepository.findById(dto.getTypeEmployeId())
                .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + dto.getTypeEmployeId()));
        DefinitionDeduction definition = definitionDeductionRepository.findById(dto.getDefinitionDeductionId())
                .orElseThrow(() -> new RuntimeException("DefinitionDeduction not found with id: " + dto.getDefinitionDeductionId()));

        ExclusionDeduction entity = new ExclusionDeduction();
        entity.setTypeEmploye(typeEmploye);
        entity.setDefinitionDeduction(definition);
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public ExclusionDeductionDTO update(Long id, ExclusionDeductionUpdateDTO dto, String username) {
        ExclusionDeduction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ExclusionDeduction not found with id: " + id));
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        if (!entity.getTypeEmploye().getId().equals(dto.getTypeEmployeId())) {
            TypeEmploye typeEmploye = typeEmployeRepository.findById(dto.getTypeEmployeId())
                    .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + dto.getTypeEmployeId()));
            entity.setTypeEmploye(typeEmploye);
        }
        if (!entity.getDefinitionDeduction().getId().equals(dto.getDefinitionDeductionId())) {
            if (repository.existsByTypeEmployeIdAndDefinitionDeductionId(dto.getTypeEmployeId(), dto.getDefinitionDeductionId())) {
                throw new RuntimeException("Exclusion already exists for this type employe and definition deduction");
            }
            DefinitionDeduction definition = definitionDeductionRepository.findById(dto.getDefinitionDeductionId())
                    .orElseThrow(() -> new RuntimeException("DefinitionDeduction not found with id: " + dto.getDefinitionDeductionId()));
            entity.setDefinitionDeduction(definition);
        }
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, Integer rowscn) {
        ExclusionDeduction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ExclusionDeduction not found with id: " + id));
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        repository.delete(entity);
    }

    private ExclusionDeductionDTO toDTO(ExclusionDeduction entity) {
        ExclusionDeductionDTO dto = new ExclusionDeductionDTO();
        dto.setId(entity.getId());
        if (entity.getTypeEmploye() != null) {
            dto.setTypeEmployeId(entity.getTypeEmploye().getId());
            dto.setTypeEmployeDescription(entity.getTypeEmploye().getDescription());
        }
        if (entity.getDefinitionDeduction() != null) {
            dto.setDefinitionDeductionId(entity.getDefinitionDeduction().getId());
            dto.setDefinitionDeductionCode(entity.getDefinitionDeduction().getCodeDeduction());
            dto.setDefinitionDeductionLibelle(entity.getDefinitionDeduction().getLibelle());
        }
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
