package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePaieCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePaieDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePaieUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePaie;
import com.rsoft.hurmanagement.hurmasterdata.repository.RubriquePaieDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RubriquePaieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RubriquePaieService {
    
    private final RubriquePaieRepository repository;
    private final RubriquePaieDeductionRepository rubriquePaieDeductionRepository;
    
    @Transactional(readOnly = true)
    public Page<RubriquePaieDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public RubriquePaieDTO findById(Long id) {
        RubriquePaie entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RubriquePaie not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public java.util.List<RubriquePaieDTO> findAllImposable() {
        return repository.findAllImposable().stream()
                .map(this::toDTO)
                .toList();
    }
    
    @Transactional
    public RubriquePaieDTO create(RubriquePaieCreateDTO dto, String username) {
        if (repository.existsByCodeRubrique(dto.getCodeRubrique())) {
            throw new RuntimeException("RubriquePaie with code " + dto.getCodeRubrique() + " already exists");
        }
        
        RubriquePaie entity = new RubriquePaie();
        entity.setCodeRubrique(dto.getCodeRubrique());
        entity.setLibelle(dto.getLibelle());
        entity.setTypeRubrique(dto.getTypeRubrique());
        entity.setModeCalcul(dto.getModeCalcul());
        entity.setBoni(dto.getBoni() != null ? dto.getBoni() : "Y");
        entity.setPrestation(dto.getPrestation() != null ? dto.getPrestation() : "Y");
        entity.setImposable(dto.getImposable() != null ? dto.getImposable() : "Y");
        entity.setPreavis(dto.getPreavis() != null ? dto.getPreavis() : "N");
        entity.setTaxesSpeciaux(dto.getTaxesSpeciaux() != null ? dto.getTaxesSpeciaux() : "N");
        entity.setSoumisCotisations(dto.getSoumisCotisations() != null ? dto.getSoumisCotisations() : "N");
        entity.setHardcoded(dto.getHardcoded() != null ? dto.getHardcoded() : "N");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public RubriquePaieDTO update(Long id, RubriquePaieUpdateDTO dto, String username) {
        RubriquePaie entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RubriquePaie not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        String oldImposable = entity.getImposable();
        String oldTaxesSpeciaux = entity.getTaxesSpeciaux();
        
        entity.setLibelle(dto.getLibelle());
        entity.setTypeRubrique(dto.getTypeRubrique());
        entity.setModeCalcul(dto.getModeCalcul());
        entity.setBoni(dto.getBoni() != null ? dto.getBoni() : "Y");
        entity.setPrestation(dto.getPrestation() != null ? dto.getPrestation() : "Y");
        entity.setImposable(dto.getImposable() != null ? dto.getImposable() : "Y");
        entity.setPreavis(dto.getPreavis() != null ? dto.getPreavis() : entity.getPreavis());
        entity.setTaxesSpeciaux(dto.getTaxesSpeciaux() != null ? dto.getTaxesSpeciaux() : entity.getTaxesSpeciaux());
        entity.setSoumisCotisations(dto.getSoumisCotisations() != null ? dto.getSoumisCotisations() : entity.getSoumisCotisations());
        entity.setHardcoded(dto.getHardcoded() != null ? dto.getHardcoded() : entity.getHardcoded());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        RubriquePaie updated = repository.save(entity);
        
        // If imposable changed from Y to N, delete all references in rubrique_paie_deduction
        if ("Y".equals(oldImposable) && "N".equals(updated.getImposable())) {
            rubriquePaieDeductionRepository.deleteByRubriquePaieId(id);
        }
        if (oldTaxesSpeciaux != null && !oldTaxesSpeciaux.equals(updated.getTaxesSpeciaux())) {
            rubriquePaieDeductionRepository.deleteByRubriquePaieIdAndDefinitionDeductionSpecialiseNot(
                    id,
                    updated.getTaxesSpeciaux() != null ? updated.getTaxesSpeciaux() : "N"
            );
        }
        
        return toDTO(updated);
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        RubriquePaie entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RubriquePaie not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        if ("Y".equalsIgnoreCase(entity.getHardcoded())) {
            throw new RuntimeException("rubriquePaie.error.cannotDeleteHardcoded");
        }
        
        repository.delete(entity);
    }
    
    private RubriquePaieDTO toDTO(RubriquePaie entity) {
        RubriquePaieDTO dto = new RubriquePaieDTO();
        dto.setId(entity.getId());
        dto.setCodeRubrique(entity.getCodeRubrique());
        dto.setLibelle(entity.getLibelle());
        dto.setTypeRubrique(entity.getTypeRubrique());
        dto.setModeCalcul(entity.getModeCalcul());
        dto.setBoni(entity.getBoni());
        dto.setPrestation(entity.getPrestation());
        dto.setImposable(entity.getImposable());
        dto.setPreavis(entity.getPreavis());
        dto.setTaxesSpeciaux(entity.getTaxesSpeciaux());
        dto.setSoumisCotisations(entity.getSoumisCotisations());
        dto.setHardcoded(entity.getHardcoded());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
