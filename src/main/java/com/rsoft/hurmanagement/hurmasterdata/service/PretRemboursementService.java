package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PretRemboursementCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PretRemboursementDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PretRemboursementUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.PretEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.PretRemboursement;
import com.rsoft.hurmanagement.hurmasterdata.repository.PretEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PretRemboursementRepository;
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
public class PretRemboursementService {
    
    private final PretRemboursementRepository repository;
    private final PretEmployeRepository pretEmployeRepository;
    private final PretEmployeService pretEmployeService;
    
    @Transactional(readOnly = true)
    public List<PretRemboursementDTO> findByPretEmployeId(Long pretEmployeId) {
        return repository.findByPretEmployeId(pretEmployeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<PretRemboursementDTO> findByPretEmployeId(Long pretEmployeId, Pageable pageable) {
        return repository.findByPretEmployeId(pretEmployeId, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public PretRemboursementDTO findById(Long id) {
        PretRemboursement entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PretRemboursement not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public PretRemboursementDTO create(PretRemboursementCreateDTO dto, String username) {
        PretEmploye pretEmploye = pretEmployeRepository.findById(dto.getPretEmployeId())
                .orElseThrow(() -> new RuntimeException("PretEmploye not found with id: " + dto.getPretEmployeId()));
        
        // Cannot add remboursement if pret is not EN_COURS
        if (pretEmploye.getStatut() != PretEmploye.StatutPret.EN_COURS) {
            throw new RuntimeException("Cannot add remboursement to pret with status: " + pretEmploye.getStatut() + ". Only EN_COURS prets can have remboursements.");
        }
        
        PretRemboursement entity = new PretRemboursement();
        entity.setPretEmploye(pretEmploye);
        entity.setDateRemboursement(dto.getDateRemboursement());
        entity.setMontantRembourse(dto.getMontantRembourse());
        entity.setMontantInteret(dto.getMontantInteret() != null ? dto.getMontantInteret() : BigDecimal.ZERO);
        entity.setMontantTotal(dto.getMontantTotal() != null ? dto.getMontantTotal() : 
                dto.getMontantRembourse().add(entity.getMontantInteret()));
        entity.setOrigine(PretRemboursement.OrigineRemboursement.valueOf(dto.getOrigine()));
        entity.setNoPayroll(dto.getNoPayroll() != null ? dto.getNoPayroll() : 0);
        entity.setCommentaire(dto.getCommentaire());
        entity.setStatut(PretRemboursement.StatutRemboursement.BROUILLON);
        
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        PretRemboursement saved = repository.save(entity);
        
        // Recalculate montant_verse in PretEmploye
        pretEmployeService.calculerMontantVerse(dto.getPretEmployeId());
        
        return toDTO(saved);
    }
    
    @Transactional
    public PretRemboursementDTO update(Long id, PretRemboursementUpdateDTO dto, String username) {
        PretRemboursement entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PretRemboursement not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Can only update if origine is MANUEL
        if (entity.getOrigine() != PretRemboursement.OrigineRemboursement.MANUEL) {
            throw new RuntimeException("Cannot update remboursement with origine: " + entity.getOrigine() + ". Only MANUEL remboursements can be updated.");
        }
        
        Long pretEmployeId = entity.getPretEmploye().getId();
        
        entity.setDateRemboursement(dto.getDateRemboursement());
        entity.setMontantRembourse(dto.getMontantRembourse());
        entity.setMontantInteret(dto.getMontantInteret());
        entity.setMontantTotal(dto.getMontantTotal());
        entity.setOrigine(PretRemboursement.OrigineRemboursement.valueOf(dto.getOrigine()));
        entity.setNoPayroll(dto.getNoPayroll());
        entity.setCommentaire(dto.getCommentaire());
        
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        
        PretRemboursement saved = repository.save(entity);
        
        // Recalculate montant_verse in PretEmploye
        pretEmployeService.calculerMontantVerse(pretEmployeId);
        
        return toDTO(saved);
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        PretRemboursement entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PretRemboursement not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Can only delete if origine is MANUEL
        if (entity.getOrigine() != PretRemboursement.OrigineRemboursement.MANUEL) {
            throw new RuntimeException("Cannot delete remboursement with origine: " + entity.getOrigine() + ". Only MANUEL remboursements can be deleted.");
        }
        
        Long pretEmployeId = entity.getPretEmploye().getId();
        
        repository.delete(entity);
        
        // Recalculate montant_verse in PretEmploye
        pretEmployeService.calculerMontantVerse(pretEmployeId);
    }
    
    @Transactional
    public BigDecimal calculerInteret(PretEmploye pretEmploye, BigDecimal montantRembourse) {
        if (pretEmploye.getTypeInteret() == null || pretEmploye.getTauxInteret() == null) {
            return BigDecimal.ZERO;
        }
        
        if (pretEmploye.getTypeInteret() == PretEmploye.TypeInteret.PLAT) {
            // Flat interest rate
            return pretEmploye.getTauxInteret();
        } else if (pretEmploye.getTypeInteret() == PretEmploye.TypeInteret.POURCENTAGE) {
            // Percentage interest rate on remaining amount
            BigDecimal montantRestant = pretEmploye.getMontantPret().subtract(pretEmploye.getMontantVerse());
            return montantRestant.multiply(pretEmploye.getTauxInteret()).divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
        }
        
        return BigDecimal.ZERO;
    }
    
    private PretRemboursementDTO toDTO(PretRemboursement entity) {
        PretRemboursementDTO dto = new PretRemboursementDTO();
        dto.setId(entity.getId());
        dto.setPretEmployeId(entity.getPretEmploye().getId());
        dto.setDateRemboursement(entity.getDateRemboursement());
        dto.setMontantRembourse(entity.getMontantRembourse());
        dto.setMontantInteret(entity.getMontantInteret());
        dto.setMontantTotal(entity.getMontantTotal());
        dto.setOrigine(entity.getOrigine() != null ? entity.getOrigine().name() : null);
        dto.setNoPayroll(entity.getNoPayroll());
        dto.setStatut(entity.getStatut() != null ? entity.getStatut().name() : null);
        dto.setCommentaire(entity.getCommentaire());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
