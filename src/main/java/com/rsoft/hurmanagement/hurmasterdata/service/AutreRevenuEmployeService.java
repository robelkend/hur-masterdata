package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.AutreRevenuEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AutreRevenuEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AutreRevenuEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AutreRevenuValidationRangeRequestDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AutreRevenuValidationRangeResultDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AutreRevenuEmployeService {

    private final AutreRevenuEmployeRepository repository;
    private final EmployeRepository employeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final TypeRevenuRepository typeRevenuRepository;
    private final DeviseRepository deviseRepository;
    private final RegimePaieRepository regimePaieRepository;

    @Transactional(readOnly = true)
    public Page<AutreRevenuEmployeDTO> findAllWithFilters(
            Long entrepriseId,
            Long employeId,
            Long typeRevenuId,
            String statut,
            LocalDate dateDebut,
            LocalDate dateFin,
            Pageable pageable) {
        
        AutreRevenuEmploye.StatutAutreRevenu statutEnum = null;
        if (statut != null && !statut.trim().isEmpty()) {
            try {
                statutEnum = AutreRevenuEmploye.StatutAutreRevenu.valueOf(statut);
            } catch (IllegalArgumentException e) {
                // Invalid statut, will be ignored
            }
        }

        return repository.findAllWithFilters(entrepriseId, employeId, typeRevenuId, statutEnum, dateDebut, dateFin, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public AutreRevenuEmployeDTO findById(Long id) {
        AutreRevenuEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AutreRevenuEmploye not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional
    public AutreRevenuEmployeDTO create(AutreRevenuEmployeCreateDTO dto, String username) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));

        TypeRevenu typeRevenu = typeRevenuRepository.findById(dto.getTypeRevenuId())
                .orElseThrow(() -> new RuntimeException("TypeRevenu not found with id: " + dto.getTypeRevenuId()));

        Devise devise = deviseRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));

        AutreRevenuEmploye entity = new AutreRevenuEmploye();
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        }

        entity.setEmploye(employe);
        entity.setTypeRevenu(typeRevenu);
        entity.setDateRevenu(dto.getDateRevenu());
        entity.setDateEffet(dto.getDateEffet());
        entity.setDevise(devise);
        entity.setMontant(dto.getMontant());
        entity.setCommentaire(dto.getCommentaire());
        entity.setModeInclusion(AutreRevenuEmploye.ModeInclusion.valueOf(dto.getModeInclusion()));

        if (dto.getRegimePaieId() != null) {
            RegimePaie regimePaie = regimePaieRepository.findById(dto.getRegimePaieId())
                    .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + dto.getRegimePaieId()));
            entity.setRegimePaie(regimePaie);
        }

        entity.setDateInclusion(dto.getDateInclusion());
        entity.setReference(dto.getReference());
        entity.setStatut(AutreRevenuEmploye.StatutAutreRevenu.valueOf(dto.getStatut()));
        entity.setPayrollNo(0);

        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public AutreRevenuEmployeDTO update(Long id, AutreRevenuEmployeUpdateDTO dto, String username) {
        AutreRevenuEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AutreRevenuEmploye not found with id: " + id));

        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }

        // Can only update if status is BROUILLON
        if (entity.getStatut() != AutreRevenuEmploye.StatutAutreRevenu.BROUILLON) {
            throw new RuntimeException("Cannot update autre revenu with status: " + entity.getStatut() + ". Only BROUILLON records can be updated.");
        }

        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        } else {
            entity.setEntreprise(null);
        }

        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        entity.setEmploye(employe);

        TypeRevenu typeRevenu = typeRevenuRepository.findById(dto.getTypeRevenuId())
                .orElseThrow(() -> new RuntimeException("TypeRevenu not found with id: " + dto.getTypeRevenuId()));
        entity.setTypeRevenu(typeRevenu);

        Devise devise = deviseRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
        entity.setDevise(devise);

        entity.setDateRevenu(dto.getDateRevenu());
        entity.setDateEffet(dto.getDateEffet());
        entity.setMontant(dto.getMontant());
        entity.setCommentaire(dto.getCommentaire());
        entity.setModeInclusion(AutreRevenuEmploye.ModeInclusion.valueOf(dto.getModeInclusion()));

        if (dto.getRegimePaieId() != null) {
            RegimePaie regimePaie = regimePaieRepository.findById(dto.getRegimePaieId())
                    .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + dto.getRegimePaieId()));
            entity.setRegimePaie(regimePaie);
        } else {
            entity.setRegimePaie(null);
        }

        entity.setDateInclusion(dto.getDateInclusion());
        entity.setReference(dto.getReference());
        entity.setStatut(AutreRevenuEmploye.StatutAutreRevenu.valueOf(dto.getStatut()));

        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        AutreRevenuEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AutreRevenuEmploye not found with id: " + id));

        // Can only delete if status is BROUILLON
        if (entity.getStatut() != AutreRevenuEmploye.StatutAutreRevenu.BROUILLON) {
            throw new RuntimeException("Cannot delete autre revenu with status: " + entity.getStatut() + ". Only BROUILLON records can be deleted.");
        }

        repository.delete(entity);
    }

    @Transactional
    public AutreRevenuEmployeDTO valider(Long id, String username) {
        AutreRevenuEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AutreRevenuEmploye not found with id: " + id));

        if (entity.getStatut() != AutreRevenuEmploye.StatutAutreRevenu.BROUILLON) {
            throw new RuntimeException("Cannot validate autre revenu with status: " + entity.getStatut() + ". Only BROUILLON records can be validated.");
        }

        entity.setStatut(AutreRevenuEmploye.StatutAutreRevenu.VALIDE);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public AutreRevenuEmployeDTO rejeter(Long id, String username) {
        AutreRevenuEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AutreRevenuEmploye not found with id: " + id));

        if (entity.getStatut() != AutreRevenuEmploye.StatutAutreRevenu.BROUILLON) {
            throw new RuntimeException("Cannot reject autre revenu with status: " + entity.getStatut() + ". Only BROUILLON records can be rejected.");
        }

        entity.setStatut(AutreRevenuEmploye.StatutAutreRevenu.REJETE);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public AutreRevenuEmployeDTO annuler(Long id, String username) {
        AutreRevenuEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AutreRevenuEmploye not found with id: " + id));

        if (entity.getStatut() != AutreRevenuEmploye.StatutAutreRevenu.VALIDE) {
            throw new RuntimeException("Cannot cancel autre revenu with status: " + entity.getStatut() + ". Only VALIDE records can be cancelled.");
        }

        entity.setStatut(AutreRevenuEmploye.StatutAutreRevenu.ANNULE);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public AutreRevenuValidationRangeResultDTO validerParPlage(AutreRevenuValidationRangeRequestDTO request, String username) {
        validateDateRange(request.getDateDebut(), request.getDateFin());
        int updated = repository.validateDraftsInRange(
                request.getDateDebut(),
                request.getDateFin(),
                request.getEntrepriseId(),
                request.getEmployeId(),
                request.getTypeRevenuId(),
                username,
                OffsetDateTime.now());
        return new AutreRevenuValidationRangeResultDTO(updated);
    }

    @Transactional
    public AutreRevenuValidationRangeResultDTO devaliderParPlage(AutreRevenuValidationRangeRequestDTO request, String username) {
        validateDateRange(request.getDateDebut(), request.getDateFin());
        int updated = repository.devalidateValidatedInRange(
                request.getDateDebut(),
                request.getDateFin(),
                request.getEntrepriseId(),
                request.getEmployeId(),
                request.getTypeRevenuId(),
                username,
                OffsetDateTime.now());
        return new AutreRevenuValidationRangeResultDTO(updated);
    }

    private void validateDateRange(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null || dateFin == null) {
            throw new RuntimeException("dateDebut and dateFin are required");
        }
        if (dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("dateFin must be after or equal to dateDebut");
        }
    }

    private AutreRevenuEmployeDTO toDTO(AutreRevenuEmploye entity) {
        AutreRevenuEmployeDTO dto = new AutreRevenuEmployeDTO();
        dto.setId(entity.getId());
        
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        
        dto.setEmployeId(entity.getEmploye().getId());
        dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
        dto.setEmployeNom(entity.getEmploye().getNom());
        dto.setEmployePrenom(entity.getEmploye().getPrenom());
        
        dto.setTypeRevenuId(entity.getTypeRevenu().getId());
        dto.setTypeRevenuCode(entity.getTypeRevenu().getCodeRevenu());
        dto.setTypeRevenuDescription(entity.getTypeRevenu().getDescription());
        
        dto.setDateRevenu(entity.getDateRevenu());
        dto.setDateEffet(entity.getDateEffet());
        
        dto.setDeviseId(entity.getDevise().getId());
        dto.setDeviseCode(entity.getDevise().getCodeDevise());
        dto.setDeviseDescription(entity.getDevise().getDescription());
        
        dto.setMontant(entity.getMontant());
        dto.setCommentaire(entity.getCommentaire());
        dto.setModeInclusion(entity.getModeInclusion().name());
        
        if (entity.getRegimePaie() != null) {
            dto.setRegimePaieId(entity.getRegimePaie().getId());
            dto.setRegimePaieCode(entity.getRegimePaie().getCodeRegimePaie());
            dto.setRegimePaieDescription(entity.getRegimePaie().getDescription());
        }
        
        dto.setDateInclusion(entity.getDateInclusion());
        dto.setReference(entity.getReference());
        dto.setStatut(entity.getStatut().name());
        dto.setPayrollNo(entity.getPayrollNo());
        
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        return dto;
    }
}
