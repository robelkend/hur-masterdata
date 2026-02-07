package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.TypeEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeEmployeService {

    private static final Logger logger = LoggerFactory.getLogger(TypeEmployeService.class);
    
    private final TypeEmployeRepository repository;
    private final DeviseRepository deviseRepository;
    private final FamilleMetierRepository familleMetierRepository;
    private final NiveauEmployeRepository niveauEmployeRepository;
    private final BaremeSanctionRepository baremeSanctionRepository;
    private final ExclusionDeductionRepository exclusionDeductionRepository;
    private final EntityManager entityManager;
    
    @Transactional(readOnly = true)
    public Page<TypeEmployeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public TypeEmployeDTO findById(Long id) {
        TypeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<TypeEmployeDTO> findAllForDropdown() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "description"))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public TypeEmployeDTO create(TypeEmployeCreateDTO dto, String username) {
        TypeEmploye entity = new TypeEmploye();
        entity.setDescription(dto.getDescription());
        entity.setPauseDebut(dto.getPauseDebut());
        entity.setPauseFin(dto.getPauseFin());
        entity.setPayerAbsence(dto.getPayerAbsence() != null ? dto.getPayerAbsence() : "Y");
        entity.setPayerAbsenceMotivee(dto.getPayerAbsenceMotivee() != null ? dto.getPayerAbsenceMotivee() : "Y");
        entity.setSalaireMinimum(dto.getSalaireMinimum() != null ? dto.getSalaireMinimum() : BigDecimal.ZERO);
        entity.setSalaireMaximum(dto.getSalaireMaximum() != null ? dto.getSalaireMaximum() : BigDecimal.ZERO);
        entity.setAjouterBonusApresNbMinutePresence(dto.getAjouterBonusApresNbMinutePresence());
        entity.setPourcentageJourBonus(dto.getPourcentageJourBonus());
        entity.setGenererPrestation(dto.getGenererPrestation() != null ? dto.getGenererPrestation() : "Y");
        entity.setBaseCalculBoni(dto.getBaseCalculBoni());
        entity.setSupplementaire(dto.getSupplementaire() != null ? dto.getSupplementaire() : "Y");
        entity.setBaseCalculSupplementaire(dto.getBaseCalculSupplementaire());
        entity.setCalculerSupplementaireApres(dto.getCalculerSupplementaireApres());
        entity.setProbation(dto.getProbation() != null ? dto.getProbation() : "Y");
        entity.setStatutManagement(dto.getStatutManagement() != null ? dto.getStatutManagement() : TypeEmploye.StatutManagement.NON_MANAGER);
        
        if (dto.getDeviseId() != null) {
            Devise devise = deviseRepository.findById(dto.getDeviseId())
                    .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
            entity.setDevise(devise);
        }
        
        if (dto.getFamilleMetierId() != null) {
            FamilleMetier familleMetier = familleMetierRepository.findById(dto.getFamilleMetierId())
                    .orElseThrow(() -> new RuntimeException("FamilleMetier not found with id: " + dto.getFamilleMetierId()));
            entity.setFamilleMetier(familleMetier);
        }
        
        if (dto.getNiveauEmployeId() != null) {
            NiveauEmploye niveauEmploye = niveauEmployeRepository.findById(dto.getNiveauEmployeId())
                    .orElseThrow(() -> new RuntimeException("NiveauEmploye not found with id: " + dto.getNiveauEmployeId()));
            entity.setNiveauEmploye(niveauEmploye);
        }
        
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public TypeEmployeDTO update(Long id, TypeEmployeUpdateDTO dto, String username) {
        TypeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setDescription(dto.getDescription());
        entity.setPauseDebut(dto.getPauseDebut());
        entity.setPauseFin(dto.getPauseFin());
        entity.setPayerAbsence(dto.getPayerAbsence());
        entity.setPayerAbsenceMotivee(dto.getPayerAbsenceMotivee());
        entity.setSalaireMinimum(dto.getSalaireMinimum());
        entity.setSalaireMaximum(dto.getSalaireMaximum());
        entity.setAjouterBonusApresNbMinutePresence(dto.getAjouterBonusApresNbMinutePresence());
        entity.setPourcentageJourBonus(dto.getPourcentageJourBonus());
        entity.setGenererPrestation(dto.getGenererPrestation());
        entity.setBaseCalculBoni(dto.getBaseCalculBoni());
        entity.setSupplementaire(dto.getSupplementaire());
        entity.setBaseCalculSupplementaire(dto.getBaseCalculSupplementaire());
        entity.setCalculerSupplementaireApres(dto.getCalculerSupplementaireApres());
        entity.setProbation(dto.getProbation());
        entity.setStatutManagement(dto.getStatutManagement() != null ? dto.getStatutManagement() : TypeEmploye.StatutManagement.NON_MANAGER);
        
        if (dto.getDeviseId() != null) {
            Devise devise = deviseRepository.findById(dto.getDeviseId())
                    .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
            entity.setDevise(devise);
        } else {
            entity.setDevise(null);
        }
        
        if (dto.getFamilleMetierId() != null) {
            FamilleMetier familleMetier = familleMetierRepository.findById(dto.getFamilleMetierId())
                    .orElseThrow(() -> new RuntimeException("FamilleMetier not found with id: " + dto.getFamilleMetierId()));
            entity.setFamilleMetier(familleMetier);
        } else {
            entity.setFamilleMetier(null);
        }
        
        if (dto.getNiveauEmployeId() != null) {
            NiveauEmploye niveauEmploye = niveauEmployeRepository.findById(dto.getNiveauEmployeId())
                    .orElseThrow(() -> new RuntimeException("NiveauEmploye not found with id: " + dto.getNiveauEmployeId()));
            entity.setNiveauEmploye(niveauEmploye);
        } else {
            entity.setNiveauEmploye(null);
        }
        
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        TypeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }

    @Transactional
    public TypeEmployeDTO cloneTypeEmploye(Long id, String newDescription, String username) {
        TypeEmploye source = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + id));

        TypeEmploye clone = new TypeEmploye();
        clone.setDescription(newDescription);
        clone.setPauseDebut(source.getPauseDebut());
        clone.setPauseFin(source.getPauseFin());
        clone.setPayerAbsence(source.getPayerAbsence());
        clone.setPayerAbsenceMotivee(source.getPayerAbsenceMotivee());
        clone.setDevise(source.getDevise());
        clone.setSalaireMinimum(source.getSalaireMinimum());
        clone.setSalaireMaximum(source.getSalaireMaximum());
        clone.setAjouterBonusApresNbMinutePresence(source.getAjouterBonusApresNbMinutePresence());
        clone.setPourcentageJourBonus(source.getPourcentageJourBonus());
        clone.setGenererPrestation(source.getGenererPrestation());
        clone.setBaseCalculBoni(source.getBaseCalculBoni());
        clone.setSupplementaire(source.getSupplementaire());
        clone.setBaseCalculSupplementaire(source.getBaseCalculSupplementaire());
        clone.setCalculerSupplementaireApres(source.getCalculerSupplementaireApres());
        clone.setProbation(source.getProbation());
        clone.setStatutManagement(source.getStatutManagement());
        clone.setFamilleMetier(source.getFamilleMetier());
        clone.setNiveauEmploye(source.getNiveauEmploye());
        clone.setCreatedBy(username);
        clone.setCreatedOn(OffsetDateTime.now());
        clone.setRowscn(1);

        TypeEmploye saved = repository.save(clone);

        List<BaremeSanction> sanctions = Collections.emptyList();
        if (tableExists("public.bareme_sanction")) {
            try {
                sanctions = baremeSanctionRepository.findByTypeEmployeId(source.getId());
            } catch (DataAccessException ex) {
                logger.warn("Skipping bareme_sanction clone due to data access error: {}", ex.getMessage());
            }
        } else {
            logger.warn("Skipping bareme_sanction clone because table does not exist");
        }
        for (BaremeSanction sourceSanction : sanctions) {
            BaremeSanction copy = new BaremeSanction();
            copy.setTypeEmploye(saved);
            copy.setInfractionType(sourceSanction.getInfractionType());
            copy.setUniteInfraction(sourceSanction.getUniteInfraction());
            copy.setSeuilMin(sourceSanction.getSeuilMin());
            copy.setSeuilMax(sourceSanction.getSeuilMax());
            copy.setPenaliteMinutes(sourceSanction.getPenaliteMinutes());
            copy.setUnitePenalite(sourceSanction.getUnitePenalite());
            copy.setCreatedBy(username);
            copy.setCreatedOn(OffsetDateTime.now());
            copy.setRowscn(1);
            baremeSanctionRepository.save(copy);
        }

        List<ExclusionDeduction> exclusions = Collections.emptyList();
        if (tableExists("public.exclusion_deduction")) {
            try {
                exclusions = exclusionDeductionRepository.findByTypeEmployeId(source.getId());
            } catch (DataAccessException ex) {
                logger.warn("Skipping exclusion_deduction clone due to data access error: {}", ex.getMessage());
            }
        } else {
            logger.warn("Skipping exclusion_deduction clone because table does not exist");
        }
        for (ExclusionDeduction sourceExclusion : exclusions) {
            ExclusionDeduction copy = new ExclusionDeduction();
            copy.setTypeEmploye(saved);
            copy.setDefinitionDeduction(sourceExclusion.getDefinitionDeduction());
            copy.setActif(sourceExclusion.getActif());
            copy.setCreatedBy(username);
            copy.setCreatedOn(OffsetDateTime.now());
            copy.setRowscn(1);
            exclusionDeductionRepository.save(copy);
        }

        return toDTO(saved);
    }

    private boolean tableExists(String tableName) {
        Object result = entityManager.createNativeQuery("SELECT to_regclass(?1)")
                .setParameter(1, tableName)
                .getSingleResult();
        return result != null;
    }
    
    private TypeEmployeDTO toDTO(TypeEmploye entity) {
        TypeEmployeDTO dto = new TypeEmployeDTO();
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        dto.setPauseDebut(entity.getPauseDebut());
        dto.setPauseFin(entity.getPauseFin());
        dto.setPayerAbsence(entity.getPayerAbsence());
        dto.setPayerAbsenceMotivee(entity.getPayerAbsenceMotivee());
        
        if (entity.getDevise() != null) {
            dto.setDeviseId(entity.getDevise().getId());
            dto.setDeviseCode(entity.getDevise().getCodeDevise());
            dto.setDeviseDescription(entity.getDevise().getDescription());
        }
        
        dto.setSalaireMinimum(entity.getSalaireMinimum());
        dto.setSalaireMaximum(entity.getSalaireMaximum());
        dto.setAjouterBonusApresNbMinutePresence(entity.getAjouterBonusApresNbMinutePresence());
        dto.setPourcentageJourBonus(entity.getPourcentageJourBonus());
        dto.setGenererPrestation(entity.getGenererPrestation());
        dto.setBaseCalculBoni(entity.getBaseCalculBoni());
        dto.setSupplementaire(entity.getSupplementaire());
        dto.setBaseCalculSupplementaire(entity.getBaseCalculSupplementaire());
        dto.setCalculerSupplementaireApres(entity.getCalculerSupplementaireApres());
        dto.setProbation(entity.getProbation());
        dto.setStatutManagement(entity.getStatutManagement());
        
        if (entity.getFamilleMetier() != null) {
            dto.setFamilleMetierId(entity.getFamilleMetier().getId());
            dto.setFamilleMetierCode(entity.getFamilleMetier().getCodeFamilleMetier());
            dto.setFamilleMetierLibelle(entity.getFamilleMetier().getLibelle());
        }
        
        if (entity.getNiveauEmploye() != null) {
            dto.setNiveauEmployeId(entity.getNiveauEmploye().getId());
            dto.setNiveauEmployeCode(entity.getNiveauEmploye().getCodeNiveau());
            dto.setNiveauEmployeDescription(entity.getNiveauEmploye().getDescription());
            dto.setNiveauEmployeNiveauHierarchique(entity.getNiveauEmploye().getNiveauHierarchique());
        }
        
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
