package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.AlertConfigCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AlertConfigDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AlertConfigUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.AlertConfig;
import com.rsoft.hurmanagement.hurmasterdata.entity.MessageDefinition;
import com.rsoft.hurmanagement.hurmasterdata.repository.AlertConfigRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.MessageDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AlertConfigService {
    
    private final AlertConfigRepository repository;
    private final MessageDefinitionRepository messageDefinitionRepository;
    
    @Transactional(readOnly = true)
    public Page<AlertConfigDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public AlertConfigDTO findById(Long id) {
        AlertConfig entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AlertConfig not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public AlertConfigDTO findByCodeMessage(String codeMessage) {
        AlertConfig entity = repository.findByMessageDefinition_CodeMessage(codeMessage)
                .orElseThrow(() -> new RuntimeException("AlertConfig not found with code message: " + codeMessage));
        return toDTO(entity);
    }
    
    @Transactional
    public AlertConfigDTO create(AlertConfigCreateDTO dto, String username) {
        if (repository.existsByMessageDefinition_CodeMessage(dto.getCodeMessage())) {
            throw new RuntimeException("AlertConfig already exists for code message: " + dto.getCodeMessage());
        }
        
        MessageDefinition messageDefinition = messageDefinitionRepository.findByCodeMessage(dto.getCodeMessage())
                .orElseThrow(() -> new RuntimeException("MessageDefinition not found with code: " + dto.getCodeMessage()));
        
        AlertConfig entity = new AlertConfig();
        entity.setMessageDefinition(messageDefinition);
        entity.setEmail(dto.getEmail());
        entity.setPushAlert(dto.getPushAlert());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public AlertConfigDTO update(Long id, AlertConfigUpdateDTO dto, String username) {
        AlertConfig entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AlertConfig not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setEmail(dto.getEmail());
        entity.setPushAlert(dto.getPushAlert());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        AlertConfig entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AlertConfig not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private AlertConfigDTO toDTO(AlertConfig entity) {
        AlertConfigDTO dto = new AlertConfigDTO();
        dto.setId(entity.getId());
        dto.setCodeMessage(entity.getMessageDefinition() != null ? entity.getMessageDefinition().getCodeMessage() : null);
        dto.setMessageTitre(entity.getMessageDefinition() != null ? entity.getMessageDefinition().getTitre() : null);
        dto.setEmail(entity.getEmail());
        dto.setPushAlert(entity.getPushAlert());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
