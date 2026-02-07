package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtraction;
import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionRequete;
import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionParam;
import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionLiaison;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceExtractionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceExtractionRequeteRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceExtractionParamRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceExtractionLiaisonRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterfaceExtractionService {
    
    private final InterfaceExtractionRepository repository;
    private final InterfaceExtractionRequeteRepository requeteRepository;
    private final InterfaceExtractionParamRepository paramRepository;
    private final InterfaceExtractionLiaisonRepository liaisonRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final InterfaceExtractionProcessService processService;
    
    @Transactional(readOnly = true)
    public Page<InterfaceExtractionDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public InterfaceExtractionDTO findById(Long id) {
        InterfaceExtraction entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("InterfaceExtraction not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<InterfaceExtractionDTO> findAllForDropdown() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    @Transactional
    public InterfaceExtractionDTO create(InterfaceExtractionCreateDTO dto, String username) {
        if (repository.existsByCodeExtraction(dto.getCodeExtraction())) {
            throw new RuntimeException("InterfaceExtraction with code " + dto.getCodeExtraction() + " already exists");
        }
        
        InterfaceExtraction entity = new InterfaceExtraction();
        entity.setCodeExtraction(dto.getCodeExtraction());
        entity.setDescription(dto.getDescription());
        entity.setSeparateur(dto.getSeparateur());
        entity.setEncadreur(dto.getEncadreur());
        entity.setActif(dto.getActif() != null ? dto.getActif() : "N");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        }
        
        InterfaceExtraction savedEntity = repository.save(entity);
        
        // Save requetes if provided
        if (dto.getRequetes() != null && !dto.getRequetes().isEmpty()) {
            saveRequetes(savedEntity, dto.getRequetes(), username, null);
        }
        
        return toDTO(savedEntity);
    }
    
    @Transactional
    public InterfaceExtractionDTO update(InterfaceExtractionUpdateDTO dto, String username) {
        InterfaceExtraction entity = repository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("InterfaceExtraction not found with id: " + dto.getId()));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        if (!entity.getCodeExtraction().equals(dto.getCodeExtraction()) && repository.existsByCodeExtraction(dto.getCodeExtraction())) {
            throw new RuntimeException("InterfaceExtraction with code " + dto.getCodeExtraction() + " already exists");
        }
        
        entity.setCodeExtraction(dto.getCodeExtraction());
        entity.setDescription(dto.getDescription());
        entity.setSeparateur(dto.getSeparateur());
        entity.setEncadreur(dto.getEncadreur());
        entity.setActif(dto.getActif());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        } else {
            entity.setEntreprise(null);
        }
        
        InterfaceExtraction savedEntity = repository.save(entity);
        
        // Update requetes - delete all existing and recreate
        requeteRepository.deleteByInterfaceExtractionId(savedEntity.getId());
        if (dto.getRequetes() != null && !dto.getRequetes().isEmpty()) {
            saveRequetesUpdate(savedEntity, dto.getRequetes(), username);
        }
        
        return toDTO(savedEntity);
    }
    
    private void saveRequetes(InterfaceExtraction extraction, List<InterfaceExtractionRequeteCreateDTO> requeteDTOs, String username, InterfaceExtractionRequete parent) {
        for (InterfaceExtractionRequeteCreateDTO reqDto : requeteDTOs) {
            InterfaceExtractionRequete requete = new InterfaceExtractionRequete();
            requete.setInterfaceExtraction(extraction);
            requete.setScriptSql(reqDto.getScriptSql());
            requete.setParent(parent);
            requete.setOrdreExecution(reqDto.getOrdreExecution() != null ? reqDto.getOrdreExecution() : 1);
            requete.setTypeRequete(reqDto.getTypeRequete() != null ? reqDto.getTypeRequete() : InterfaceExtractionRequete.TypeRequete.PRINCIPALE);
            requete.setActif(reqDto.getActif() != null ? reqDto.getActif() : "Y");
            requete.setCreatedBy(username);
            requete.setCreatedOn(OffsetDateTime.now());
            requete.setRowscn(1);
            
            InterfaceExtractionRequete savedRequete = requeteRepository.save(requete);
            
            // Save params
            if (reqDto.getParams() != null && !reqDto.getParams().isEmpty()) {
                saveParamsCreate(savedRequete, reqDto.getParams(), username);
            }
            
            // Save liaisons
            if (reqDto.getLiaisons() != null && !reqDto.getLiaisons().isEmpty()) {
                saveLiaisonsCreate(savedRequete, reqDto.getLiaisons(), username);
            }
        }
    }
    
    private void saveRequetesUpdate(InterfaceExtraction extraction, List<InterfaceExtractionRequeteUpdateDTO> requeteDTOs, String username) {
        // First, save all requetes without parent (main queries)
        Map<Long, InterfaceExtractionRequete> savedRequetesMap = new HashMap<>();
        
        for (InterfaceExtractionRequeteUpdateDTO reqDto : requeteDTOs) {
            if (reqDto.getParentId() == null) {
                InterfaceExtractionRequete requete = new InterfaceExtractionRequete();
                requete.setInterfaceExtraction(extraction);
                requete.setScriptSql(reqDto.getScriptSql());
                requete.setParent(null);
                requete.setOrdreExecution(reqDto.getOrdreExecution() != null ? reqDto.getOrdreExecution() : 1);
                requete.setTypeRequete(reqDto.getTypeRequete() != null ? reqDto.getTypeRequete() : InterfaceExtractionRequete.TypeRequete.PRINCIPALE);
                requete.setActif(reqDto.getActif() != null ? reqDto.getActif() : "Y");
                requete.setCreatedBy(username);
                requete.setCreatedOn(OffsetDateTime.now());
                requete.setRowscn(1);
                
                InterfaceExtractionRequete savedRequete = requeteRepository.save(requete);
                savedRequetesMap.put(reqDto.getId(), savedRequete);
                
                // Save params
                if (reqDto.getParams() != null && !reqDto.getParams().isEmpty()) {
                    saveParamsUpdate(savedRequete, reqDto.getParams(), username);
                }
                
                // Save liaisons
                if (reqDto.getLiaisons() != null && !reqDto.getLiaisons().isEmpty()) {
                    saveLiaisonsUpdate(savedRequete, reqDto.getLiaisons(), username);
                }
            }
        }
        
        // Then, save child queries
        for (InterfaceExtractionRequeteUpdateDTO reqDto : requeteDTOs) {
            if (reqDto.getParentId() != null && savedRequetesMap.containsKey(reqDto.getParentId())) {
                InterfaceExtractionRequete requete = new InterfaceExtractionRequete();
                requete.setInterfaceExtraction(extraction);
                requete.setScriptSql(reqDto.getScriptSql());
                requete.setParent(savedRequetesMap.get(reqDto.getParentId()));
                requete.setOrdreExecution(reqDto.getOrdreExecution() != null ? reqDto.getOrdreExecution() : 1);
                requete.setTypeRequete(reqDto.getTypeRequete() != null ? reqDto.getTypeRequete() : InterfaceExtractionRequete.TypeRequete.SOUS_REQUETE);
                requete.setActif(reqDto.getActif() != null ? reqDto.getActif() : "Y");
                requete.setCreatedBy(username);
                requete.setCreatedOn(OffsetDateTime.now());
                requete.setRowscn(1);
                
                InterfaceExtractionRequete savedRequete = requeteRepository.save(requete);
                savedRequetesMap.put(reqDto.getId(), savedRequete);
                
                // Save params
                if (reqDto.getParams() != null && !reqDto.getParams().isEmpty()) {
                    saveParamsUpdate(savedRequete, reqDto.getParams(), username);
                }
                
                // Save liaisons
                if (reqDto.getLiaisons() != null && !reqDto.getLiaisons().isEmpty()) {
                    saveLiaisonsUpdate(savedRequete, reqDto.getLiaisons(), username);
                }
            }
        }
    }
    
    private void saveParamsCreate(InterfaceExtractionRequete requete, List<InterfaceExtractionParamCreateDTO> paramDTOs, String username) {
        for (InterfaceExtractionParamCreateDTO paramDto : paramDTOs) {
            InterfaceExtractionParam param = new InterfaceExtractionParam();
            param.setRequete(requete);
            param.setNomParam(paramDto.getNomParam());
            param.setTypeParam(paramDto.getTypeParam());
            param.setPosition(paramDto.getPosition());
            param.setObligatoire(paramDto.getObligatoire() != null ? paramDto.getObligatoire() : "Y");
            param.setActif(paramDto.getActif() != null ? paramDto.getActif() : "Y");
            param.setCreatedBy(username);
            param.setCreatedOn(OffsetDateTime.now());
            param.setRowscn(1);
            paramRepository.save(param);
        }
    }
    
    private void saveLiaisonsCreate(InterfaceExtractionRequete requeteFille, List<InterfaceExtractionLiaisonCreateDTO> liaisonDTOs, String username) {
        for (InterfaceExtractionLiaisonCreateDTO liaisonDto : liaisonDTOs) {
            InterfaceExtractionLiaison liaison = new InterfaceExtractionLiaison();
            liaison.setRequeteFille(requeteFille);
            liaison.setParamPosition(liaisonDto.getParamPosition());
            liaison.setSourceType(liaisonDto.getSourceType());
            liaison.setSourceValeur(liaisonDto.getSourceValeur());
            liaison.setCreatedBy(username);
            liaison.setCreatedOn(OffsetDateTime.now());
            liaison.setRowscn(1);
            liaisonRepository.save(liaison);
        }
    }
    
    private void saveParamsUpdate(InterfaceExtractionRequete requete, List<InterfaceExtractionParamUpdateDTO> paramDTOs, String username) {
        for (InterfaceExtractionParamUpdateDTO paramDto : paramDTOs) {
            InterfaceExtractionParam param = new InterfaceExtractionParam();
            param.setRequete(requete);
            param.setNomParam(paramDto.getNomParam());
            param.setTypeParam(paramDto.getTypeParam());
            param.setPosition(paramDto.getPosition());
            param.setObligatoire(paramDto.getObligatoire() != null ? paramDto.getObligatoire() : "Y");
            param.setActif(paramDto.getActif() != null ? paramDto.getActif() : "Y");
            param.setCreatedBy(username);
            param.setCreatedOn(OffsetDateTime.now());
            param.setRowscn(1);
            paramRepository.save(param);
        }
    }
    
    private void saveLiaisonsUpdate(InterfaceExtractionRequete requeteFille, List<InterfaceExtractionLiaisonUpdateDTO> liaisonDTOs, String username) {
        for (InterfaceExtractionLiaisonUpdateDTO liaisonDto : liaisonDTOs) {
            InterfaceExtractionLiaison liaison = new InterfaceExtractionLiaison();
            liaison.setRequeteFille(requeteFille);
            liaison.setParamPosition(liaisonDto.getParamPosition());
            liaison.setSourceType(liaisonDto.getSourceType());
            liaison.setSourceValeur(liaisonDto.getSourceValeur());
            liaison.setCreatedBy(username);
            liaison.setCreatedOn(OffsetDateTime.now());
            liaison.setRowscn(1);
            liaisonRepository.save(liaison);
        }
    }
    
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("InterfaceExtraction not found with id: " + id);
        }
        repository.deleteById(id);
    }
    
    @Transactional
    public byte[] exportToCsv(Long id, String username) {
        InterfaceExtraction extraction = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("InterfaceExtraction not found with id: " + id));
        return processService.exportToCsv(extraction, username);
    }
    
    private InterfaceExtractionDTO toDTO(InterfaceExtraction entity) {
        InterfaceExtractionDTO dto = new InterfaceExtractionDTO();
        dto.setId(entity.getId());
        dto.setCodeExtraction(entity.getCodeExtraction());
        dto.setDescription(entity.getDescription());
        dto.setSeparateur(entity.getSeparateur());
        dto.setEncadreur(entity.getEncadreur());
        dto.setActif(entity.getActif());
        
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        
        List<InterfaceExtractionRequete> requetes = requeteRepository.findByInterfaceExtractionIdOrderByOrdreExecutionAsc(entity.getId());
        if (requetes != null && !requetes.isEmpty()) {
            List<InterfaceExtractionRequeteDTO> requeteDTOs = requetes.stream()
                    .map(this::requeteToDTO)
                    .collect(Collectors.toList());
            dto.setRequetes(requeteDTOs);
        }
        
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        return dto;
    }
    
    private InterfaceExtractionRequeteDTO requeteToDTO(InterfaceExtractionRequete requete) {
        InterfaceExtractionRequeteDTO dto = new InterfaceExtractionRequeteDTO();
        dto.setId(requete.getId());
        dto.setInterfaceExtractionId(requete.getInterfaceExtraction().getId());
        dto.setScriptSql(requete.getScriptSql());
        if (requete.getParent() != null) {
            dto.setParentId(requete.getParent().getId());
        }
        dto.setOrdreExecution(requete.getOrdreExecution());
        dto.setTypeRequete(requete.getTypeRequete());
        dto.setActif(requete.getActif());
        
        List<InterfaceExtractionParam> params = paramRepository.findByRequeteIdOrderByPositionAsc(requete.getId());
        if (params != null && !params.isEmpty()) {
            dto.setParams(params.stream().map(this::paramToDTO).collect(Collectors.toList()));
        }
        
        List<InterfaceExtractionLiaison> liaisons = liaisonRepository.findByRequeteFilleIdOrderByParamPositionAsc(requete.getId());
        if (liaisons != null && !liaisons.isEmpty()) {
            dto.setLiaisons(liaisons.stream().map(this::liaisonToDTO).collect(Collectors.toList()));
        }
        
        dto.setCreatedBy(requete.getCreatedBy());
        dto.setCreatedOn(requete.getCreatedOn());
        dto.setUpdatedBy(requete.getUpdatedBy());
        dto.setUpdatedOn(requete.getUpdatedOn());
        dto.setRowscn(requete.getRowscn());
        
        return dto;
    }
    
    private InterfaceExtractionParamDTO paramToDTO(InterfaceExtractionParam param) {
        InterfaceExtractionParamDTO dto = new InterfaceExtractionParamDTO();
        dto.setId(param.getId());
        dto.setRequeteId(param.getRequete().getId());
        dto.setNomParam(param.getNomParam());
        dto.setTypeParam(param.getTypeParam());
        dto.setPosition(param.getPosition());
        dto.setObligatoire(param.getObligatoire());
        dto.setActif(param.getActif());
        dto.setCreatedBy(param.getCreatedBy());
        dto.setCreatedOn(param.getCreatedOn());
        dto.setUpdatedBy(param.getUpdatedBy());
        dto.setUpdatedOn(param.getUpdatedOn());
        dto.setRowscn(param.getRowscn());
        return dto;
    }
    
    private InterfaceExtractionLiaisonDTO liaisonToDTO(InterfaceExtractionLiaison liaison) {
        InterfaceExtractionLiaisonDTO dto = new InterfaceExtractionLiaisonDTO();
        dto.setId(liaison.getId());
        dto.setRequeteFilleId(liaison.getRequeteFille().getId());
        dto.setParamPosition(liaison.getParamPosition());
        dto.setSourceType(liaison.getSourceType());
        dto.setSourceValeur(liaison.getSourceValeur());
        dto.setCreatedBy(liaison.getCreatedBy());
        dto.setCreatedOn(liaison.getCreatedOn());
        dto.setUpdatedBy(liaison.getUpdatedBy());
        dto.setUpdatedOn(liaison.getUpdatedOn());
        dto.setRowscn(liaison.getRowscn());
        return dto;
    }
}
