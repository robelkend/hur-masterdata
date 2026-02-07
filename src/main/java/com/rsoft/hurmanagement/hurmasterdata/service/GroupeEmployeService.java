package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.GroupeEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.GroupeEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.GroupeEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.GroupeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.GroupeEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class GroupeEmployeService {
    
    private final GroupeEmployeRepository repository;
    
    @Transactional(readOnly = true)
    public Page<GroupeEmployeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public GroupeEmployeDTO findById(Long id) {
        GroupeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("GroupeEmploye not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public GroupeEmployeDTO create(GroupeEmployeCreateDTO dto, String username) {
        if (repository.existsByCodeGroupe(dto.getCodeGroupe())) {
            throw new RuntimeException("GroupeEmploye with code " + dto.getCodeGroupe() + " already exists");
        }
        
        GroupeEmploye entity = new GroupeEmploye();
        entity.setCodeGroupe(dto.getCodeGroupe());
        entity.setDescription(dto.getDescription());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public GroupeEmployeDTO update(Long id, GroupeEmployeUpdateDTO dto, String username) {
        GroupeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("GroupeEmploye not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setDescription(dto.getDescription());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        GroupeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("GroupeEmploye not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }
    
    private GroupeEmployeDTO toDTO(GroupeEmploye entity) {
        GroupeEmployeDTO dto = new GroupeEmployeDTO();
        dto.setId(entity.getId());
        dto.setCodeGroupe(entity.getCodeGroupe());
        dto.setDescription(entity.getDescription());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
