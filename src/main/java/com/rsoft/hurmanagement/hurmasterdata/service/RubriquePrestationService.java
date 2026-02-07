package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePrestationCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePrestationDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePrestationUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePrestation;
import com.rsoft.hurmanagement.hurmasterdata.repository.RubriquePrestationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RubriquePrestationService {
    
    private final RubriquePrestationRepository repository;
    
    @Transactional(readOnly = true)
    public Page<RubriquePrestationDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public RubriquePrestationDTO findById(Long id) {
        RubriquePrestation entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RubriquePrestation not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public RubriquePrestationDTO create(RubriquePrestationCreateDTO dto, String username) {
        if (repository.existsByCodePrestation(dto.getCodePrestation())) {
            throw new RuntimeException("RubriquePrestation with code " + dto.getCodePrestation() + " already exists");
        }
        
        RubriquePrestation entity = new RubriquePrestation();
        entity.setCodePrestation(dto.getCodePrestation());
        entity.setDescription(dto.getDescription());
        entity.setPrelevement(dto.getPrelevement());
        entity.setHardcoded(dto.getHardcoded() != null ? dto.getHardcoded() : "N");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public RubriquePrestationDTO update(Long id, RubriquePrestationUpdateDTO dto, String username) {
        RubriquePrestation entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RubriquePrestation not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setDescription(dto.getDescription());
        entity.setPrelevement(dto.getPrelevement());
        entity.setHardcoded(dto.getHardcoded() != null ? dto.getHardcoded() : entity.getHardcoded());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        RubriquePrestation entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RubriquePrestation not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        if ("Y".equalsIgnoreCase(entity.getHardcoded())) {
            throw new RuntimeException("rubriquePrestation.error.cannotDeleteHardcoded");
        }
        
        repository.delete(entity);
    }
    
    private RubriquePrestationDTO toDTO(RubriquePrestation entity) {
        RubriquePrestationDTO dto = new RubriquePrestationDTO();
        dto.setId(entity.getId());
        dto.setCodePrestation(entity.getCodePrestation());
        dto.setDescription(entity.getDescription());
        dto.setPrelevement(entity.getPrelevement());
        dto.setHardcoded(entity.getHardcoded());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
