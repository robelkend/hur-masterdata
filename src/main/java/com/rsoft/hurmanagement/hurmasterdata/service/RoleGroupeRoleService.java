package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.RoleGroupeRoleCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RoleGroupeRoleDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.AppRole;
import com.rsoft.hurmanagement.hurmasterdata.entity.RoleGroupe;
import com.rsoft.hurmanagement.hurmasterdata.entity.RoleGroupeRole;
import com.rsoft.hurmanagement.hurmasterdata.repository.AppRoleRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RoleGroupeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RoleGroupeRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleGroupeRoleService {
    private final RoleGroupeRoleRepository repository;
    private final RoleGroupeRepository roleGroupeRepository;
    private final AppRoleRepository appRoleRepository;

    @Transactional(readOnly = true)
    public List<RoleGroupeRoleDTO> findByGroupeId(Long groupeId) {
        return repository.findByGroupeIdOrderByIdAsc(groupeId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public List<RoleGroupeRoleDTO> replaceForGroupe(Long groupeId,
                                                    List<RoleGroupeRoleCreateDTO> dtos,
                                                    String username) {
        RoleGroupe groupe = roleGroupeRepository.findById(groupeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "roleGroupe.error.notFound"));

        repository.deleteByGroupeId(groupeId);
        repository.flush();

        List<RoleGroupeRoleCreateDTO> safeDtos = dtos == null ? List.of() : dtos;
        List<RoleGroupeRoleCreateDTO> uniqueDtos = safeDtos.stream()
                .filter(dto -> dto.getRoleId() != null)
                .collect(java.util.stream.Collectors.toMap(
                        RoleGroupeRoleCreateDTO::getRoleId,
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

        List<RoleGroupeRole> saved = uniqueDtos.stream().map(dto -> {
            AppRole role = appRoleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "appRole.error.notFound"));
            RoleGroupeRole entity = new RoleGroupeRole();
            entity.setGroupe(groupe);
            entity.setRole(role);
            entity.setActif(dto.getActif() == null ? "N" : dto.getActif());
            entity.setCreatedBy(username);
            entity.setCreatedOn(OffsetDateTime.now());
            entity.setRowscn(1);
            return entity;
        }).toList();

        return repository.saveAll(saved).stream().map(this::toDTO).toList();
    }

    private RoleGroupeRoleDTO toDTO(RoleGroupeRole entity) {
        RoleGroupeRoleDTO dto = new RoleGroupeRoleDTO();
        dto.setId(entity.getId());
        dto.setGroupeId(entity.getGroupe().getId());
        dto.setRoleId(entity.getRole().getId());
        dto.setRoleCode(entity.getRole().getCodeRole());
        dto.setRoleLibelle(entity.getRole().getLibelle());
        dto.setActif(entity.getActif());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
