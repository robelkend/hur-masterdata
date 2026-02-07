package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.RefFormuleTokenCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefFormuleTokenDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefFormuleTokenUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.RefFormuleToken;
import com.rsoft.hurmanagement.hurmasterdata.repository.RefFormuleTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefFormuleTokenService {
    
    private final RefFormuleTokenRepository repository;
    
    @Transactional(readOnly = true)
    public Page<RefFormuleTokenDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public RefFormuleTokenDTO findById(String codeElement) {
        RefFormuleToken entity = repository.findById(codeElement)
                .orElseThrow(() -> new RuntimeException("RefFormuleToken not found with code: " + codeElement));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<RefFormuleTokenDTO> findAllForDropdown() {
        return repository.findAllByOrderByTypeElementAscLibelleAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RefFormuleTokenDTO create(RefFormuleTokenCreateDTO dto, String username) {
        if (repository.existsByCodeElement(dto.getCodeElement())) {
            throw new RuntimeException("RefFormuleToken with code " + dto.getCodeElement() + " already exists");
        }
        
        RefFormuleToken entity = new RefFormuleToken();
        entity.setCodeElement(dto.getCodeElement());
        entity.setTypeElement(dto.getTypeElement());
        entity.setSymbole(dto.getSymbole());
        entity.setLibelle(dto.getLibelle());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        // rowscn is managed automatically by Hibernate @Version annotation
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public RefFormuleTokenDTO update(String codeElement, RefFormuleTokenUpdateDTO dto, String username) {
        RefFormuleToken entity = repository.findById(codeElement)
                .orElseThrow(() -> new RuntimeException("RefFormuleToken not found with code: " + codeElement));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setTypeElement(dto.getTypeElement());
        entity.setSymbole(dto.getSymbole());
        entity.setLibelle(dto.getLibelle());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(String codeElement, Integer rowscn) {
        RefFormuleToken entity = repository.findById(codeElement)
                .orElseThrow(() -> new RuntimeException("RefFormuleToken not found with code: " + codeElement));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private RefFormuleTokenDTO toDTO(RefFormuleToken entity) {
        RefFormuleTokenDTO dto = new RefFormuleTokenDTO();
        dto.setCodeElement(entity.getCodeElement());
        dto.setTypeElement(entity.getTypeElement());
        dto.setSymbole(entity.getSymbole());
        dto.setLibelle(entity.getLibelle());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
