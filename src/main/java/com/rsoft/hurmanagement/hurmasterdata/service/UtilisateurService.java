package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {
    
    private final UtilisateurRepository repository;
    private final EntrepriseRepository entrepriseRepository;
    private PasswordEncoder passwordEncoder;
    
    // Initialize password encoder if not provided
    private PasswordEncoder getPasswordEncoder() {
        if (passwordEncoder == null) {
            // Use BCrypt with default strength
            passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        }
        return passwordEncoder;
    }
    
    @Transactional(readOnly = true)
    public Page<UtilisateurDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public UtilisateurDTO findById(Long id) {
        Utilisateur entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<UtilisateurDTO> findAllForDropdown() {
        return repository.findAllByOrderByIdentifiantAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UtilisateurDTO create(UtilisateurCreateDTO dto, String username) {
        // Check if identifiant already exists
        if (repository.existsByIdentifiant(dto.getIdentifiant())) {
            throw new RuntimeException("Identifiant already exists: " + dto.getIdentifiant());
        }
        
        // Check if email already exists
        if (repository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists: " + dto.getEmail());
        }
        
        Utilisateur entity = new Utilisateur();
        entity.setIdentifiant(dto.getIdentifiant());
        entity.setEmail(dto.getEmail());
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setLangue(dto.getLangue());
        entity.setActif("N"); // Default inactive
        entity.setPasswordHash(getPasswordEncoder().encode(dto.getPassword()));
        
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
    public UtilisateurDTO update(Long id, UtilisateurUpdateDTO dto, String username) {
        Utilisateur entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Check if identifiant changed and already exists
        if (!entity.getIdentifiant().equals(dto.getIdentifiant()) && 
            repository.existsByIdentifiant(dto.getIdentifiant())) {
            throw new RuntimeException("Identifiant already exists: " + dto.getIdentifiant());
        }
        
        // Check if email changed and already exists
        if (!entity.getEmail().equals(dto.getEmail()) && 
            repository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists: " + dto.getEmail());
        }
        
        entity.setIdentifiant(dto.getIdentifiant());
        entity.setEmail(dto.getEmail());
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setLangue(dto.getLangue());
        
        // Update password only if provided
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            entity.setPasswordHash(getPasswordEncoder().encode(dto.getPassword()));
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
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        Utilisateur entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    @Transactional
    public UtilisateurDTO toggleActif(Long id, Integer rowscn, String username) {
        Utilisateur entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setActif("Y".equals(entity.getActif()) ? "N" : "Y");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    private UtilisateurDTO toDTO(Utilisateur entity) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(entity.getId());
        dto.setIdentifiant(entity.getIdentifiant());
        dto.setEmail(entity.getEmail());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setLangue(entity.getLangue());
        dto.setActif(entity.getActif());
        dto.setDateExpPassword(entity.getDateExpPassword());
        
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
}
