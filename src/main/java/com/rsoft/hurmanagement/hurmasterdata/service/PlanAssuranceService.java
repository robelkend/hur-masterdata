package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PlanAssuranceCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PlanAssuranceDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PlanAssuranceUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.InstitutionTierse;
import com.rsoft.hurmanagement.hurmasterdata.entity.PlanAssurance;
import com.rsoft.hurmanagement.hurmasterdata.entity.ReferencePayroll;
import com.rsoft.hurmanagement.hurmasterdata.repository.InstitutionTierseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PlanAssuranceRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.ReferencePayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PlanAssuranceService {
    
    private final PlanAssuranceRepository repository;
    private final ReferencePayrollRepository referencePayrollRepository;
    private final InstitutionTierseRepository institutionTierseRepository;
    
    @Transactional(readOnly = true)
    public Page<PlanAssuranceDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public PlanAssuranceDTO findById(Long id) {
        PlanAssurance entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PlanAssurance not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public PlanAssuranceDTO create(PlanAssuranceCreateDTO dto, String username) {
        if (repository.existsByCodePlan(dto.getCodePlan())) {
            throw new RuntimeException("PlanAssurance with code " + dto.getCodePlan() + " already exists");
        }
        
        // Validate reference_payroll if provided
        ReferencePayroll referencePayroll = null;
        if (dto.getCodePayroll() != null && !dto.getCodePayroll().trim().isEmpty()) {
            referencePayroll = referencePayrollRepository.findByCodePayroll(dto.getCodePayroll())
                    .orElseThrow(() -> new RuntimeException("ReferencePayroll with code " + dto.getCodePayroll() + " not found"));
        }
        
        // Validate compagnie_assurance (required)
        InstitutionTierse compagnieAssurance = institutionTierseRepository.findByCodeInstitution(dto.getCodeInstitution())
                .orElseThrow(() -> new RuntimeException("InstitutionTierse with code " + dto.getCodeInstitution() + " not found"));
        
        PlanAssurance entity = new PlanAssurance();
        entity.setCodePlan(dto.getCodePlan());
        entity.setLibelle(dto.getLibelle());
        entity.setDescription(dto.getDescription());
        entity.setReferencePayroll(referencePayroll);
        entity.setTypePrelevement(dto.getTypePrelevement());
        entity.setValeur(dto.getValeur());
        entity.setValeurCouverte(dto.getValeurCouverte());
        entity.setCompagnieAssurance(compagnieAssurance);
        entity.setCategorie(dto.getCategorie());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public PlanAssuranceDTO update(Long id, PlanAssuranceUpdateDTO dto, String username) {
        PlanAssurance entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PlanAssurance not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Validate reference_payroll if provided
        ReferencePayroll referencePayroll = null;
        if (dto.getCodePayroll() != null && !dto.getCodePayroll().trim().isEmpty()) {
            referencePayroll = referencePayrollRepository.findByCodePayroll(dto.getCodePayroll())
                    .orElseThrow(() -> new RuntimeException("ReferencePayroll with code " + dto.getCodePayroll() + " not found"));
        }
        
        // Validate compagnie_assurance (required)
        InstitutionTierse compagnieAssurance = institutionTierseRepository.findByCodeInstitution(dto.getCodeInstitution())
                .orElseThrow(() -> new RuntimeException("InstitutionTierse with code " + dto.getCodeInstitution() + " not found"));
        
        entity.setLibelle(dto.getLibelle());
        entity.setDescription(dto.getDescription());
        entity.setReferencePayroll(referencePayroll);
        entity.setTypePrelevement(dto.getTypePrelevement());
        entity.setValeur(dto.getValeur());
        entity.setValeurCouverte(dto.getValeurCouverte());
        entity.setCompagnieAssurance(compagnieAssurance);
        entity.setCategorie(dto.getCategorie());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        PlanAssurance entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PlanAssurance not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private PlanAssuranceDTO toDTO(PlanAssurance entity) {
        PlanAssuranceDTO dto = new PlanAssuranceDTO();
        dto.setId(entity.getId());
        dto.setCodePlan(entity.getCodePlan());
        dto.setLibelle(entity.getLibelle());
        dto.setDescription(entity.getDescription());
        if (entity.getReferencePayroll() != null) {
            dto.setCodePayroll(entity.getReferencePayroll().getCodePayroll());
            dto.setReferencePayrollDescription(entity.getReferencePayroll().getDescription());
        }
        dto.setTypePrelevement(entity.getTypePrelevement());
        dto.setValeur(entity.getValeur());
        dto.setValeurCouverte(entity.getValeurCouverte());
        if (entity.getCompagnieAssurance() != null) {
            dto.setCodeInstitution(entity.getCompagnieAssurance().getCodeInstitution());
            dto.setCompagnieAssuranceNom(entity.getCompagnieAssurance().getNom());
        }
        dto.setCategorie(entity.getCategorie());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
