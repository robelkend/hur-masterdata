package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
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
public class EntrepriseService {
    
    private final EntrepriseRepository repository;
    private final DeviseRepository deviseRepository;
    
    @Transactional(readOnly = true)
    public Page<EntrepriseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public EntrepriseDTO findById(Long id) {
        Entreprise entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<EntrepriseDTO> findAllForDropdown() {
        return repository.findAllByOrderByCodeEntrepriseAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public EntrepriseDTO create(EntrepriseCreateDTO dto, String username) {
        // Check if code already exists
        if (repository.existsByCodeEntreprise(dto.getCodeEntreprise())) {
            throw new RuntimeException("Code entreprise already exists: " + dto.getCodeEntreprise());
        }
        
        // If setting as active, deactivate all others
        if ("Y".equals(dto.getActif())) {
            List<Entreprise> activeEntreprises = repository.findByActif("Y");
            for (Entreprise e : activeEntreprises) {
                e.setActif("N");
                repository.save(e);
            }
        }
        
        Entreprise entity = new Entreprise();
        entity.setCodeEntreprise(dto.getCodeEntreprise());
        entity.setNomEntreprise(dto.getNomEntreprise());
        entity.setNomLegal(dto.getNomLegal());
        
        Devise devise = deviseRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
        entity.setDevise(devise);
        
        entity.setMoisDebutAnneeFiscale(dto.getMoisDebutAnneeFiscale());
        entity.setAnneeFiscaleCourante(dto.getAnneeFiscaleCourante());
        entity.setSecteurActivite(dto.getSecteurActivite());
        entity.setEtat(dto.getEtat());
        entity.setVille(dto.getVille());
        entity.setAdresse(dto.getAdresse());
        entity.setCodePostal(dto.getCodePostal());
        entity.setPays(dto.getPays());
        entity.setTelephone1(dto.getTelephone1());
        entity.setTelephone2(dto.getTelephone2());
        entity.setTelephone3(dto.getTelephone3());
        entity.setFax(dto.getFax());
        entity.setCourriel(dto.getCourriel());
        
        entity.setCongeCumule("N"); // Default
        entity.setCongeApresAnnees(dto.getCongeApresAnnees() != null ? dto.getCongeApresAnnees() : 0);
        entity.setNbAnneesCumulAccepte(dto.getNbAnneesCumulAccepte() != null ? dto.getNbAnneesCumulAccepte() : 0);
        entity.setGenererAbsenceDansJours(dto.getGenererAbsenceDansJours() != null ? dto.getGenererAbsenceDansJours() : 1);
        entity.setDateCongeGenere(dto.getDateCongeGenere());
        entity.setAutoActiverConge("N"); // Default
        entity.setAutoFermerConge("N"); // Default
        entity.setMatriculeAssureurDefaut(dto.getMatriculeAssureurDefaut());
        
        if (dto.getEntrepriseMereId() != null) {
            Entreprise entrepriseMere = repository.findById(dto.getEntrepriseMereId())
                    .orElseThrow(() -> new RuntimeException("Entreprise mere not found with id: " + dto.getEntrepriseMereId()));
            entity.setEntrepriseMere(entrepriseMere);
        }
        
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setLogoUrl(dto.getLogoUrl());
        
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public EntrepriseDTO update(Long id, EntrepriseUpdateDTO dto, String username) {
        Entreprise entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // If setting as active, deactivate all others
        if ("Y".equals(dto.getActif()) && !"Y".equals(entity.getActif())) {
            List<Entreprise> activeEntreprises = repository.findByActif("Y");
            for (Entreprise e : activeEntreprises) {
                if (!e.getId().equals(id)) {
                    e.setActif("N");
                    repository.save(e);
                }
            }
        }
        
        if (dto.getNomEntreprise() != null) {
            entity.setNomEntreprise(dto.getNomEntreprise());
        }
        if (dto.getNomLegal() != null) {
            entity.setNomLegal(dto.getNomLegal());
        }
        if (dto.getDeviseId() != null) {
            Devise devise = deviseRepository.findById(dto.getDeviseId())
                    .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
            entity.setDevise(devise);
        }
        if (dto.getMoisDebutAnneeFiscale() != null) {
            entity.setMoisDebutAnneeFiscale(dto.getMoisDebutAnneeFiscale());
        }
        if (dto.getAnneeFiscaleCourante() != null) {
            entity.setAnneeFiscaleCourante(dto.getAnneeFiscaleCourante());
        }
        if (dto.getSecteurActivite() != null) {
            entity.setSecteurActivite(dto.getSecteurActivite());
        }
        if (dto.getEtat() != null) {
            entity.setEtat(dto.getEtat());
        }
        if (dto.getVille() != null) {
            entity.setVille(dto.getVille());
        }
        if (dto.getAdresse() != null) {
            entity.setAdresse(dto.getAdresse());
        }
        if (dto.getCodePostal() != null) {
            entity.setCodePostal(dto.getCodePostal());
        }
        if (dto.getPays() != null) {
            entity.setPays(dto.getPays());
        }
        if (dto.getTelephone1() != null) {
            entity.setTelephone1(dto.getTelephone1());
        }
        if (dto.getTelephone2() != null) {
            entity.setTelephone2(dto.getTelephone2());
        }
        if (dto.getTelephone3() != null) {
            entity.setTelephone3(dto.getTelephone3());
        }
        if (dto.getFax() != null) {
            entity.setFax(dto.getFax());
        }
        if (dto.getCourriel() != null) {
            entity.setCourriel(dto.getCourriel());
        }
        if (dto.getCongeCumule() != null) {
            entity.setCongeCumule(dto.getCongeCumule());
        }
        if (dto.getCongeApresAnnees() != null) {
            entity.setCongeApresAnnees(dto.getCongeApresAnnees());
        }
        if (dto.getNbAnneesCumulAccepte() != null) {
            entity.setNbAnneesCumulAccepte(dto.getNbAnneesCumulAccepte());
        }
        if (dto.getGenererAbsenceDansJours() != null) {
            entity.setGenererAbsenceDansJours(dto.getGenererAbsenceDansJours());
        }
        if (dto.getDateCongeGenere() != null) {
            entity.setDateCongeGenere(dto.getDateCongeGenere());
        }
        if (dto.getAutoActiverConge() != null) {
            entity.setAutoActiverConge(dto.getAutoActiverConge());
        }
        if (dto.getAutoFermerConge() != null) {
            entity.setAutoFermerConge(dto.getAutoFermerConge());
        }
        if (dto.getMatriculeAssureurDefaut() != null) {
            entity.setMatriculeAssureurDefaut(dto.getMatriculeAssureurDefaut());
        }
        if (dto.getEntrepriseMereId() != null) {
            Entreprise entrepriseMere = repository.findById(dto.getEntrepriseMereId())
                    .orElseThrow(() -> new RuntimeException("Entreprise mere not found with id: " + dto.getEntrepriseMereId()));
            entity.setEntrepriseMere(entrepriseMere);
        } else if (dto.getEntrepriseMereId() == null && entity.getEntrepriseMere() != null) {
            entity.setEntrepriseMere(null);
        }
        if (dto.getActif() != null) {
            entity.setActif(dto.getActif());
        }
        if (dto.getLogoUrl() != null) {
            entity.setLogoUrl(dto.getLogoUrl());
        }
        
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        Entreprise entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private EntrepriseDTO toDTO(Entreprise entity) {
        EntrepriseDTO dto = new EntrepriseDTO();
        dto.setId(entity.getId());
        dto.setCodeEntreprise(entity.getCodeEntreprise());
        dto.setNomEntreprise(entity.getNomEntreprise());
        dto.setNomLegal(entity.getNomLegal());
        if (entity.getDevise() != null) {
            dto.setDeviseId(entity.getDevise().getId());
            dto.setDeviseCode(entity.getDevise().getCodeDevise());
            dto.setDeviseDescription(entity.getDevise().getDescription());
        }
        dto.setMoisDebutAnneeFiscale(entity.getMoisDebutAnneeFiscale());
        dto.setAnneeFiscaleCourante(entity.getAnneeFiscaleCourante());
        dto.setSecteurActivite(entity.getSecteurActivite());
        dto.setEtat(entity.getEtat());
        dto.setVille(entity.getVille());
        dto.setAdresse(entity.getAdresse());
        dto.setCodePostal(entity.getCodePostal());
        dto.setPays(entity.getPays());
        dto.setTelephone1(entity.getTelephone1());
        dto.setTelephone2(entity.getTelephone2());
        dto.setTelephone3(entity.getTelephone3());
        dto.setFax(entity.getFax());
        dto.setCourriel(entity.getCourriel());
        dto.setCongeCumule(entity.getCongeCumule());
        dto.setCongeApresAnnees(entity.getCongeApresAnnees());
        dto.setNbAnneesCumulAccepte(entity.getNbAnneesCumulAccepte());
        dto.setGenererAbsenceDansJours(entity.getGenererAbsenceDansJours());
        dto.setDateCongeGenere(entity.getDateCongeGenere());
        dto.setAutoActiverConge(entity.getAutoActiverConge());
        dto.setAutoFermerConge(entity.getAutoFermerConge());
        dto.setMatriculeAssureurDefaut(entity.getMatriculeAssureurDefaut());
        if (entity.getEntrepriseMere() != null) {
            dto.setEntrepriseMereId(entity.getEntrepriseMere().getId());
            dto.setEntrepriseMereCode(entity.getEntrepriseMere().getCodeEntreprise());
            dto.setEntrepriseMereNom(entity.getEntrepriseMere().getNomEntreprise());
        }
        dto.setActif(entity.getActif());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
