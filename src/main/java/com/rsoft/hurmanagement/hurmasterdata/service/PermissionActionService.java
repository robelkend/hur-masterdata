package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PermissionActionDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.PermissionAction;
import com.rsoft.hurmanagement.hurmasterdata.repository.PermissionActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionActionService {
    private final PermissionActionRepository repository;

    @Transactional(readOnly = true)
    public List<PermissionActionDTO> findAllForDropdown() {
        return repository.findAllByOrderByCodeActionAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    private PermissionActionDTO toDTO(PermissionAction entity) {
        PermissionActionDTO dto = new PermissionActionDTO();
        dto.setId(entity.getId());
        dto.setCodeAction(entity.getCodeAction());
        dto.setLibelle(entity.getLibelle());
        return dto;
    }
}
