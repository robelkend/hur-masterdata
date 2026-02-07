package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoading;
import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoadingChamp;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceLoadingRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceLoadingChampRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterfaceLoadingService {
    
    private final InterfaceLoadingRepository repository;
    private final InterfaceLoadingChampRepository champRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final InterfaceLoadingProcessService processService;
    private final JdbcTemplate jdbcTemplate;
    
    @Transactional(readOnly = true)
    public Page<InterfaceLoadingDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public InterfaceLoadingDTO findById(Long id) {
        InterfaceLoading entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("InterfaceLoading not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<InterfaceLoadingDTO> findAllForDropdown() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public InterfaceLoadingDTO create(InterfaceLoadingCreateDTO dto, String username) {
        if (repository.existsByCodeLoading(dto.getCodeLoading())) {
            throw new RuntimeException("InterfaceLoading with code " + dto.getCodeLoading() + " already exists");
        }
        
        InterfaceLoading entity = new InterfaceLoading();
        entity.setCodeLoading(dto.getCodeLoading());
        entity.setDescription(dto.getDescription());
        entity.setSource(dto.getSource() != null ? dto.getSource() : InterfaceLoading.Source.FILE);
        entity.setExclusDerniereLigne(dto.getExclusDerniereLigne() != null ? dto.getExclusDerniereLigne() : "N");
        entity.setSeparateurChamp(dto.getSeparateurChamp());
        entity.setDelimiteurChamp(dto.getDelimiteurChamp());
        entity.setExclusLignes(dto.getExclusLignes() != null ? dto.getExclusLignes() : 0);
        entity.setTableCible(dto.getTableCible());
        entity.setTableSource(dto.getTableSource());
        entity.setExtraClause(dto.getExtraClause());
        entity.setRdbUrl(dto.getRdbUrl());
        entity.setRdbDriver(dto.getRdbDriver());
        entity.setRdbUsername(dto.getRdbUsername());
        entity.setRdbPassword(dto.getRdbPassword());
        entity.setRdbSchema(dto.getRdbSchema());
        entity.setRdbQuery(dto.getRdbQuery());
        entity.setApiBaseUrl(dto.getApiBaseUrl());
        entity.setApiEndpoint(dto.getApiEndpoint());
        entity.setApiMethod(dto.getApiMethod());
        entity.setApiAuthType(dto.getApiAuthType());
        entity.setApiUsername(dto.getApiUsername());
        entity.setApiPassword(dto.getApiPassword());
        entity.setApiToken(dto.getApiToken());
        entity.setApiHeaders(dto.getApiHeaders());
        entity.setApiQueryParams(dto.getApiQueryParams());
        entity.setApiBody(dto.getApiBody());
        entity.setApiTimeoutMs(dto.getApiTimeoutMs());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        }
        
        InterfaceLoading savedEntity = repository.save(entity);
        
        // Save champs if provided
        if (dto.getChamps() != null && !dto.getChamps().isEmpty()) {
            List<InterfaceLoadingChamp> champs = dto.getChamps().stream()
                    .map(c -> {
                        InterfaceLoadingChamp champ = new InterfaceLoadingChamp();
                        champ.setLoading(savedEntity);
                        champ.setNomCible(c.getNomCible());
                        champ.setNomSource(c.getNomSource());
                        champ.setTypeDonnee(c.getTypeDonnee() != null ? c.getTypeDonnee() : InterfaceLoadingChamp.TypeDonnee.CHAR);
                        champ.setTaille(c.getTaille());
                        champ.setFormat(c.getFormat());
                        champ.setPosition(c.getPosition());
                        champ.setValeur(c.getValeur());
                        champ.setUpdateChamp(c.getUpdateChamp());
                        champ.setUpdateValeur(c.getUpdateValeur());
                        champ.setUpdateCondition(c.getUpdateCondition());
                        champ.setObligatoire(c.getObligatoire() != null ? c.getObligatoire() : "N");
                        champ.setCreatedBy(username);
                        champ.setCreatedOn(OffsetDateTime.now());
                        champ.setRowscn(1);
                        return champ;
                    })
                    .collect(Collectors.toList());
            champRepository.saveAll(champs);
        }
        
        return toDTO(savedEntity);
    }
    
    @Transactional
    public InterfaceLoadingDTO update(InterfaceLoadingUpdateDTO dto, String username) {
        InterfaceLoading entity = repository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("InterfaceLoading not found with id: " + dto.getId()));
        
        // Check optimistic concurrency
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Check code uniqueness if changed
        if (!entity.getCodeLoading().equals(dto.getCodeLoading()) && repository.existsByCodeLoading(dto.getCodeLoading())) {
            throw new RuntimeException("InterfaceLoading with code " + dto.getCodeLoading() + " already exists");
        }
        
        entity.setCodeLoading(dto.getCodeLoading());
        entity.setDescription(dto.getDescription());
        entity.setSource(dto.getSource());
        entity.setExclusDerniereLigne(dto.getExclusDerniereLigne());
        entity.setSeparateurChamp(dto.getSeparateurChamp());
        entity.setDelimiteurChamp(dto.getDelimiteurChamp());
        entity.setExclusLignes(dto.getExclusLignes());
        entity.setTableCible(dto.getTableCible());
        entity.setTableSource(dto.getTableSource());
        entity.setExtraClause(dto.getExtraClause());
        entity.setRdbUrl(dto.getRdbUrl());
        entity.setRdbDriver(dto.getRdbDriver());
        entity.setRdbUsername(dto.getRdbUsername());
        entity.setRdbPassword(dto.getRdbPassword());
        entity.setRdbSchema(dto.getRdbSchema());
        entity.setRdbQuery(dto.getRdbQuery());
        entity.setApiBaseUrl(dto.getApiBaseUrl());
        entity.setApiEndpoint(dto.getApiEndpoint());
        entity.setApiMethod(dto.getApiMethod());
        entity.setApiAuthType(dto.getApiAuthType());
        entity.setApiUsername(dto.getApiUsername());
        entity.setApiPassword(dto.getApiPassword());
        entity.setApiToken(dto.getApiToken());
        entity.setApiHeaders(dto.getApiHeaders());
        entity.setApiQueryParams(dto.getApiQueryParams());
        entity.setApiBody(dto.getApiBody());
        entity.setApiTimeoutMs(dto.getApiTimeoutMs());
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
        
        InterfaceLoading savedEntity = repository.save(entity);
        
        // Update champs - delete all existing and recreate
        champRepository.deleteByLoadingId(savedEntity.getId());
        if (dto.getChamps() != null && !dto.getChamps().isEmpty()) {
            List<InterfaceLoadingChamp> champs = dto.getChamps().stream()
                    .map(c -> {
                        InterfaceLoadingChamp champ = new InterfaceLoadingChamp();
                        champ.setLoading(savedEntity);
                        champ.setNomCible(c.getNomCible());
                        champ.setNomSource(c.getNomSource());
                        champ.setTypeDonnee(c.getTypeDonnee());
                        champ.setTaille(c.getTaille());
                        champ.setFormat(c.getFormat());
                        champ.setPosition(c.getPosition());
                        champ.setValeur(c.getValeur());
                        champ.setUpdateChamp(c.getUpdateChamp());
                        champ.setUpdateValeur(c.getUpdateValeur());
                        champ.setUpdateCondition(c.getUpdateCondition());
                        champ.setObligatoire(c.getObligatoire());
                        champ.setCreatedBy(username);
                        champ.setCreatedOn(OffsetDateTime.now());
                        champ.setRowscn(1);
                        return champ;
                    })
                    .collect(Collectors.toList());
            champRepository.saveAll(champs);
        }
        
        return toDTO(savedEntity);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("InterfaceLoading not found with id: " + id);
        }
        repository.deleteById(id);
    }
    
    private InterfaceLoadingDTO toDTO(InterfaceLoading entity) {
        InterfaceLoadingDTO dto = new InterfaceLoadingDTO();
        dto.setId(entity.getId());
        dto.setCodeLoading(entity.getCodeLoading());
        dto.setDescription(entity.getDescription());
        dto.setSource(entity.getSource());
        dto.setExclusDerniereLigne(entity.getExclusDerniereLigne());
        dto.setSeparateurChamp(entity.getSeparateurChamp());
        dto.setDelimiteurChamp(entity.getDelimiteurChamp());
        dto.setExclusLignes(entity.getExclusLignes());
        dto.setTableCible(entity.getTableCible());
        dto.setTableSource(entity.getTableSource());
        dto.setExtraClause(entity.getExtraClause());
        dto.setRdbUrl(entity.getRdbUrl());
        dto.setRdbDriver(entity.getRdbDriver());
        dto.setRdbUsername(entity.getRdbUsername());
        dto.setRdbPassword(entity.getRdbPassword());
        dto.setRdbSchema(entity.getRdbSchema());
        dto.setRdbQuery(entity.getRdbQuery());
        dto.setApiBaseUrl(entity.getApiBaseUrl());
        dto.setApiEndpoint(entity.getApiEndpoint());
        dto.setApiMethod(entity.getApiMethod());
        dto.setApiAuthType(entity.getApiAuthType());
        dto.setApiUsername(entity.getApiUsername());
        dto.setApiPassword(entity.getApiPassword());
        dto.setApiToken(entity.getApiToken());
        dto.setApiHeaders(entity.getApiHeaders());
        dto.setApiQueryParams(entity.getApiQueryParams());
        dto.setApiBody(entity.getApiBody());
        dto.setApiTimeoutMs(entity.getApiTimeoutMs());
        
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        
        // Load champs explicitly
        List<InterfaceLoadingChamp> champs = champRepository.findByLoadingId(entity.getId());
        if (champs != null && !champs.isEmpty()) {
            List<InterfaceLoadingChampDTO> champDTOs = champs.stream()
                    .map(this::champToDTO)
                    .collect(Collectors.toList());
            dto.setChamps(champDTOs);
        }
        
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        return dto;
    }
    
    private InterfaceLoadingChampDTO champToDTO(InterfaceLoadingChamp champ) {
        InterfaceLoadingChampDTO dto = new InterfaceLoadingChampDTO();
        dto.setId(champ.getId());
        dto.setLoadingId(champ.getLoading().getId());
        dto.setNomCible(champ.getNomCible());
        dto.setNomSource(champ.getNomSource());
        dto.setTypeDonnee(champ.getTypeDonnee());
        dto.setTaille(champ.getTaille());
        dto.setFormat(champ.getFormat());
        dto.setPosition(champ.getPosition());
        dto.setValeur(champ.getValeur());
        dto.setUpdateChamp(champ.getUpdateChamp());
        dto.setUpdateValeur(champ.getUpdateValeur());
        dto.setUpdateCondition(champ.getUpdateCondition());
        dto.setObligatoire(champ.getObligatoire());
        dto.setCreatedBy(champ.getCreatedBy());
        dto.setCreatedOn(champ.getCreatedOn());
        dto.setUpdatedBy(champ.getUpdatedBy());
        dto.setUpdatedOn(champ.getUpdatedOn());
        dto.setRowscn(champ.getRowscn());
        return dto;
    }
    
    public Map<String, Object> loadCsv(Long id, MultipartFile file, String username) {
        return processService.loadCsv(id, file, username);
    }
    
    public Map<String, Object> loadFromDatabase(Long id, String username) {
        return processService.loadFromDatabase(id, username);
    }

    public Map<String, Object> loadFromDatabase(Long id, String username, Map<String, String> params) {
        return processService.loadFromDatabaseWithParams(id, username, params);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> testRdbConnection(Long id) {
        return processService.testRdbConnection(id);
    }
    
    public Map<String, Object> loadFromApi(Long id, String username) {
        return processService.loadFromApi(id, username);
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTableColumns(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Query PostgreSQL information_schema to get column names and nullable status
        String sql = "SELECT column_name, is_nullable, data_type " +
                    "FROM information_schema.columns " +
                    "WHERE table_name = LOWER(?) " +
                    "AND table_schema = 'public' " +
                    "ORDER BY ordinal_position";
        
        try {
            return jdbcTemplate.queryForList(sql, tableName.trim().toLowerCase());
        } catch (Exception e) {
            // If table doesn't exist or error, return empty list
            throw new RuntimeException("Error retrieving columns for table " + tableName + ": " + e.getMessage(), e);
        }
    }
}
