package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PosteCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PosteDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PosteUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Devise;
import com.rsoft.hurmanagement.hurmasterdata.entity.Poste;
import com.rsoft.hurmanagement.hurmasterdata.repository.DeviseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PosteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PosteService {
    
    private final PosteRepository repository;
    private final DeviseRepository deviseRepository;
    
    @Transactional(readOnly = true)
    public Page<PosteDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public PosteDTO findById(Long id) {
        Poste entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Poste not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public PosteDTO create(PosteCreateDTO dto, String username) {
        if (repository.existsByCodePoste(dto.getCodePoste())) {
            throw new RuntimeException("Poste with code " + dto.getCodePoste() + " already exists");
        }
        
        // Validate salaire_min <= salaire_max
        if (dto.getSalaireMin().compareTo(dto.getSalaireMax()) > 0) {
            throw new RuntimeException("Salaire min must be less than or equal to salaire max");
        }
        
        // Validate that devise exists
        Devise devise = deviseRepository.findByCodeDevise(dto.getCodeDevise())
                .orElseThrow(() -> new RuntimeException("Devise with code " + dto.getCodeDevise() + " not found"));
        
        Poste entity = new Poste();
        entity.setCodePoste(dto.getCodePoste());
        entity.setTypeSalaire(dto.getTypeSalaire());
        entity.setDescription(dto.getDescription());
        entity.setDevise(devise);
        entity.setSalaireMin(dto.getSalaireMin());
        entity.setSalaireMax(dto.getSalaireMax());
        entity.setNbJourSemaine(dto.getNbJourSemaine());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public PosteDTO update(Long id, PosteUpdateDTO dto, String username) {
        Poste entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Poste not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Validate salaire_min <= salaire_max
        if (dto.getSalaireMin().compareTo(dto.getSalaireMax()) > 0) {
            throw new RuntimeException("Salaire min must be less than or equal to salaire max");
        }
        
        // Validate that devise exists
        Devise devise = deviseRepository.findByCodeDevise(dto.getCodeDevise())
                .orElseThrow(() -> new RuntimeException("Devise with code " + dto.getCodeDevise() + " not found"));
        
        entity.setTypeSalaire(dto.getTypeSalaire());
        entity.setDescription(dto.getDescription());
        entity.setDevise(devise);
        entity.setSalaireMin(dto.getSalaireMin());
        entity.setSalaireMax(dto.getSalaireMax());
        entity.setNbJourSemaine(dto.getNbJourSemaine());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        Poste entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Poste not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private PosteDTO toDTO(Poste entity) {
        PosteDTO dto = new PosteDTO();
        dto.setId(entity.getId());
        dto.setCodePoste(entity.getCodePoste());
        dto.setTypeSalaire(entity.getTypeSalaire());
        dto.setDescription(entity.getDescription());
        if (entity.getDevise() != null) {
            dto.setCodeDevise(entity.getDevise().getCodeDevise());
            dto.setDeviseDescription(entity.getDevise().getDescription());
        }
        dto.setSalaireMin(entity.getSalaireMin());
        dto.setSalaireMax(entity.getSalaireMax());
        dto.setNbJourSemaine(entity.getNbJourSemaine());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
