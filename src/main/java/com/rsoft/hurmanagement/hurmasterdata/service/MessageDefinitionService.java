package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.MessageDefinition;
import com.rsoft.hurmanagement.hurmasterdata.entity.MessageDestinataire;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.MessageDefinitionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.MessageDestinataireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageDefinitionService {
    
    private final MessageDefinitionRepository repository;
    private final MessageDestinataireRepository destinataireRepository;
    private final EntrepriseRepository entrepriseRepository;
    
    @Transactional(readOnly = true)
    public Page<MessageDefinitionDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public MessageDefinitionDTO findById(Long idMessage) {
        MessageDefinition entity = repository.findById(idMessage)
                .orElseThrow(() -> new RuntimeException("MessageDefinition not found with id: " + idMessage));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<MessageDefinitionDTO> findAllForDropdown() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "codeMessage"))
                .stream()
                .filter(m -> "Y".equals(m.getActif()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public MessageDefinitionDTO create(MessageDefinitionCreateDTO dto, String username) {
        if (repository.existsByCodeMessage(dto.getCodeMessage())) {
            throw new RuntimeException("MessageDefinition with code " + dto.getCodeMessage() + " already exists");
        }
        
        MessageDefinition entity = new MessageDefinition();
        entity.setCodeMessage(dto.getCodeMessage());
        entity.setTitre(dto.getTitre());
        entity.setLangue(dto.getLangue());
        entity.setFrequence(MessageDefinition.Frequence.valueOf(dto.getFrequence()));
        entity.setEmailEnvoye(dto.getEmailEnvoye() != null ? dto.getEmailEnvoye() : "N");
        entity.setFormat(MessageDefinition.Format.valueOf(dto.getFormat()));
        entity.setContenu(dto.getContenu());
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        }
        
        MessageDefinition savedEntity = repository.save(entity);
        
        // Save destinataires if provided
        if (dto.getDestinataires() != null && !dto.getDestinataires().isEmpty()) {
            saveDestinataires(savedEntity, dto.getDestinataires(), username);
        }
        
        return toDTO(savedEntity);
    }
    
    @Transactional
    public MessageDefinitionDTO update(MessageDefinitionUpdateDTO dto, String username) {
        MessageDefinition entity = repository.findById(dto.getIdMessage())
                .orElseThrow(() -> new RuntimeException("MessageDefinition not found with id: " + dto.getIdMessage()));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        if (!entity.getCodeMessage().equals(dto.getCodeMessage()) && repository.existsByCodeMessage(dto.getCodeMessage())) {
            throw new RuntimeException("MessageDefinition with code " + dto.getCodeMessage() + " already exists");
        }
        
        entity.setCodeMessage(dto.getCodeMessage());
        entity.setTitre(dto.getTitre());
        entity.setLangue(dto.getLangue());
        entity.setFrequence(MessageDefinition.Frequence.valueOf(dto.getFrequence()));
        entity.setEmailEnvoye(dto.getEmailEnvoye() != null ? dto.getEmailEnvoye() : "N");
        entity.setFormat(MessageDefinition.Format.valueOf(dto.getFormat()));
        entity.setContenu(dto.getContenu());
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
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
        
        MessageDefinition savedEntity = repository.save(entity);
        
        // Update destinataires - delete all existing and recreate
        destinataireRepository.deleteByMessageIdMessage(savedEntity.getIdMessage());
        if (dto.getDestinataires() != null && !dto.getDestinataires().isEmpty()) {
            saveDestinatairesUpdate(savedEntity, dto.getDestinataires(), username);
        }
        
        return toDTO(savedEntity);
    }
    
    @Transactional
    public void delete(Long idMessage, Integer rowscn) {
        MessageDefinition entity = repository.findById(idMessage)
                .orElseThrow(() -> new RuntimeException("MessageDefinition not found with id: " + idMessage));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private void saveDestinataires(MessageDefinition message, List<MessageDestinataireCreateDTO> destinataireDTOs, String username) {
        for (MessageDestinataireCreateDTO destDto : destinataireDTOs) {
            MessageDestinataire destinataire = new MessageDestinataire();
            destinataire.setMessage(message);
            destinataire.setTypeCible(MessageDestinataire.TypeCible.valueOf(destDto.getTypeCible()));
            destinataire.setValeurCible(destDto.getValeurCible());
            destinataire.setModeEnvoi(MessageDestinataire.ModeEnvoi.valueOf(destDto.getModeEnvoi()));
            destinataire.setCreatedBy(username);
            destinataire.setCreatedOn(OffsetDateTime.now());
            destinataireRepository.save(destinataire);
        }
    }
    
    private void saveDestinatairesUpdate(MessageDefinition message, List<MessageDestinataireUpdateDTO> destinataireDTOs, String username) {
        for (MessageDestinataireUpdateDTO destDto : destinataireDTOs) {
            MessageDestinataire destinataire = new MessageDestinataire();
            destinataire.setMessage(message);
            destinataire.setTypeCible(MessageDestinataire.TypeCible.valueOf(destDto.getTypeCible()));
            destinataire.setValeurCible(destDto.getValeurCible());
            destinataire.setModeEnvoi(MessageDestinataire.ModeEnvoi.valueOf(destDto.getModeEnvoi()));
            destinataire.setCreatedBy(username);
            destinataire.setCreatedOn(OffsetDateTime.now());
            destinataireRepository.save(destinataire);
        }
    }
    
    private MessageDefinitionDTO toDTO(MessageDefinition entity) {
        MessageDefinitionDTO dto = new MessageDefinitionDTO();
        dto.setIdMessage(entity.getIdMessage());
        dto.setCodeMessage(entity.getCodeMessage());
        dto.setTitre(entity.getTitre());
        dto.setLangue(entity.getLangue());
        dto.setFrequence(entity.getFrequence().name());
        dto.setEmailEnvoye(entity.getEmailEnvoye());
        dto.setFormat(entity.getFormat().name());
        dto.setContenu(entity.getContenu());
        dto.setActif(entity.getActif());
        dto.setEntrepriseId(entity.getEntreprise() != null ? entity.getEntreprise().getId() : null);
        dto.setEntrepriseCode(entity.getEntreprise() != null ? entity.getEntreprise().getCodeEntreprise() : null);
        dto.setEntrepriseNom(entity.getEntreprise() != null ? entity.getEntreprise().getNomEntreprise() : null);
        
        List<MessageDestinataire> destinataires = destinataireRepository.findByMessageIdMessage(entity.getIdMessage());
        if (destinataires != null && !destinataires.isEmpty()) {
            dto.setDestinataires(destinataires.stream().map(this::destinataireToDTO).collect(Collectors.toList()));
        }
        
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
    
    private MessageDestinataireDTO destinataireToDTO(MessageDestinataire entity) {
        MessageDestinataireDTO dto = new MessageDestinataireDTO();
        dto.setId(entity.getId());
        dto.setMessageId(entity.getMessage().getIdMessage());
        dto.setTypeCible(entity.getTypeCible().name());
        dto.setValeurCible(entity.getValeurCible());
        dto.setModeEnvoi(entity.getModeEnvoi().name());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
