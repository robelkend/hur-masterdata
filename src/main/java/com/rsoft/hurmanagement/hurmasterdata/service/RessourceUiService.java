package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.RessourceUiDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.RessourceUi;
import com.rsoft.hurmanagement.hurmasterdata.repository.RessourceUiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RessourceUiService {
    private final RessourceUiRepository repository;

    @Transactional(readOnly = true)
    public List<RessourceUiDTO> findAllForDropdown() {
        return repository.findAllByOrderByCodeResourceAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    private RessourceUiDTO toDTO(RessourceUi entity) {
        RessourceUiDTO dto = new RessourceUiDTO();
        dto.setId(entity.getId());
        dto.setCodeResource(entity.getCodeResource());
        dto.setLibelle(entity.getLibelle());
        dto.setTypeResource(entity.getTypeResource());
        dto.setParentId(entity.getParent() == null ? null : entity.getParent().getId());
        dto.setEstMenu(entity.getEstMenu());
        dto.setActif(entity.getActif());
        return dto;
    }
}
