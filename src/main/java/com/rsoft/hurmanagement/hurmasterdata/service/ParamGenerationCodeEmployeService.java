package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.ParamGenerationCodeEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ParamGenerationCodeEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ParamGenerationCodeEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.ParamGenerationCodeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.ParamGenerationCodeEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ParamGenerationCodeEmployeService {
    
    private final ParamGenerationCodeEmployeRepository repository;
    private final EntrepriseRepository entrepriseRepository;
    private final TypeEmployeRepository typeEmployeRepository;
    
    @Transactional(readOnly = true)
    public Page<ParamGenerationCodeEmployeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public ParamGenerationCodeEmployeDTO findById(Long id) {
        ParamGenerationCodeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ParamGenerationCodeEmploye not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public ParamGenerationCodeEmployeDTO create(ParamGenerationCodeEmployeCreateDTO dto, String username) {
        ParamGenerationCodeEmploye entity = new ParamGenerationCodeEmploye();
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        }
        
        if (dto.getTypeEmployeId() != null) {
            TypeEmploye typeEmploye = typeEmployeRepository.findById(dto.getTypeEmployeId())
                    .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + dto.getTypeEmployeId()));
            entity.setTypeEmploye(typeEmploye);
        }
        
        entity.setDateEffectif(dto.getDateEffectif());
        entity.setDateFin(dto.getDateFin());
        entity.setModeGeneration(ParamGenerationCodeEmploye.ModeGeneration.valueOf(dto.getModeGeneration()));
        entity.setValeurDepart(dto.getValeurDepart());
        entity.setValeurCourante(dto.getValeurCourante());
        entity.setPasIncrementation(dto.getPasIncrementation() != null ? dto.getPasIncrementation() : 1);
        entity.setLongueurMin(dto.getLongueurMin());
        entity.setPaddingChar(dto.getPaddingChar() != null ? dto.getPaddingChar() : "0");
        entity.setPrefixeFixe(dto.getPrefixeFixe());
        entity.setSuffixeFixe(dto.getSuffixeFixe());
        entity.setPattern(dto.getPattern());
        entity.setMajuscules(dto.getMajuscules() != null ? dto.getMajuscules() : "Y");
        entity.setEnleverAccents(dto.getEnleverAccents() != null ? dto.getEnleverAccents() : "Y");
        entity.setOptions(dto.getOptions() != null ? dto.getOptions() : "{}");
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public ParamGenerationCodeEmployeDTO update(ParamGenerationCodeEmployeUpdateDTO dto, String username) {
        ParamGenerationCodeEmploye entity = repository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("ParamGenerationCodeEmploye not found with id: " + dto.getId()));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        } else {
            entity.setEntreprise(null);
        }
        
        if (dto.getTypeEmployeId() != null) {
            TypeEmploye typeEmploye = typeEmployeRepository.findById(dto.getTypeEmployeId())
                    .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + dto.getTypeEmployeId()));
            entity.setTypeEmploye(typeEmploye);
        } else {
            entity.setTypeEmploye(null);
        }
        
        entity.setDateEffectif(dto.getDateEffectif());
        entity.setDateFin(dto.getDateFin());
        entity.setModeGeneration(ParamGenerationCodeEmploye.ModeGeneration.valueOf(dto.getModeGeneration()));
        entity.setValeurDepart(dto.getValeurDepart());
        entity.setValeurCourante(dto.getValeurCourante());
        entity.setPasIncrementation(dto.getPasIncrementation() != null ? dto.getPasIncrementation() : 1);
        entity.setLongueurMin(dto.getLongueurMin());
        entity.setPaddingChar(dto.getPaddingChar() != null ? dto.getPaddingChar() : "0");
        entity.setPrefixeFixe(dto.getPrefixeFixe());
        entity.setSuffixeFixe(dto.getSuffixeFixe());
        entity.setPattern(dto.getPattern());
        entity.setMajuscules(dto.getMajuscules() != null ? dto.getMajuscules() : "Y");
        entity.setEnleverAccents(dto.getEnleverAccents() != null ? dto.getEnleverAccents() : "Y");
        entity.setOptions(dto.getOptions() != null ? dto.getOptions() : "{}");
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        ParamGenerationCodeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ParamGenerationCodeEmploye not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private ParamGenerationCodeEmployeDTO toDTO(ParamGenerationCodeEmploye entity) {
        ParamGenerationCodeEmployeDTO dto = new ParamGenerationCodeEmployeDTO();
        dto.setId(entity.getId());
        dto.setEntrepriseId(entity.getEntreprise() != null ? entity.getEntreprise().getId() : null);
        dto.setEntrepriseCode(entity.getEntreprise() != null ? entity.getEntreprise().getCodeEntreprise() : null);
        dto.setEntrepriseNom(entity.getEntreprise() != null ? entity.getEntreprise().getNomEntreprise() : null);
        dto.setTypeEmployeId(entity.getTypeEmploye() != null ? entity.getTypeEmploye().getId() : null);
        dto.setTypeEmployeDescription(entity.getTypeEmploye() != null ? entity.getTypeEmploye().getDescription() : null);
        dto.setDateEffectif(entity.getDateEffectif());
        dto.setDateFin(entity.getDateFin());
        dto.setModeGeneration(entity.getModeGeneration().name());
        dto.setValeurDepart(entity.getValeurDepart());
        dto.setValeurCourante(entity.getValeurCourante());
        dto.setPasIncrementation(entity.getPasIncrementation());
        dto.setLongueurMin(entity.getLongueurMin());
        dto.setPaddingChar(entity.getPaddingChar());
        dto.setPrefixeFixe(entity.getPrefixeFixe());
        dto.setSuffixeFixe(entity.getSuffixeFixe());
        dto.setPattern(entity.getPattern());
        dto.setMajuscules(entity.getMajuscules());
        dto.setEnleverAccents(entity.getEnleverAccents());
        dto.setOptions(entity.getOptions());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
