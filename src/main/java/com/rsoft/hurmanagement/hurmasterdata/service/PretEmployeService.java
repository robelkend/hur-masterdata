package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PretEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PretEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PretEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PretEmployeService {
    
    private final PretEmployeRepository repository;
    private final PretRemboursementRepository remboursementRepository;
    private final EmployeRepository employeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final DeviseRepository deviseRepository;
    private final RegimePaieRepository regimePaieRepository;
    private final TypeRevenuRepository typeRevenuRepository;
    
    @Transactional(readOnly = true)
    public Page<PretEmployeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<PretEmployeDTO> findAllWithFilters(Long employeId, String statut, String avance, LocalDate dateDebut, LocalDate dateFin, Pageable pageable) {
        PretEmploye.StatutPret statutEnum = null;
        if (statut != null && !statut.trim().isEmpty()) {
            try {
                statutEnum = PretEmploye.StatutPret.valueOf(statut);
            } catch (IllegalArgumentException e) {
                // Invalid statut, will be ignored
            }
        }
        
        return repository.findAllWithFilters(employeId, statutEnum, avance, dateDebut, dateFin, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public PretEmployeDTO findById(Long id) {
        PretEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PretEmploye not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public PretEmployeDTO create(PretEmployeCreateDTO dto, String username) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        
        Devise devise = deviseRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
        
        PretEmploye entity = new PretEmploye();
        entity.setEmploye(employe);
        entity.setDatePret(dto.getDatePret());
        entity.setDevise(devise);
        entity.setMontantPret(dto.getMontantPret());
        entity.setMontantSubvention(dto.getMontantSubvention() != null ? dto.getMontantSubvention() : BigDecimal.ZERO);
        entity.setPeriodicite(PretEmploye.PeriodicitePret.valueOf(dto.getPeriodicite()));
        entity.setPreleverDansPayroll(resolvePreleverDansPayroll(entity.getPeriodicite()));
        entity.setPrelevementPartiel(dto.getPrelevementPartiel() != null ? dto.getPrelevementPartiel() : "N");
        entity.setNbPrevu(dto.getNbPrevu() != null ? dto.getNbPrevu() : 1);
        entity.setMontantPeriode(resolveMontantPeriode(entity.getMontantPret(), entity.getNbPrevu()));
        entity.setMontantVerse(BigDecimal.ZERO);
        entity.setFrequenceNbPeriodicites(dto.getFrequenceNbPeriodicites() != null ? dto.getFrequenceNbPeriodicites() : 1);
        entity.setFrequenceCompteur(0);
        entity.setPremierPrelevement(dto.getPremierPrelevement());
        entity.setDernierPrelevement(null);
        
        if (dto.getTypeInteret() != null) {
            entity.setTypeInteret(PretEmploye.TypeInteret.valueOf(dto.getTypeInteret()));
        }
        entity.setTauxInteret(dto.getTauxInteret() != null ? dto.getTauxInteret() : BigDecimal.ZERO);
        entity.setAvance(dto.getAvance() != null ? dto.getAvance() : "N");
        entity.setLibelle(dto.getLibelle());
        entity.setNote(dto.getNote());
        entity.setOrdre(dto.getOrdre() != null ? dto.getOrdre() : 1);
        
        if (dto.getRegimePaieId() != null) {
            RegimePaie regimePaie = regimePaieRepository.findById(dto.getRegimePaieId())
                    .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + dto.getRegimePaieId()));
            entity.setRegimePaie(regimePaie);
        }
        
        if (dto.getTypeRevenuId() != null) {
            TypeRevenu typeRevenu = typeRevenuRepository.findById(dto.getTypeRevenuId())
                    .orElseThrow(() -> new RuntimeException("TypeRevenu not found with id: " + dto.getTypeRevenuId()));
            entity.setTypeRevenu(typeRevenu);
        }
        
        entity.setStatut(PretEmploye.StatutPret.BROUILLON);
        
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
    public PretEmployeDTO update(Long id, PretEmployeUpdateDTO dto, String username) {
        PretEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PretEmploye not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Can only update if status is BROUILLON
        if (entity.getStatut() != PretEmploye.StatutPret.BROUILLON) {
            throw new RuntimeException("Cannot update pret with status: " + entity.getStatut() + ". Only BROUILLON prets can be updated.");
        }
        
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        entity.setEmploye(employe);
        
        Devise devise = deviseRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
        entity.setDevise(devise);
        
        entity.setDatePret(dto.getDatePret());
        entity.setMontantPret(dto.getMontantPret());
        entity.setMontantSubvention(dto.getMontantSubvention());
        entity.setPeriodicite(PretEmploye.PeriodicitePret.valueOf(dto.getPeriodicite()));
        entity.setPreleverDansPayroll(resolvePreleverDansPayroll(entity.getPeriodicite()));
        entity.setPrelevementPartiel(dto.getPrelevementPartiel());
        entity.setNbPrevu(dto.getNbPrevu());
        entity.setMontantPeriode(resolveMontantPeriode(entity.getMontantPret(), entity.getNbPrevu()));
        Integer oldFrequence = entity.getFrequenceNbPeriodicites() != null ? entity.getFrequenceNbPeriodicites() : 1;
        Integer newFrequence = dto.getFrequenceNbPeriodicites() != null ? dto.getFrequenceNbPeriodicites() : oldFrequence;
        entity.setFrequenceNbPeriodicites(newFrequence);
        if (!newFrequence.equals(oldFrequence)) {
            entity.setFrequenceCompteur(0);
        }
        entity.setPremierPrelevement(dto.getPremierPrelevement());
        
        if (dto.getTypeInteret() != null) {
            entity.setTypeInteret(PretEmploye.TypeInteret.valueOf(dto.getTypeInteret()));
        } else {
            entity.setTypeInteret(null);
        }
        entity.setTauxInteret(dto.getTauxInteret());
        entity.setAvance(dto.getAvance());
        entity.setLibelle(dto.getLibelle());
        entity.setNote(dto.getNote());
        entity.setOrdre(dto.getOrdre());
        
        if (dto.getRegimePaieId() != null) {
            RegimePaie regimePaie = regimePaieRepository.findById(dto.getRegimePaieId())
                    .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + dto.getRegimePaieId()));
            entity.setRegimePaie(regimePaie);
        } else {
            entity.setRegimePaie(null);
        }
        
        if (dto.getTypeRevenuId() != null) {
            TypeRevenu typeRevenu = typeRevenuRepository.findById(dto.getTypeRevenuId())
                    .orElseThrow(() -> new RuntimeException("TypeRevenu not found with id: " + dto.getTypeRevenuId()));
            entity.setTypeRevenu(typeRevenu);
        } else {
            entity.setTypeRevenu(null);
        }
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        } else {
            entity.setEntreprise(null);
        }
        
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        PretEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PretEmploye not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Can only delete if status is BROUILLON
        if (entity.getStatut() != PretEmploye.StatutPret.BROUILLON) {
            throw new RuntimeException("Cannot delete pret with status: " + entity.getStatut() + ". Only BROUILLON prets can be deleted.");
        }
        
        repository.delete(entity);
    }
    
    @Transactional
    public PretEmployeDTO activer(Long id, String username) {
        PretEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PretEmploye not found with id: " + id));
        
        if (entity.getStatut() != PretEmploye.StatutPret.BROUILLON) {
            throw new RuntimeException("Cannot activate pret with status: " + entity.getStatut() + ". Only BROUILLON prets can be activated.");
        }
        
        entity.setStatut(PretEmploye.StatutPret.EN_COURS);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public PretEmployeDTO suspendre(Long id, String username) {
        PretEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PretEmploye not found with id: " + id));
        
        if (entity.getStatut() != PretEmploye.StatutPret.EN_COURS) {
            throw new RuntimeException("Cannot suspend pret with status: " + entity.getStatut() + ". Only EN_COURS prets can be suspended.");
        }
        
        entity.setStatut(PretEmploye.StatutPret.SUSPENDU);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public PretEmployeDTO reprendre(Long id, String username) {
        PretEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PretEmploye not found with id: " + id));
        
        if (entity.getStatut() != PretEmploye.StatutPret.SUSPENDU) {
            throw new RuntimeException("Cannot resume pret with status: " + entity.getStatut() + ". Only SUSPENDU prets can be resumed.");
        }
        
        entity.setStatut(PretEmploye.StatutPret.EN_COURS);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public PretEmployeDTO annuler(Long id, String username) {
        PretEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PretEmploye not found with id: " + id));
        
        if (entity.getStatut() != PretEmploye.StatutPret.EN_COURS && entity.getStatut() != PretEmploye.StatutPret.SUSPENDU) {
            throw new RuntimeException("Cannot cancel pret with status: " + entity.getStatut() + ". Only EN_COURS or SUSPENDU prets can be cancelled.");
        }
        
        entity.setStatut(PretEmploye.StatutPret.ANNULE);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void calculerMontantVerse(Long pretEmployeId) {
        PretEmploye entity = repository.findById(pretEmployeId)
                .orElseThrow(() -> new RuntimeException("PretEmploye not found with id: " + pretEmployeId));
        
        BigDecimal montantVerse = remboursementRepository.sumMontantRembourseByPretEmployeId(pretEmployeId);
        if (montantVerse == null) {
            montantVerse = BigDecimal.ZERO;
        }
        
        entity.setMontantVerse(montantVerse);
        
        // Check if pret is complete
        if (montantVerse.compareTo(entity.getMontantPret()) >= 0) {
            entity.setStatut(PretEmploye.StatutPret.TERMINE);
        }
        
        // Update dernier_prelevement
        LocalDate dernierPrelevement = remboursementRepository.findLastRemboursementDate(pretEmployeId);
        entity.setDernierPrelevement(dernierPrelevement);
        
        repository.save(entity);
    }

    private String resolvePreleverDansPayroll(PretEmploye.PeriodicitePret periodicite) {
        return periodicite == PretEmploye.PeriodicitePret.PAIE ? "Y" : "N";
    }

    private BigDecimal resolveMontantPeriode(BigDecimal montantPret, Integer nbPrevu) {
        if (montantPret == null || nbPrevu == null || nbPrevu <= 0) {
            return BigDecimal.ZERO;
        }
        return montantPret.divide(BigDecimal.valueOf(nbPrevu), 2, RoundingMode.HALF_UP);
    }
    
    private PretEmployeDTO toDTO(PretEmploye entity) {
        PretEmployeDTO dto = new PretEmployeDTO();
        dto.setId(entity.getId());
        
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        
        if (entity.getEmploye() != null) {
            dto.setEmployeId(entity.getEmploye().getId());
            dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
            dto.setEmployeNom(entity.getEmploye().getNom());
            dto.setEmployePrenom(entity.getEmploye().getPrenom());
        }
        
        dto.setDatePret(entity.getDatePret());
        
        if (entity.getDevise() != null) {
            dto.setDeviseId(entity.getDevise().getId());
            dto.setDeviseCode(entity.getDevise().getCodeDevise());
            dto.setDeviseDescription(entity.getDevise().getDescription());
        }
        
        dto.setMontantPret(entity.getMontantPret());
        dto.setMontantSubvention(entity.getMontantSubvention());
        dto.setPeriodicite(entity.getPeriodicite() != null ? entity.getPeriodicite().name() : null);
        dto.setPreleverDansPayroll(entity.getPreleverDansPayroll());
        dto.setPrelevementPartiel(entity.getPrelevementPartiel());
        dto.setNbPrevu(entity.getNbPrevu());
        dto.setMontantPeriode(entity.getMontantPeriode());
        dto.setMontantVerse(entity.getMontantVerse());
        dto.setFrequenceNbPeriodicites(entity.getFrequenceNbPeriodicites());
        dto.setFrequenceCompteur(entity.getFrequenceCompteur());
        dto.setPremierPrelevement(entity.getPremierPrelevement());
        dto.setDernierPrelevement(entity.getDernierPrelevement());
        dto.setTypeInteret(entity.getTypeInteret() != null ? entity.getTypeInteret().name() : null);
        dto.setTauxInteret(entity.getTauxInteret());
        dto.setAvance(entity.getAvance());
        dto.setLibelle(entity.getLibelle());
        dto.setNote(entity.getNote());
        dto.setOrdre(entity.getOrdre());
        
        if (entity.getRegimePaie() != null) {
            dto.setRegimePaieId(entity.getRegimePaie().getId());
            dto.setRegimePaieCode(entity.getRegimePaie().getCodeRegimePaie());
            dto.setRegimePaieDescription(entity.getRegimePaie().getDescription());
        }
        
        if (entity.getTypeRevenu() != null) {
            dto.setTypeRevenuId(entity.getTypeRevenu().getId());
            dto.setTypeRevenuCode(entity.getTypeRevenu().getCodeRevenu());
            dto.setTypeRevenuDescription(entity.getTypeRevenu().getDescription());
        }
        
        dto.setStatut(entity.getStatut() != null ? entity.getStatut().name() : null);
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        // Calculate montant restant
        BigDecimal montantRestant = entity.getMontantPret().subtract(entity.getMontantVerse());
        dto.setMontantRestant(montantRestant);
        
        return dto;
    }
}
