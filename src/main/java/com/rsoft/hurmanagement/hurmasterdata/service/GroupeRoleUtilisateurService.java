package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.GroupeRoleUtilisateurCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.GroupeRoleUtilisateurDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.GroupeRoleUtilisateur;
import com.rsoft.hurmanagement.hurmasterdata.entity.RoleGroupe;
import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import com.rsoft.hurmanagement.hurmasterdata.repository.GroupeRoleUtilisateurRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RoleGroupeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupeRoleUtilisateurService {
    private final GroupeRoleUtilisateurRepository repository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleGroupeRepository roleGroupeRepository;

    @Transactional(readOnly = true)
    public List<GroupeRoleUtilisateurDTO> findByUtilisateurId(Long utilisateurId) {
        return repository.findByUtilisateurIdOrderByIdAsc(utilisateurId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public List<GroupeRoleUtilisateurDTO> replaceForUtilisateur(Long utilisateurId,
                                                                List<GroupeRoleUtilisateurCreateDTO> dtos,
                                                                String username) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "utilisateur.error.notFound"));

        List<GroupeRoleUtilisateurCreateDTO> safeDtos = dtos == null ? List.of() : dtos;
        List<GroupeRoleUtilisateurCreateDTO> uniqueDtos = safeDtos.stream()
                .filter(dto -> dto.getGroupeId() != null)
                .collect(java.util.stream.Collectors.toMap(
                        GroupeRoleUtilisateurCreateDTO::getGroupeId,
                        dto -> dto,
                        (existing, ignored) -> existing,
                        java.util.LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();

        long primaryCount = uniqueDtos.stream()
                .filter(dto -> "Y".equalsIgnoreCase(dto.getEstPrimaire()))
                .count();
        if (primaryCount > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "utilisateur.error.multiplePrimaryGroup");
        }

        repository.deleteByUtilisateurId(utilisateurId);

        List<GroupeRoleUtilisateur> saved = uniqueDtos.stream().map(dto -> {
            RoleGroupe groupe = roleGroupeRepository.findById(dto.getGroupeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "utilisateur.error.groupNotFound"));
            GroupeRoleUtilisateur entity = new GroupeRoleUtilisateur();
            entity.setUtilisateur(utilisateur);
            entity.setGroupe(groupe);
            entity.setEstPrimaire(dto.getEstPrimaire() == null ? "N" : dto.getEstPrimaire());
            entity.setCreatedBy(username);
            entity.setCreatedOn(OffsetDateTime.now());
            entity.setRowscn(1);
            return entity;
        }).toList();

        return repository.saveAll(saved).stream().map(this::toDTO).toList();
    }

    private GroupeRoleUtilisateurDTO toDTO(GroupeRoleUtilisateur entity) {
        GroupeRoleUtilisateurDTO dto = new GroupeRoleUtilisateurDTO();
        dto.setId(entity.getId());
        dto.setUtilisateurId(entity.getUtilisateur().getId());
        dto.setGroupeId(entity.getGroupe().getId());
        dto.setGroupeCode(entity.getGroupe().getCodeGroupe());
        dto.setGroupeLibelle(entity.getGroupe().getLibelle());
        dto.setEstPrimaire(entity.getEstPrimaire());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
