package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.SanctionEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SanctionEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SanctionEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanctionEmployeService {
    
    private final SanctionEmployeRepository repository;
    private final EmployeRepository employeRepository;
    private final BaremeSanctionRepository baremeSanctionRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    
    @Transactional(readOnly = true)
    public Page<SanctionEmployeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<SanctionEmployeDTO> findByEmployeId(Long employeId, Pageable pageable) {
        return repository.findByEmployeId(employeId, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<SanctionEmployeDTO> findByFilters(Long employeId, String statut, Long entrepriseId, Pageable pageable) {
        SanctionEmploye.StatutSanction statutEnum = null;
        if (statut != null && !statut.trim().isEmpty()) {
            try {
                statutEnum = SanctionEmploye.StatutSanction.valueOf(statut);
            } catch (IllegalArgumentException e) {
                // Invalid statut, will be ignored
            }
        }
        return repository.findByFilters(employeId, statutEnum, entrepriseId, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public SanctionEmployeDTO findById(Long id) {
        SanctionEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanctionEmploye not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public SanctionEmployeDTO create(SanctionEmployeCreateDTO dto, String username) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        
        SanctionEmploye entity = new SanctionEmploye();
        entity.setEmploye(employe);
        entity.setEmploiEmploye(resolveEmploiEmploye(dto.getEmployeId(), dto.getEmploiEmployeId()));
        entity.setDateSanction(dto.getDateSanction() != null ? dto.getDateSanction() : java.time.LocalDate.now());
        
        if (dto.getTypeEvenement() != null) {
            entity.setTypeEvenement(SanctionEmploye.TypeEvenement.valueOf(dto.getTypeEvenement()));
        }
        entity.setValeurMesuree(dto.getValeurMesuree());
        if (dto.getUniteMesure() != null) {
            entity.setUniteMesure(SanctionEmploye.UniteMesure.valueOf(dto.getUniteMesure()));
        }
        
        if (dto.getRegleId() != null) {
            BaremeSanction regle = baremeSanctionRepository.findById(dto.getRegleId())
                    .orElseThrow(() -> new RuntimeException("BaremeSanction not found with id: " + dto.getRegleId()));
            entity.setRegle(regle);
        }
        
        if (dto.getTypeSanction() != null) {
            entity.setTypeSanction(SanctionEmploye.TypeSanction.valueOf(dto.getTypeSanction()));
        }
        entity.setValeurSanction(dto.getValeurSanction());
        if (dto.getUniteSanction() != null) {
            entity.setUniteSanction(SanctionEmploye.UniteSanction.valueOf(dto.getUniteSanction()));
        }
        entity.setMontantCalcule(dto.getMontantCalcule());
        
        if (dto.getStatut() != null) {
            entity.setStatut(SanctionEmploye.StatutSanction.valueOf(dto.getStatut()));
        } else {
            entity.setStatut(SanctionEmploye.StatutSanction.NOUVEAU);
        }
        
        entity.setMotif(dto.getMotif());
        entity.setReferenceExterne(dto.getReferenceExterne());
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        }
        
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public SanctionEmployeDTO update(Long id, SanctionEmployeUpdateDTO dto, String username) {
        SanctionEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanctionEmploye not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        entity.setEmploye(employe);
        entity.setEmploiEmploye(resolveEmploiEmploye(dto.getEmployeId(), dto.getEmploiEmployeId()));
        
        entity.setDateSanction(dto.getDateSanction());
        
        if (dto.getTypeEvenement() != null) {
            entity.setTypeEvenement(SanctionEmploye.TypeEvenement.valueOf(dto.getTypeEvenement()));
        }
        entity.setValeurMesuree(dto.getValeurMesuree());
        if (dto.getUniteMesure() != null) {
            entity.setUniteMesure(SanctionEmploye.UniteMesure.valueOf(dto.getUniteMesure()));
        }
        
        if (dto.getRegleId() != null) {
            BaremeSanction regle = baremeSanctionRepository.findById(dto.getRegleId())
                    .orElseThrow(() -> new RuntimeException("BaremeSanction not found with id: " + dto.getRegleId()));
            entity.setRegle(regle);
        } else {
            entity.setRegle(null);
        }
        
        if (dto.getTypeSanction() != null) {
            entity.setTypeSanction(SanctionEmploye.TypeSanction.valueOf(dto.getTypeSanction()));
        }
        entity.setValeurSanction(dto.getValeurSanction());
        if (dto.getUniteSanction() != null) {
            entity.setUniteSanction(SanctionEmploye.UniteSanction.valueOf(dto.getUniteSanction()));
        }
        entity.setMontantCalcule(dto.getMontantCalcule());
        
        if (dto.getStatut() != null) {
            entity.setStatut(SanctionEmploye.StatutSanction.valueOf(dto.getStatut()));
        }
        
        entity.setMotif(dto.getMotif());
        entity.setReferenceExterne(dto.getReferenceExterne());
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        } else {
            entity.setEntreprise(null);
        }
        
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        SanctionEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanctionEmploye not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private SanctionEmployeDTO toDTO(SanctionEmploye entity) {
        SanctionEmployeDTO dto = new SanctionEmployeDTO();
        dto.setId(entity.getId());
        
        if (entity.getEmploye() != null) {
            dto.setEmployeId(entity.getEmploye().getId());
            dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
            dto.setEmployeNom(entity.getEmploye().getNom());
            dto.setEmployePrenom(entity.getEmploye().getPrenom());
        }
        if (entity.getEmploiEmploye() != null) {
            dto.setEmploiEmployeId(entity.getEmploiEmploye().getId());
        }
        
        dto.setDateSanction(entity.getDateSanction());
        dto.setTypeEvenement(entity.getTypeEvenement() != null ? entity.getTypeEvenement().name() : null);
        dto.setValeurMesuree(entity.getValeurMesuree());
        dto.setUniteMesure(entity.getUniteMesure() != null ? entity.getUniteMesure().name() : null);
        
        if (entity.getRegle() != null) {
            dto.setRegleId(entity.getRegle().getId());
            // Description could be built from regle fields if needed
        }
        
        dto.setTypeSanction(entity.getTypeSanction() != null ? entity.getTypeSanction().name() : null);
        dto.setValeurSanction(entity.getValeurSanction());
        dto.setUniteSanction(entity.getUniteSanction() != null ? entity.getUniteSanction().name() : null);
        dto.setMontantCalcule(entity.getMontantCalcule());
        dto.setStatut(entity.getStatut() != null ? entity.getStatut().name() : null);
        dto.setMotif(entity.getMotif());
        dto.setReferenceExterne(entity.getReferenceExterne());
        
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        return dto;
    }

    private EmploiEmploye resolveEmploiEmploye(Long employeId, Long emploiEmployeId) {
        if (employeId == null) {
            return null;
        }
        if (emploiEmployeId != null) {
            EmploiEmploye emploi = emploiEmployeRepository.findById(emploiEmployeId)
                    .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiEmployeId));
            if (!emploi.getEmploye().getId().equals(employeId)) {
                throw new RuntimeException("EmploiEmploye does not belong to employe " + employeId);
            }
            if (emploi.getStatutEmploi() == EmploiEmploye.StatutEmploi.TERMINE) {
                throw new RuntimeException("EmploiEmploye is terminated and cannot be used.");
            }
            return emploi;
        }

        List<EmploiEmploye> emplois = emploiEmployeRepository.findByEmployeIdAndStatutEmploiNot(
                employeId,
                EmploiEmploye.StatutEmploi.TERMINE);
        if (emplois.isEmpty()) {
            throw new RuntimeException("No non-terminated emploi found for employe " + employeId);
        }
        if (emplois.size() > 1) {
            throw new RuntimeException("Multiple non-terminated emplois found for employe " + employeId + ". Please specify emploiEmployeId.");
        }
        return emplois.get(0);
    }
}
