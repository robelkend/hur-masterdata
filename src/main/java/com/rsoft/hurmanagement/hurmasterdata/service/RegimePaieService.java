package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegimePaieService {
    
    private final RegimePaieRepository repository;
    private final DeviseRepository deviseRepository;
    private final EmployeRepository employeRepository;
    private final RegimePaieDeductionRepository regimePaieDeductionRepository;
    
    @Transactional(readOnly = true)
    public Page<RegimePaieDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public RegimePaieDTO findById(Long id) {
        RegimePaie entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<RegimePaieDTO> findAllForDropdown() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "codeRegimePaie"))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RegimePaieDTO create(RegimePaieCreateDTO dto, String username) {
        if (repository.existsByCodeRegimePaie(dto.getCodeRegimePaie())) {
            throw new RuntimeException("RegimePaie with code " + dto.getCodeRegimePaie() + " already exists");
        }
        
        Devise devise = deviseRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
        
        RegimePaie entity = new RegimePaie();
        entity.setCodeRegimePaie(dto.getCodeRegimePaie());
        entity.setDescription(dto.getDescription());
        entity.setModeRemuneration(dto.getModeRemuneration());
        entity.setPeriodicite(dto.getPeriodicite());
        entity.setDevise(devise);
        entity.setHoraireActif(dto.getHoraireActif() != null ? dto.getHoraireActif() : "Y");
        entity.setJoursPayes(dto.getJoursPayes());
        entity.setSuppAuto(dto.getSuppAuto() != null ? dto.getSuppAuto() : "N");
        entity.setBloquerNetNegatif(dto.getBloquerNetNegatif() != null ? dto.getBloquerNetNegatif() : "N");
        entity.setTaxeChaqueNPaies(dto.getTaxeChaqueNPaies() != null ? dto.getTaxeChaqueNPaies() : 0);
        entity.setSuppChaqueNPaies(dto.getSuppChaqueNPaies() != null ? dto.getSuppChaqueNPaies() : 0);
        entity.setSuppDecalageNbPaies(dto.getSuppDecalageNbPaies() != null ? dto.getSuppDecalageNbPaies() : 0);
        entity.setAutoTraitement(dto.getAutoTraitement() != null ? dto.getAutoTraitement() : "N");
        entity.setNiveauAutoTraitement(dto.getNiveauAutoTraitement() != null ? dto.getNiveauAutoTraitement() : RegimePaie.NiveauAutoTraitement.AUCUN);
        entity.setHeuresMinJour(dto.getHeuresMinJour());
        entity.setPayerSiMoinsMin(dto.getPayerSiMoinsMin() != null ? dto.getPayerSiMoinsMin() : "Y");
        entity.setRetardsMaxJour(dto.getRetardsMaxJour() != null ? dto.getRetardsMaxJour() : 0);
        entity.setPaiementSurCompte(dto.getPaiementSurCompte() != null ? dto.getPaiementSurCompte() : "Y");
        entity.setTaxeSurDernierNetPositif(dto.getTaxeSurDernierNetPositif() != null ? dto.getTaxeSurDernierNetPositif() : "Y");
        entity.setTaxable(dto.getTaxable() != null ? dto.getTaxable() : "Y");
        
        if (dto.getResponsableId() != null) {
            Employe responsable = employeRepository.findById(dto.getResponsableId())
                    .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getResponsableId()));
            entity.setResponsable(responsable);
        }
        
        entity.setDernierePaie(dto.getDernierePaie());
        entity.setProchainePaie(dto.getProchainePaie());
        entity.setDernierPrelevement(dto.getDernierPrelevement());
        entity.setDernierSupplement(dto.getDernierSupplement());
        entity.setProchainSupplement(dto.getProchainSupplement());
        
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        RegimePaie saved = repository.save(entity);
        return toDTO(saved);
    }
    
    @Transactional
    public RegimePaieDTO update(Long id, RegimePaieUpdateDTO dto, String username) {
        RegimePaie entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        Devise devise = deviseRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
        
        entity.setDescription(dto.getDescription());
        entity.setModeRemuneration(dto.getModeRemuneration());
        entity.setPeriodicite(dto.getPeriodicite());
        entity.setDevise(devise);
        entity.setHoraireActif(dto.getHoraireActif() != null ? dto.getHoraireActif() : "Y");
        entity.setJoursPayes(dto.getJoursPayes());
        entity.setSuppAuto(dto.getSuppAuto() != null ? dto.getSuppAuto() : "N");
        entity.setBloquerNetNegatif(dto.getBloquerNetNegatif() != null ? dto.getBloquerNetNegatif() : "N");
        entity.setTaxeChaqueNPaies(dto.getTaxeChaqueNPaies() != null ? dto.getTaxeChaqueNPaies() : 0);
        entity.setSuppChaqueNPaies(dto.getSuppChaqueNPaies() != null ? dto.getSuppChaqueNPaies() : 0);
        entity.setSuppDecalageNbPaies(dto.getSuppDecalageNbPaies() != null ? dto.getSuppDecalageNbPaies() : 0);
        entity.setAutoTraitement(dto.getAutoTraitement() != null ? dto.getAutoTraitement() : "N");
        entity.setNiveauAutoTraitement(dto.getNiveauAutoTraitement() != null ? dto.getNiveauAutoTraitement() : RegimePaie.NiveauAutoTraitement.AUCUN);
        entity.setHeuresMinJour(dto.getHeuresMinJour());
        entity.setPayerSiMoinsMin(dto.getPayerSiMoinsMin() != null ? dto.getPayerSiMoinsMin() : "Y");
        entity.setRetardsMaxJour(dto.getRetardsMaxJour() != null ? dto.getRetardsMaxJour() : 0);
        entity.setPaiementSurCompte(dto.getPaiementSurCompte() != null ? dto.getPaiementSurCompte() : "Y");
        entity.setTaxeSurDernierNetPositif(dto.getTaxeSurDernierNetPositif() != null ? dto.getTaxeSurDernierNetPositif() : "Y");
        entity.setTaxable(dto.getTaxable() != null ? dto.getTaxable() : "Y");
        
        if (dto.getResponsableId() != null) {
            Employe responsable = employeRepository.findById(dto.getResponsableId())
                    .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getResponsableId()));
            entity.setResponsable(responsable);
        } else {
            entity.setResponsable(null);
        }
        
        entity.setDernierePaie(dto.getDernierePaie());
        entity.setProchainePaie(dto.getProchainePaie());
        entity.setDernierPrelevement(dto.getDernierPrelevement());
        entity.setDernierSupplement(dto.getDernierSupplement());
        entity.setProchainSupplement(dto.getProchainSupplement());
        
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        RegimePaie saved = repository.save(entity);
        return toDTO(saved);
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        RegimePaie entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }

    @Transactional
    public RegimePaieDTO cloneRegimePaie(Long id, String newCodeRegimePaie, String username) {
        if (repository.existsByCodeRegimePaie(newCodeRegimePaie)) {
            throw new RuntimeException("RegimePaie with code " + newCodeRegimePaie + " already exists");
        }
        RegimePaie source = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + id));

        RegimePaie clone = new RegimePaie();
        clone.setCodeRegimePaie(newCodeRegimePaie);
        clone.setDescription(source.getDescription());
        clone.setModeRemuneration(source.getModeRemuneration());
        clone.setPeriodicite(source.getPeriodicite());
        clone.setDevise(source.getDevise());
        clone.setHoraireActif(source.getHoraireActif());
        clone.setJoursPayes(source.getJoursPayes());
        clone.setSuppAuto(source.getSuppAuto());
        clone.setBloquerNetNegatif(source.getBloquerNetNegatif());
        clone.setTaxeChaqueNPaies(source.getTaxeChaqueNPaies());
        clone.setSuppChaqueNPaies(source.getSuppChaqueNPaies());
        clone.setSuppDecalageNbPaies(source.getSuppDecalageNbPaies());
        clone.setAutoTraitement(source.getAutoTraitement());
        clone.setNiveauAutoTraitement(source.getNiveauAutoTraitement());
        clone.setHeuresMinJour(source.getHeuresMinJour());
        clone.setPayerSiMoinsMin(source.getPayerSiMoinsMin());
        clone.setRetardsMaxJour(source.getRetardsMaxJour());
        clone.setPaiementSurCompte(source.getPaiementSurCompte());
        clone.setTaxeSurDernierNetPositif(source.getTaxeSurDernierNetPositif());
        clone.setTaxable(source.getTaxable());
        clone.setResponsable(source.getResponsable());
        clone.setDernierePaie(source.getDernierePaie());
        clone.setProchainePaie(source.getProchainePaie());
        clone.setDernierPrelevement(source.getDernierPrelevement());
        clone.setDernierSupplement(source.getDernierSupplement());
        clone.setProchainSupplement(source.getProchainSupplement());
        clone.setCreatedBy(username);
        clone.setCreatedOn(OffsetDateTime.now());
        clone.setRowscn(1);

        RegimePaie saved = repository.save(clone);

        List<RegimePaieDeduction> deductions = regimePaieDeductionRepository.findByRegimePaieId(source.getId());
        for (RegimePaieDeduction sourceDeduction : deductions) {
            RegimePaieDeduction copy = new RegimePaieDeduction();
            copy.setRegimePaie(saved);
            copy.setDeductionCode(sourceDeduction.getDeductionCode());
            copy.setExclusif(sourceDeduction.getExclusif());
            copy.setCreatedBy(username);
            copy.setCreatedOn(OffsetDateTime.now());
            copy.setRowscn(1);
            regimePaieDeductionRepository.save(copy);
        }

        return toDTO(saved);
    }
    
    private RegimePaieDTO toDTO(RegimePaie entity) {
        RegimePaieDTO dto = new RegimePaieDTO();
        dto.setId(entity.getId());
        dto.setCodeRegimePaie(entity.getCodeRegimePaie());
        dto.setDescription(entity.getDescription());
        dto.setModeRemuneration(entity.getModeRemuneration());
        dto.setPeriodicite(entity.getPeriodicite());
        
        if (entity.getDevise() != null) {
            dto.setDeviseId(entity.getDevise().getId());
            dto.setDeviseCode(entity.getDevise().getCodeDevise());
            dto.setDeviseDescription(entity.getDevise().getDescription());
        }
        
        dto.setHoraireActif(entity.getHoraireActif());
        dto.setJoursPayes(entity.getJoursPayes());
        dto.setSuppAuto(entity.getSuppAuto());
        dto.setBloquerNetNegatif(entity.getBloquerNetNegatif());
        dto.setTaxeChaqueNPaies(entity.getTaxeChaqueNPaies());
        dto.setSuppChaqueNPaies(entity.getSuppChaqueNPaies());
        dto.setSuppDecalageNbPaies(entity.getSuppDecalageNbPaies());
        dto.setAutoTraitement(entity.getAutoTraitement());
        dto.setNiveauAutoTraitement(entity.getNiveauAutoTraitement());
        dto.setHeuresMinJour(entity.getHeuresMinJour());
        dto.setPayerSiMoinsMin(entity.getPayerSiMoinsMin());
        dto.setRetardsMaxJour(entity.getRetardsMaxJour());
        dto.setPaiementSurCompte(entity.getPaiementSurCompte());
        dto.setTaxeSurDernierNetPositif(entity.getTaxeSurDernierNetPositif());
        dto.setTaxable(entity.getTaxable());
        
        if (entity.getResponsable() != null) {
            dto.setResponsableId(entity.getResponsable().getId());
            dto.setResponsableCodeEmploye(entity.getResponsable().getCodeEmploye());
            dto.setResponsableNom(entity.getResponsable().getNom());
            dto.setResponsablePrenom(entity.getResponsable().getPrenom());
        }
        
        dto.setDernierePaie(entity.getDernierePaie());
        dto.setProchainePaie(entity.getProchainePaie());
        dto.setDernierPrelevement(entity.getDernierPrelevement());
        dto.setDernierSupplement(entity.getDernierSupplement());
        dto.setProchainSupplement(entity.getProchainSupplement());
        
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        return dto;
    }
}
