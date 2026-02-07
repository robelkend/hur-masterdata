package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.TauxChangeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TauxChangeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TauxChangeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Devise;
import com.rsoft.hurmanagement.hurmasterdata.entity.InstitutionTierse;
import com.rsoft.hurmanagement.hurmasterdata.entity.TauxChange;
import com.rsoft.hurmanagement.hurmasterdata.repository.DeviseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.InstitutionTierseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TauxChangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TauxChangeService {
    
    private final TauxChangeRepository repository;
    private final DeviseRepository deviseRepository;
    private final InstitutionTierseRepository institutionRepository;
    
    @Transactional(readOnly = true)
    public Page<TauxChangeDTO> findAll(String codeDevise, Pageable pageable) {
        if (codeDevise != null && !codeDevise.trim().isEmpty()) {
            return repository.findAllByCodeDeviseOptional(codeDevise, pageable)
                    .map(this::toDTO);
        }
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public List<TauxChangeDTO> findByDeviseCodeDevise(String codeDevise, LocalDate dateFrom, LocalDate dateTo) {
        List<TauxChange> results;
        
        // Use appropriate query based on which date filters are provided
        if (dateFrom != null && dateTo != null) {
            // Both dates provided
            results = repository.findByDeviseCodeDeviseAndDateRange(codeDevise, dateFrom, dateTo);
        } else if (dateFrom != null) {
            // Only dateFrom provided
            results = repository.findByDeviseCodeDeviseAndDateFrom(codeDevise, dateFrom);
        } else if (dateTo != null) {
            // Only dateTo provided
            results = repository.findByDeviseCodeDeviseAndDateTo(codeDevise, dateTo);
        } else {
            // No date filters, return all for the devise
            results = repository.findByDeviseCodeDeviseOrderByDateTauxDesc(codeDevise);
        }
        
        return results.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public TauxChangeDTO findById(Long id) {
        TauxChange entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TauxChange not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public TauxChangeDTO create(TauxChangeCreateDTO dto, String username) {
        // Validate that devise exists
        Devise devise = deviseRepository.findByCodeDevise(dto.getCodeDevise())
                .orElseThrow(() -> new RuntimeException("Devise with code " + dto.getCodeDevise() + " not found"));
        
        // Validate institution if provided
        InstitutionTierse institution = null;
        if (dto.getCodeInstitution() != null && !dto.getCodeInstitution().trim().isEmpty()) {
            institution = institutionRepository.findByCodeInstitution(dto.getCodeInstitution())
                    .orElseThrow(() -> new RuntimeException("Institution with code " + dto.getCodeInstitution() + " not found"));
            
            // Check uniqueness: (devise, date, institution)
            if (repository.existsByDeviseCodeDeviseAndDateTauxAndInstitutionCodeInstitution(
                    dto.getCodeDevise(), dto.getDateTaux(), dto.getCodeInstitution())) {
                throw new RuntimeException("A rate already exists for this devise, date, and institution combination");
            }
        } else {
            // Check uniqueness: (devise, date, null institution)
            if (repository.existsByDeviseCodeDeviseAndDateTauxAndInstitutionIsNull(
                    dto.getCodeDevise(), dto.getDateTaux())) {
                throw new RuntimeException("A rate already exists for this devise and date combination");
            }
        }
        
        TauxChange entity = new TauxChange();
        entity.setDateTaux(dto.getDateTaux());
        entity.setTaux(dto.getTaux());
        // Initialize tauxPayroll to 0 if not provided
        entity.setTauxPayroll(dto.getTauxPayroll() != null ? dto.getTauxPayroll() : BigDecimal.ZERO);
        entity.setDevise(devise);
        entity.setInstitution(institution);
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public TauxChangeDTO update(Long id, TauxChangeUpdateDTO dto, String username) {
        TauxChange entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TauxChange not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Validate institution if provided
        InstitutionTierse institution = null;
        if (dto.getCodeInstitution() != null && !dto.getCodeInstitution().trim().isEmpty()) {
            institution = institutionRepository.findByCodeInstitution(dto.getCodeInstitution())
                    .orElseThrow(() -> new RuntimeException("Institution with code " + dto.getCodeInstitution() + " not found"));
            
            // Check uniqueness excluding current record
            String currentInstitutionCode = entity.getInstitution() != null ? entity.getInstitution().getCodeInstitution() : null;
            if (!dto.getCodeInstitution().equals(currentInstitutionCode) || 
                !entity.getDateTaux().equals(dto.getDateTaux())) {
                // Date or institution changed, check for duplicates
                if (repository.existsByDeviseCodeDeviseAndDateTauxAndInstitutionCodeInstitution(
                        entity.getDevise().getCodeDevise(), dto.getDateTaux(), dto.getCodeInstitution())) {
                    throw new RuntimeException("A rate already exists for this devise, date, and institution combination");
                }
            }
        } else {
            // Check uniqueness excluding current record
            String currentInstitutionCode = entity.getInstitution() != null ? entity.getInstitution().getCodeInstitution() : null;
            if (currentInstitutionCode != null || !entity.getDateTaux().equals(dto.getDateTaux())) {
                // Date or institution changed, check for duplicates
                if (repository.existsByDeviseCodeDeviseAndDateTauxAndInstitutionIsNull(
                        entity.getDevise().getCodeDevise(), dto.getDateTaux())) {
                    throw new RuntimeException("A rate already exists for this devise and date combination");
                }
            }
        }
        
        entity.setDateTaux(dto.getDateTaux());
        entity.setTaux(dto.getTaux());
        // Initialize tauxPayroll to 0 if not provided
        entity.setTauxPayroll(dto.getTauxPayroll() != null ? dto.getTauxPayroll() : BigDecimal.ZERO);
        entity.setInstitution(institution);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        TauxChange entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TauxChange not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private TauxChangeDTO toDTO(TauxChange entity) {
        TauxChangeDTO dto = new TauxChangeDTO();
        dto.setId(entity.getId());
        dto.setDateTaux(entity.getDateTaux());
        dto.setTaux(entity.getTaux());
        dto.setTauxPayroll(entity.getTauxPayroll());
        if (entity.getDevise() != null) {
            dto.setCodeDevise(entity.getDevise().getCodeDevise());
            dto.setDeviseDescription(entity.getDevise().getDescription());
        }
        if (entity.getInstitution() != null) {
            dto.setCodeInstitution(entity.getInstitution().getCodeInstitution());
            dto.setInstitutionNom(entity.getInstitution().getNom());
        }
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
