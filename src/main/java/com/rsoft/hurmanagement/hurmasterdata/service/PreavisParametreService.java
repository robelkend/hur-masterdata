package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PreavisParametreCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PreavisParametreDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PreavisParametreUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreavisParametreService {
    
    private final PreavisParametreRepository repository;
    private final TypeEmployeRepository typeEmployeRepository;
    private final RegimePaieRepository regimePaieRepository;
    private final EntrepriseRepository entrepriseRepository;
    
    @Transactional(readOnly = true)
    public Page<PreavisParametreDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<PreavisParametreDTO> findByEntrepriseId(Long entrepriseId, Pageable pageable) {
        return repository.findByEntrepriseId(entrepriseId, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public PreavisParametreDTO findById(Long id) {
        PreavisParametre entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreavisParametre not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public PreavisParametreDTO create(PreavisParametreCreateDTO dto, String username) {
        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
        
        // Validate anciennete range
        if (dto.getAncienneteMax() != null && dto.getAncienneteMax() < dto.getAncienneteMin()) {
            throw new RuntimeException("Anciennete max must be >= anciennete min");
        }
        
        PreavisParametre entity = new PreavisParametre();
        
        if (dto.getTypeEmployeId() != null) {
            TypeEmploye typeEmploye = typeEmployeRepository.findById(dto.getTypeEmployeId())
                    .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + dto.getTypeEmployeId()));
            entity.setTypeEmploye(typeEmploye);
        } else {
            entity.setTypeEmploye(null);
        }
        
        if (dto.getRegimePaieId() != null) {
            RegimePaie regimePaie = regimePaieRepository.findById(dto.getRegimePaieId())
                    .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + dto.getRegimePaieId()));
            entity.setRegimePaie(regimePaie);
        } else {
            entity.setRegimePaie(null);
        }
        
        entity.setTypeDepart(PreavisParametre.TypeDepart.valueOf(dto.getTypeDepart()));
        entity.setAncienneteMin(dto.getAncienneteMin());
        entity.setAncienneteMax(dto.getAncienneteMax());
        entity.setInclureMax(dto.getInclureMax());
        entity.setValeurPreavis(dto.getValeurPreavis());
        entity.setUnitePreavis(PreavisParametre.UnitePreavis.valueOf(dto.getUnitePreavis()));
        entity.setModeApplication(PreavisParametre.ModeApplication.valueOf(dto.getModeApplication()));
        entity.setPriorite(dto.getPriorite() != null ? dto.getPriorite() : 1);
        entity.setEntreprise(entreprise);
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public PreavisParametreDTO update(Long id, PreavisParametreUpdateDTO dto, String username) {
        PreavisParametre entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreavisParametre not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
        
        // Validate anciennete range
        if (dto.getAncienneteMax() != null && dto.getAncienneteMax() < dto.getAncienneteMin()) {
            throw new RuntimeException("Anciennete max must be >= anciennete min");
        }
        
        if (dto.getTypeEmployeId() != null) {
            TypeEmploye typeEmploye = typeEmployeRepository.findById(dto.getTypeEmployeId())
                    .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + dto.getTypeEmployeId()));
            entity.setTypeEmploye(typeEmploye);
        } else {
            entity.setTypeEmploye(null);
        }
        
        if (dto.getRegimePaieId() != null) {
            RegimePaie regimePaie = regimePaieRepository.findById(dto.getRegimePaieId())
                    .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + dto.getRegimePaieId()));
            entity.setRegimePaie(regimePaie);
        } else {
            entity.setRegimePaie(null);
        }
        
        entity.setTypeDepart(PreavisParametre.TypeDepart.valueOf(dto.getTypeDepart()));
        entity.setAncienneteMin(dto.getAncienneteMin());
        entity.setAncienneteMax(dto.getAncienneteMax());
        entity.setInclureMax(dto.getInclureMax());
        entity.setValeurPreavis(dto.getValeurPreavis());
        entity.setUnitePreavis(PreavisParametre.UnitePreavis.valueOf(dto.getUnitePreavis()));
        entity.setModeApplication(PreavisParametre.ModeApplication.valueOf(dto.getModeApplication()));
        entity.setPriorite(dto.getPriorite());
        entity.setEntreprise(entreprise);
        entity.setActif(dto.getActif());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        PreavisParametre entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreavisParametre not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private PreavisParametreDTO toDTO(PreavisParametre entity) {
        PreavisParametreDTO dto = new PreavisParametreDTO();
        dto.setId(entity.getId());
        dto.setTypeEmployeId(entity.getTypeEmploye() != null ? entity.getTypeEmploye().getId() : null);
        dto.setTypeEmployeDescription(entity.getTypeEmploye() != null ? entity.getTypeEmploye().getDescription() : null);
        dto.setRegimePaieId(entity.getRegimePaie() != null ? entity.getRegimePaie().getId() : null);
        dto.setRegimePaieCode(entity.getRegimePaie() != null ? entity.getRegimePaie().getCodeRegimePaie() : null);
        dto.setRegimePaieDescription(entity.getRegimePaie() != null ? entity.getRegimePaie().getDescription() : null);
        dto.setTypeDepart(entity.getTypeDepart() != null ? entity.getTypeDepart().name() : null);
        dto.setAncienneteMin(entity.getAncienneteMin());
        dto.setAncienneteMax(entity.getAncienneteMax());
        dto.setInclureMax(entity.getInclureMax());
        dto.setValeurPreavis(entity.getValeurPreavis());
        dto.setUnitePreavis(entity.getUnitePreavis() != null ? entity.getUnitePreavis().name() : null);
        dto.setModeApplication(entity.getModeApplication() != null ? entity.getModeApplication().name() : null);
        dto.setPriorite(entity.getPriorite());
        dto.setEntrepriseId(entity.getEntreprise() != null ? entity.getEntreprise().getId() : null);
        dto.setEntrepriseCode(entity.getEntreprise() != null ? entity.getEntreprise().getCodeEntreprise() : null);
        dto.setEntrepriseNom(entity.getEntreprise() != null ? entity.getEntreprise().getNomEntreprise() : null);
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
