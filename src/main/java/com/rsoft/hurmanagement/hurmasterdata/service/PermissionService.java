package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PermissionGrantDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PermissionSummaryDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.GroupeRoleUtilisateur;
import com.rsoft.hurmanagement.hurmasterdata.entity.RessourceUi;
import com.rsoft.hurmanagement.hurmasterdata.entity.RolePermission;
import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import com.rsoft.hurmanagement.hurmasterdata.repository.GroupeRoleUtilisateurRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RessourceUiRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RolePermissionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final UtilisateurRepository utilisateurRepository;
    private final GroupeRoleUtilisateurRepository groupeRoleUtilisateurRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RessourceUiRepository ressourceUiRepository;

    @Transactional(readOnly = true)
    public PermissionSummaryDTO getPermissionsForUser(String identifiant) {
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiantOrEmail(identifiant, identifiant)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "utilisateur.error.notFound"));

        List<GroupeRoleUtilisateur> groupes = groupeRoleUtilisateurRepository
                .findByUtilisateurIdOrderByIdAsc(utilisateur.getId());

        boolean allAccess = groupes.stream()
                .anyMatch(g -> "Y".equalsIgnoreCase(g.getGroupe().getAllAccess())
                        && "Y".equalsIgnoreCase(g.getGroupe().getActif()));

        if (allAccess) {
            return new PermissionSummaryDTO(true, List.of());
        }

        List<RolePermission> permissions = rolePermissionRepository.findActiveForUser(identifiant);
        if (permissions.isEmpty()) {
            return new PermissionSummaryDTO(false, List.of());
        }

        List<RessourceUi> ressources = ressourceUiRepository.findByActif("Y");
        Map<Long, RessourceUi> ressourceMap = new HashMap<>();
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (RessourceUi ressource : ressources) {
            ressourceMap.put(ressource.getId(), ressource);
            if (ressource.getParent() != null) {
                childrenMap.computeIfAbsent(ressource.getParent().getId(), k -> new ArrayList<>())
                        .add(ressource.getId());
            }
        }

        Map<String, RolePermission.Effet> effectMap = new HashMap<>();
        for (RolePermission permission : permissions) {
            Long ressourceId = permission.getRessource().getId();
            Set<Long> targets = Boolean.TRUE.equals(permission.getHeritageDescendant())
                    ? collectDescendants(ressourceId, childrenMap)
                    : Set.of(ressourceId);

            for (Long targetId : targets) {
                if (!ressourceMap.containsKey(targetId)) {
                    continue;
                }
                String key = targetId + ":" + permission.getAction().getId();
                if (permission.getEffet() == RolePermission.Effet.DENY) {
                    effectMap.put(key, RolePermission.Effet.DENY);
                } else if (!effectMap.containsKey(key)) {
                    effectMap.put(key, RolePermission.Effet.ALLOW);
                }
            }
        }

        List<PermissionGrantDTO> grants = new ArrayList<>();
        for (Map.Entry<String, RolePermission.Effet> entry : effectMap.entrySet()) {
            if (entry.getValue() != RolePermission.Effet.ALLOW) {
                continue;
            }
            String[] parts = entry.getKey().split(":");
            Long ressourceId = Long.parseLong(parts[0]);
            Long actionId = Long.parseLong(parts[1]);
            RessourceUi ressource = ressourceMap.get(ressourceId);
            if (ressource == null) {
                continue;
            }
            String actionCode = permissions.stream()
                    .filter(p -> p.getAction().getId().equals(actionId))
                    .map(p -> p.getAction().getCodeAction())
                    .findFirst()
                    .orElse(null);
            if (actionCode != null) {
                grants.add(new PermissionGrantDTO(ressource.getCodeResource(), actionCode));
            }
        }

        return new PermissionSummaryDTO(false, grants);
    }

    private Set<Long> collectDescendants(Long rootId, Map<Long, List<Long>> childrenMap) {
        Set<Long> collected = new HashSet<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(rootId);
        while (!stack.isEmpty()) {
            Long current = stack.pop();
            if (collected.add(current)) {
                for (Long child : childrenMap.getOrDefault(current, List.of())) {
                    stack.push(child);
                }
            }
        }
        return collected;
    }
}
