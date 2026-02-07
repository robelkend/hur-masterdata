package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.DefinitionDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.InstitutionTierse;
import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePaieDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.TrancheBaremeDeduction;
import com.rsoft.hurmanagement.hurmasterdata.repository.DefinitionDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.InstitutionTierseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RubriquePaieDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TrancheBaremeDeductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefinitionDeductionService {
    
    private final DefinitionDeductionRepository repository;
    private final InstitutionTierseRepository institutionTierseRepository;
    private final TrancheBaremeDeductionRepository trancheRepository;
    private final RubriquePaieDeductionRepository rubriquePaieDeductionRepository;
    
    @Transactional(readOnly = true)
    public Page<DefinitionDeductionDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public DefinitionDeductionDTO findById(Long id) {
        DefinitionDeduction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DefinitionDeduction not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<DefinitionDeductionDTO> findAllForDropdown() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public DefinitionDeductionDTO create(DefinitionDeductionCreateDTO dto, String username) {
        if (repository.existsByCodeDeduction(dto.getCodeDeduction())) {
            throw new RuntimeException("DefinitionDeduction with code " + dto.getCodeDeduction() + " already exists");
        }
        
        DefinitionDeduction entity = new DefinitionDeduction();
        entity.setCodeDeduction(dto.getCodeDeduction());
        entity.setLibelle(dto.getLibelle());
        entity.setDescription(dto.getDescription());
        entity.setTypeDeduction(dto.getTypeDeduction() != null ? dto.getTypeDeduction() : DefinitionDeduction.TypeDeduction.POURCENTAGE);
        entity.setBaseLimite(dto.getBaseLimite() != null ? dto.getBaseLimite() : DefinitionDeduction.BaseLimite.FIXE);
        entity.setArrondir(dto.getArrondir());
        entity.setValeur(dto.getValeur() != null ? dto.getValeur() : BigDecimal.ZERO);
        entity.setValeurCouvert(dto.getValeurCouvert() != null ? dto.getValeurCouvert() : BigDecimal.ZERO);
        entity.setFrequence(dto.getFrequence());
        entity.setPctHorsCalcul(dto.getPctHorsCalcul() != null ? dto.getPctHorsCalcul() : BigDecimal.ZERO);
        entity.setMinPrelevement(dto.getMinPrelevement() != null ? dto.getMinPrelevement() : BigDecimal.ZERO);
        entity.setMaxPrelevement(dto.getMaxPrelevement() != null ? dto.getMaxPrelevement() : BigDecimal.ZERO);
        entity.setProbatoire(dto.getProbatoire() != null ? dto.getProbatoire() : "Y");
        entity.setSpecialise(dto.getSpecialise() != null ? dto.getSpecialise() : "N");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        if (dto.getEntiteId() != null) {
            InstitutionTierse entite = institutionTierseRepository.findById(dto.getEntiteId())
                    .orElseThrow(() -> new RuntimeException("InstitutionTierse not found with id: " + dto.getEntiteId()));
            entity.setEntite(entite);
        }
        
        DefinitionDeduction savedEntity = repository.save(entity);

        // Ensure rubrique paie deductions match specialise flag
        rubriquePaieDeductionRepository.deleteByDefinitionDeductionIdAndRubriquePaieTaxesSpeciauxNot(
                savedEntity.getId(),
                savedEntity.getSpecialise() != null ? savedEntity.getSpecialise() : "N"
        );
        
        // Save tranches if provided
        if (dto.getTranches() != null && !dto.getTranches().isEmpty()) {
            List<TrancheBaremeDeduction> tranches = dto.getTranches().stream()
                    .map(t -> {
                        TrancheBaremeDeduction tranche = new TrancheBaremeDeduction();
                        tranche.setDefinitionDeduction(savedEntity);
                        tranche.setBorneInf(t.getBorneInf());
                        tranche.setBorneSup(t.getBorneSup());
                        tranche.setTypeDeduction(t.getTypeDeduction() != null ? t.getTypeDeduction() : DefinitionDeduction.TypeDeduction.POURCENTAGE);
                        tranche.setValeur(t.getValeur());
                        tranche.setCreatedBy(username);
                        tranche.setCreatedOn(OffsetDateTime.now());
                        tranche.setRowscn(1);
                        return tranche;
                    })
                    .collect(Collectors.toList());
            trancheRepository.saveAll(tranches);
        }
        
        return toDTO(savedEntity);
    }
    
    @Transactional
    public DefinitionDeductionDTO update(Long id, DefinitionDeductionUpdateDTO dto, String username) {
        DefinitionDeduction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DefinitionDeduction not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setLibelle(dto.getLibelle());
        entity.setDescription(dto.getDescription());
        entity.setTypeDeduction(dto.getTypeDeduction());
        entity.setBaseLimite(dto.getBaseLimite());
        entity.setArrondir(dto.getArrondir());
        entity.setValeur(dto.getValeur());
        entity.setValeurCouvert(dto.getValeurCouvert());
        entity.setFrequence(dto.getFrequence());
        entity.setPctHorsCalcul(dto.getPctHorsCalcul() != null ? dto.getPctHorsCalcul() : BigDecimal.ZERO);
        entity.setMinPrelevement(dto.getMinPrelevement() != null ? dto.getMinPrelevement() : BigDecimal.ZERO);
        entity.setMaxPrelevement(dto.getMaxPrelevement() != null ? dto.getMaxPrelevement() : BigDecimal.ZERO);
        entity.setProbatoire(dto.getProbatoire());
        entity.setSpecialise(dto.getSpecialise());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        if (dto.getEntiteId() != null) {
            InstitutionTierse entite = institutionTierseRepository.findById(dto.getEntiteId())
                    .orElseThrow(() -> new RuntimeException("InstitutionTierse not found with id: " + dto.getEntiteId()));
            entity.setEntite(entite);
        } else {
            entity.setEntite(null);
        }
        
        DefinitionDeduction savedEntity = repository.save(entity);

        // Ensure rubrique paie deductions match specialise flag
        rubriquePaieDeductionRepository.deleteByDefinitionDeductionIdAndRubriquePaieTaxesSpeciauxNot(
                savedEntity.getId(),
                savedEntity.getSpecialise() != null ? savedEntity.getSpecialise() : "N"
        );
        
        // Update tranches
        if (dto.getTranches() != null) {
            // Delete existing tranches
            trancheRepository.deleteByDefinitionDeductionId(id);
            
            // Save new/updated tranches
            if (!dto.getTranches().isEmpty()) {
                List<TrancheBaremeDeduction> tranches = dto.getTranches().stream()
                        .map(t -> {
                            TrancheBaremeDeduction tranche;
                            if (t.getId() != null && t.getId() > 0) {
                                // Update existing
                                tranche = trancheRepository.findById(t.getId())
                                        .orElseThrow(() -> new RuntimeException("TrancheBaremeDeduction not found with id: " + t.getId()));
                                
                                if (!tranche.getRowscn().equals(t.getRowscn())) {
                                    throw new RuntimeException("Tranche record has been modified by another user. Please refresh before saving.");
                                }
                                tranche.setRowscn(tranche.getRowscn() + 1);
                                tranche.setUpdatedBy(username);
                                tranche.setUpdatedOn(OffsetDateTime.now());
                            } else {
                                // New tranche
                                tranche = new TrancheBaremeDeduction();
                                tranche.setCreatedBy(username);
                                tranche.setCreatedOn(OffsetDateTime.now());
                                tranche.setRowscn(1);
                            }
                            
                            tranche.setDefinitionDeduction(savedEntity);
                            tranche.setBorneInf(t.getBorneInf());
                            tranche.setBorneSup(t.getBorneSup());
                            tranche.setTypeDeduction(t.getTypeDeduction());
                            tranche.setValeur(t.getValeur());
                            
                            return tranche;
                        })
                        .collect(Collectors.toList());
                trancheRepository.saveAll(tranches);
            }
        }
        
        return toDTO(savedEntity);
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        DefinitionDeduction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DefinitionDeduction not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Delete associated tranches first
        trancheRepository.deleteByDefinitionDeductionId(id);
        
        repository.delete(entity);
    }

    @Transactional
    public DefinitionDeductionDTO cloneDefinitionDeduction(Long id, String newCodeDeduction, String username) {
        if (repository.existsByCodeDeduction(newCodeDeduction)) {
            throw new RuntimeException("DefinitionDeduction with code " + newCodeDeduction + " already exists");
        }
        DefinitionDeduction source = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DefinitionDeduction not found with id: " + id));

        DefinitionDeduction clone = new DefinitionDeduction();
        clone.setCodeDeduction(newCodeDeduction);
        clone.setLibelle(source.getLibelle());
        clone.setDescription(source.getDescription());
        clone.setTypeDeduction(source.getTypeDeduction());
        clone.setBaseLimite(source.getBaseLimite());
        clone.setEntite(source.getEntite());
        clone.setArrondir(source.getArrondir());
        clone.setValeur(source.getValeur());
        clone.setValeurCouvert(source.getValeurCouvert());
        clone.setFrequence(source.getFrequence());
        clone.setPctHorsCalcul(source.getPctHorsCalcul());
        clone.setMinPrelevement(source.getMinPrelevement());
        clone.setMaxPrelevement(source.getMaxPrelevement());
        clone.setProbatoire(source.getProbatoire());
        clone.setSpecialise(source.getSpecialise());
        clone.setCreatedBy(username);
        clone.setCreatedOn(OffsetDateTime.now());
        clone.setRowscn(1);

        DefinitionDeduction saved = repository.save(clone);

        List<TrancheBaremeDeduction> tranches = trancheRepository.findByDefinitionDeductionIdOrderByBorneInfAsc(source.getId());
        for (TrancheBaremeDeduction sourceTranche : tranches) {
            TrancheBaremeDeduction copy = new TrancheBaremeDeduction();
            copy.setDefinitionDeduction(saved);
            copy.setBorneInf(sourceTranche.getBorneInf());
            copy.setBorneSup(sourceTranche.getBorneSup());
            copy.setTypeDeduction(sourceTranche.getTypeDeduction());
            copy.setValeur(sourceTranche.getValeur());
            copy.setCreatedBy(username);
            copy.setCreatedOn(OffsetDateTime.now());
            copy.setRowscn(1);
            trancheRepository.save(copy);
        }

        List<RubriquePaieDeduction> rubriqueDeductions = rubriquePaieDeductionRepository.findByDefinitionDeductionId(source.getId());
        for (RubriquePaieDeduction sourceRubrique : rubriqueDeductions) {
            RubriquePaieDeduction copy = new RubriquePaieDeduction();
            copy.setDefinitionDeduction(saved);
            copy.setRubriquePaie(sourceRubrique.getRubriquePaie());
            copy.setCreatedBy(username);
            copy.setCreatedOn(OffsetDateTime.now());
            copy.setRowscn(1);
            rubriquePaieDeductionRepository.save(copy);
        }

        return toDTO(saved);
    }
    
    private DefinitionDeductionDTO toDTO(DefinitionDeduction entity) {
        DefinitionDeductionDTO dto = new DefinitionDeductionDTO();
        dto.setId(entity.getId());
        dto.setCodeDeduction(entity.getCodeDeduction());
        dto.setLibelle(entity.getLibelle());
        dto.setDescription(entity.getDescription());
        dto.setTypeDeduction(entity.getTypeDeduction());
        dto.setBaseLimite(entity.getBaseLimite());
        dto.setArrondir(entity.getArrondir());
        dto.setValeur(entity.getValeur());
        dto.setValeurCouvert(entity.getValeurCouvert());
        dto.setFrequence(entity.getFrequence());
        dto.setPctHorsCalcul(entity.getPctHorsCalcul());
        dto.setMinPrelevement(entity.getMinPrelevement());
        dto.setMaxPrelevement(entity.getMaxPrelevement());
        dto.setProbatoire(entity.getProbatoire());
        dto.setSpecialise(entity.getSpecialise());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        if (entity.getEntite() != null) {
            dto.setEntiteId(entity.getEntite().getId());
            dto.setEntiteCode(entity.getEntite().getCodeInstitution());
            dto.setEntiteNom(entity.getEntite().getNom());
        }
        
        // Load tranches
        List<TrancheBaremeDeductionDTO> tranches = trancheRepository.findByDefinitionDeductionIdOrderByBorneInfAsc(entity.getId())
                .stream()
                .map(this::trancheToDTO)
                .collect(Collectors.toList());
        dto.setTranches(tranches);
        
        return dto;
    }
    
    private TrancheBaremeDeductionDTO trancheToDTO(TrancheBaremeDeduction tranche) {
        TrancheBaremeDeductionDTO dto = new TrancheBaremeDeductionDTO();
        dto.setId(tranche.getId());
        dto.setDefinitionDeductionId(tranche.getDefinitionDeduction().getId());
        dto.setBorneInf(tranche.getBorneInf());
        dto.setBorneSup(tranche.getBorneSup());
        dto.setTypeDeduction(tranche.getTypeDeduction());
        dto.setValeur(tranche.getValeur());
        dto.setCreatedBy(tranche.getCreatedBy());
        dto.setCreatedOn(tranche.getCreatedOn());
        dto.setUpdatedBy(tranche.getUpdatedBy());
        dto.setUpdatedOn(tranche.getUpdatedOn());
        dto.setRowscn(tranche.getRowscn());
        return dto;
    }
}
