package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.UniteOrganisationnelleCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.UniteOrganisationnelleDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.UniteOrganisationnelleUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeUniteOrganisationnelle;
import com.rsoft.hurmanagement.hurmasterdata.entity.UniteOrganisationnelle;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeUniteOrganisationnelleRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.UniteOrganisationnelleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UniteOrganisationnelleService {
    
    private final UniteOrganisationnelleRepository repository;
    private final TypeUniteOrganisationnelleRepository typeRepository;
    private final EmployeRepository employeRepository;
    
    @Transactional(readOnly = true)
    public Page<UniteOrganisationnelleDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public UniteOrganisationnelleDTO findById(Long id) {
        UniteOrganisationnelle entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("UniteOrganisationnelle not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<UniteOrganisationnelleDTO> findAllForDropdown(Long excludeId) {
        List<UniteOrganisationnelle> entities = repository.findAllExcluding(excludeId);
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UniteOrganisationnelleDTO create(UniteOrganisationnelleCreateDTO dto, String username) {
        if (repository.existsByCode(dto.getCode())) {
            throw new RuntimeException("UniteOrganisationnelle with code " + dto.getCode() + " already exists");
        }
        
        UniteOrganisationnelle entity = new UniteOrganisationnelle();
        entity.setCode(dto.getCode());
        entity.setNom(dto.getNom());
        
        // Type Unite Organisationnelle
        TypeUniteOrganisationnelle type = typeRepository.findById(dto.getTypeUniteOrganisationnelleId())
                .orElseThrow(() -> new RuntimeException("TypeUniteOrganisationnelle not found with id: " + dto.getTypeUniteOrganisationnelleId()));
        entity.setTypeUniteOrganisationnelle(type);
        
        // Unite Parente
        if (dto.getUniteParenteId() != null) {
            UniteOrganisationnelle parente = repository.findById(dto.getUniteParenteId())
                    .orElseThrow(() -> new RuntimeException("UniteOrganisationnelle parente not found with id: " + dto.getUniteParenteId()));
            entity.setUniteParente(parente);
        }
        
        // Responsable Employe
        if (dto.getResponsableEmployeId() != null) {
            Employe responsable = employeRepository.findById(dto.getResponsableEmployeId())
                    .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getResponsableEmployeId()));
            entity.setResponsableEmploye(responsable);
        }
        
        // Contact fields
        entity.setEmail(dto.getEmail());
        entity.setTelephone1(dto.getTelephone1());
        entity.setTelephone2(dto.getTelephone2());
        entity.setExtensionTelephone(dto.getExtensionTelephone());
        
        // Status
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        
        // Dates
        entity.setDateDebutEffet(dto.getDateDebutEffet() != null ? dto.getDateDebutEffet() : LocalDate.now());
        entity.setDateFinEffet(dto.getDateFinEffet());
        
        // Validate date range
        if (entity.getDateDebutEffet() != null && entity.getDateFinEffet() != null) {
            if (entity.getDateFinEffet().isBefore(entity.getDateDebutEffet())) {
                throw new RuntimeException("Date fin effet must be greater than or equal to date debut effet");
            }
        }
        
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public UniteOrganisationnelleDTO update(Long id, UniteOrganisationnelleUpdateDTO dto, String username) {
        UniteOrganisationnelle entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("UniteOrganisationnelle not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setNom(dto.getNom());
        
        // Type Unite Organisationnelle
        TypeUniteOrganisationnelle type = typeRepository.findById(dto.getTypeUniteOrganisationnelleId())
                .orElseThrow(() -> new RuntimeException("TypeUniteOrganisationnelle not found with id: " + dto.getTypeUniteOrganisationnelleId()));
        entity.setTypeUniteOrganisationnelle(type);
        
        // Unite Parente - check for circular reference
        if (dto.getUniteParenteId() != null) {
            if (dto.getUniteParenteId().equals(id)) {
                throw new RuntimeException("A unit cannot be its own parent");
            }
            // Check for circular reference by traversing up the parent chain
            if (wouldCreateCircularReference(dto.getUniteParenteId(), id)) {
                throw new RuntimeException("Circular reference detected. Cannot set this parent.");
            }
            UniteOrganisationnelle parente = repository.findById(dto.getUniteParenteId())
                    .orElseThrow(() -> new RuntimeException("UniteOrganisationnelle parente not found with id: " + dto.getUniteParenteId()));
            entity.setUniteParente(parente);
        } else {
            entity.setUniteParente(null);
        }
        
        // Responsable Employe
        if (dto.getResponsableEmployeId() != null) {
            Employe responsable = employeRepository.findById(dto.getResponsableEmployeId())
                    .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getResponsableEmployeId()));
            entity.setResponsableEmploye(responsable);
        } else {
            entity.setResponsableEmploye(null);
        }
        
        // Contact fields
        entity.setEmail(dto.getEmail());
        entity.setTelephone1(dto.getTelephone1());
        entity.setTelephone2(dto.getTelephone2());
        entity.setExtensionTelephone(dto.getExtensionTelephone());
        
        // Status
        entity.setActif(dto.getActif());
        
        // Dates
        entity.setDateDebutEffet(dto.getDateDebutEffet());
        entity.setDateFinEffet(dto.getDateFinEffet());
        
        // Validate date range
        if (entity.getDateDebutEffet() != null && entity.getDateFinEffet() != null) {
            if (entity.getDateFinEffet().isBefore(entity.getDateDebutEffet())) {
                throw new RuntimeException("Date fin effet must be greater than or equal to date debut effet");
            }
        }
        
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        UniteOrganisationnelle entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("UniteOrganisationnelle not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Check if this unit has children
        List<UniteOrganisationnelle> children = repository.findAll().stream()
                .filter(u -> u.getUniteParente() != null && u.getUniteParente().getId().equals(id))
                .collect(Collectors.toList());
        if (!children.isEmpty()) {
            throw new RuntimeException("Cannot delete unit with child units. Please reassign or delete children first.");
        }
        
        repository.delete(entity);
    }
    
    private UniteOrganisationnelleDTO toDTO(UniteOrganisationnelle entity) {
        UniteOrganisationnelleDTO dto = new UniteOrganisationnelleDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setNom(entity.getNom());
        
        // Type Unite Organisationnelle
        if (entity.getTypeUniteOrganisationnelle() != null) {
            dto.setTypeUniteOrganisationnelleId(entity.getTypeUniteOrganisationnelle().getId());
            dto.setTypeUniteOrganisationnelleCode(entity.getTypeUniteOrganisationnelle().getCode());
            dto.setTypeUniteOrganisationnelleLibelle(entity.getTypeUniteOrganisationnelle().getLibelle());
        }
        
        // Unite Parente
        if (entity.getUniteParente() != null) {
            dto.setUniteParenteId(entity.getUniteParente().getId());
            dto.setUniteParenteCode(entity.getUniteParente().getCode());
            dto.setUniteParenteNom(entity.getUniteParente().getNom());
        }
        
        // Responsable Employe
        if (entity.getResponsableEmploye() != null) {
            dto.setResponsableEmployeId(entity.getResponsableEmploye().getId());
            dto.setResponsableEmployeCode(entity.getResponsableEmploye().getCodeEmploye());
            dto.setResponsableEmployeNom(entity.getResponsableEmploye().getNom());
            dto.setResponsableEmployePrenom(entity.getResponsableEmploye().getPrenom());
        }
        
        // Contact fields
        dto.setEmail(entity.getEmail());
        dto.setTelephone1(entity.getTelephone1());
        dto.setTelephone2(entity.getTelephone2());
        dto.setExtensionTelephone(entity.getExtensionTelephone());
        
        // Status
        dto.setActif(entity.getActif());
        
        // Dates
        dto.setDateDebutEffet(entity.getDateDebutEffet());
        dto.setDateFinEffet(entity.getDateFinEffet());
        
        // Audit fields
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        return dto;
    }
    
    /**
     * Check if setting parentId as parent of childId would create a circular reference
     * by traversing up the parent chain from parentId
     */
    private boolean wouldCreateCircularReference(Long parentId, Long childId) {
        UniteOrganisationnelle current = repository.findById(parentId).orElse(null);
        int maxDepth = 100; // Prevent infinite loops
        int depth = 0;
        
        while (current != null && depth < maxDepth) {
            if (current.getId().equals(childId)) {
                return true; // Circular reference detected
            }
            current = current.getUniteParente();
            depth++;
        }
        
        return false;
    }
}
