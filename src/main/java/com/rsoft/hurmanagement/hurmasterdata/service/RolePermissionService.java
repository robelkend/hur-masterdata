package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.RolePermissionCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RolePermissionDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.AppRole;
import com.rsoft.hurmanagement.hurmasterdata.entity.PermissionAction;
import com.rsoft.hurmanagement.hurmasterdata.entity.RessourceUi;
import com.rsoft.hurmanagement.hurmasterdata.entity.RolePermission;
import com.rsoft.hurmanagement.hurmasterdata.repository.AppRoleRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PermissionActionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RessourceUiRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionService {
    private final RolePermissionRepository repository;
    private final AppRoleRepository appRoleRepository;
    private final RessourceUiRepository ressourceUiRepository;
    private final PermissionActionRepository permissionActionRepository;

    @Transactional(readOnly = true)
    public List<RolePermissionDTO> findByRoleId(Long roleId) {
        return repository.findByRoleIdOrderByIdAsc(roleId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public List<RolePermissionDTO> replaceForRole(Long roleId,
                                                  List<RolePermissionCreateDTO> dtos,
                                                  String username) {
        AppRole role = appRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "appRole.error.notFound"));

        repository.deleteByRoleId(roleId);
        repository.flush();

        List<RolePermissionCreateDTO> safeDtos = dtos == null ? List.of() : dtos;
        List<RolePermissionCreateDTO> uniqueDtos = safeDtos.stream()
                .filter(dto -> dto.getRessourceId() != null && dto.getActionId() != null)
                .collect(java.util.stream.Collectors.toMap(
                        dto -> dto.getRessourceId() + ":" + dto.getActionId(),
                        dto -> dto,
                        (existing, ignored) -> existing,
                        java.util.LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();

        if (uniqueDtos.isEmpty()) {
            return List.of();
        }

        List<RolePermission> saved = uniqueDtos.stream().map(dto -> {
            RessourceUi ressource = ressourceUiRepository.findById(dto.getRessourceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ressourceUi.error.notFound"));
            PermissionAction action = permissionActionRepository.findById(dto.getActionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "permissionAction.error.notFound"));
            RolePermission entity = new RolePermission();
            entity.setRole(role);
            entity.setRessource(ressource);
            entity.setAction(action);
            entity.setEffet(dto.getEffet() == null ? RolePermission.Effet.ALLOW : dto.getEffet());
            entity.setHeritageDescendant(dto.getHeritageDescendant() == null ? true : dto.getHeritageDescendant());
            entity.setActif(dto.getActif() == null ? "N" : dto.getActif());
            entity.setCreatedBy(username);
            entity.setCreatedOn(OffsetDateTime.now());
            entity.setRowscn(1);
            return entity;
        }).toList();

        return repository.saveAll(saved).stream().map(this::toDTO).toList();
    }

    private RolePermissionDTO toDTO(RolePermission entity) {
        RolePermissionDTO dto = new RolePermissionDTO();
        dto.setId(entity.getId());
        dto.setRoleId(entity.getRole().getId());
        dto.setRessourceId(entity.getRessource().getId());
        dto.setRessourceCode(entity.getRessource().getCodeResource());
        dto.setRessourceLibelle(entity.getRessource().getLibelle());
        dto.setActionId(entity.getAction().getId());
        dto.setActionCode(entity.getAction().getCodeAction());
        dto.setActionLibelle(entity.getAction().getLibelle());
        dto.setEffet(entity.getEffet());
        dto.setHeritageDescendant(entity.getHeritageDescendant());
        dto.setActif(entity.getActif());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
