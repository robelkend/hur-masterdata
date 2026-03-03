package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.AuditAccesDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.AuditAccesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import com.rsoft.hurmanagement.hurmasterdata.entity.AuditAcces;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

@RestController
@RequestMapping("/api/audit-acces")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuditAccesController {
    
    private final AuditAccesService service;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "dateEvenement",
            "utilisateur",
            "typeEvenement",
            "resultat",
            "resourceCode",
            "actionCode",
            "ipAddress",
            "dureeMs",
            "resourceType",
            "cibleType",
            "cibleId",
            "sessionId"
    );
    
    @GetMapping
    public ResponseEntity<Page<AuditAccesDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFin,
            @RequestParam(required = false) String utilisateur,
            @RequestParam(required = false) String typeEvenement,
            @RequestParam(required = false) String resultat,
            @RequestParam(required = false) Long entrepriseId) {
        
        String safeSortBy = (sortBy != null && ALLOWED_SORT_FIELDS.contains(sortBy)) ? sortBy : "dateEvenement";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, safeSortBy));
        // #region agent log
        try { Files.writeString(Path.of("c:\\My Datas\\Robelkend\\hurmagement\\dev\\rhp\\hurmanagementnew\\.cursor\\debug.log"),
                "{\"location\":\"AuditAccesController.java:40\",\"message\":\"findAll params\",\"data\":{\"page\":"+page+",\"size\":"+size+",\"hasDateDebut\":"+(dateDebut!=null)+",\"hasDateFin\":"+(dateFin!=null)+",\"hasUtilisateur\":"+(utilisateur!=null && !utilisateur.trim().isEmpty())+",\"typeEvenement\":\""+(typeEvenement!=null?typeEvenement:"")+"\",\"resultat\":\""+(resultat!=null?resultat:"")+"\",\"hasEntrepriseId\":"+(entrepriseId!=null)+"},\"timestamp\":"+System.currentTimeMillis()+",\"runId\":\"run1\",\"hypothesisId\":\"H1\"}\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);} catch (Exception ignored) {}
        // #endregion agent log

        String utilisateurFilter = utilisateur != null && !utilisateur.trim().isEmpty() ? utilisateur.trim() : null;
        AuditAcces.TypeEvenement typeEvenementEnum = parseTypeEvenement(typeEvenement);
        AuditAcces.Resultat resultatEnum = parseResultat(resultat);
        // #region agent log
        try { Files.writeString(Path.of("c:\\My Datas\\Robelkend\\hurmagement\\dev\\rhp\\hurmanagementnew\\.cursor\\debug.log"),
                "{\"location\":\"AuditAccesController.java:47\",\"message\":\"findAll parsed\",\"data\":{\"utilisateurProvided\":"+(utilisateurFilter!=null)+",\"typeEvenement\":\""+(typeEvenementEnum!=null?typeEvenementEnum.name():"")+"\",\"resultat\":\""+(resultatEnum!=null?resultatEnum.name():"")+"\"},\"timestamp\":"+System.currentTimeMillis()+",\"runId\":\"run1\",\"hypothesisId\":\"H2\"}\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);} catch (Exception ignored) {}
        // #endregion agent log

        if (dateDebut == null && dateFin == null && utilisateurFilter == null && typeEvenementEnum == null && resultatEnum == null && entrepriseId == null) {
            try {
                return ResponseEntity.ok(service.findAll(pageable));
            } catch (Exception ex) {
                // #region agent log
                try { Files.writeString(Path.of("c:\\My Datas\\Robelkend\\hurmagement\\dev\\rhp\\hurmanagementnew\\.cursor\\debug.log"),
                        "{\"location\":\"AuditAccesController.java:50\",\"message\":\"findAll exception\",\"data\":{\"error\":\""+ex.getClass().getSimpleName()+"\"},\"timestamp\":"+System.currentTimeMillis()+",\"runId\":\"run1\",\"hypothesisId\":\"H1\"}\n",
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);} catch (Exception ignored) {}
                // #endregion agent log
                throw ex;
            }
        }

        try {
            return ResponseEntity.ok(service.findByFilters(dateDebut, dateFin, utilisateurFilter, typeEvenementEnum, resultatEnum, entrepriseId, pageable));
        } catch (Exception ex) {
            // #region agent log
            try { Files.writeString(Path.of("c:\\My Datas\\Robelkend\\hurmagement\\dev\\rhp\\hurmanagementnew\\.cursor\\debug.log"),
                    "{\"location\":\"AuditAccesController.java:58\",\"message\":\"findByFilters exception\",\"data\":{\"error\":\""+ex.getClass().getSimpleName()+"\"},\"timestamp\":"+System.currentTimeMillis()+",\"runId\":\"run1\",\"hypothesisId\":\"H2\"}\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);} catch (Exception ignored) {}
            // #endregion agent log
            throw ex;
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AuditAccesDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<AuditAccesDTO> create(@RequestBody AuditAccesDTO dto) {
        if (dto.getTypeEvenement() == null || dto.getTypeEvenement().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "typeEvenement is required");
        }
        try {
            AuditAcces.TypeEvenement.valueOf(dto.getTypeEvenement());
            if (dto.getResultat() != null && !dto.getResultat().isBlank()) {
                AuditAcces.Resultat.valueOf(dto.getResultat());
            }
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid audit evenement/resultat");
        }
        return ResponseEntity.ok(service.createFromDto(dto));
    }

    private AuditAcces.TypeEvenement parseTypeEvenement(String typeEvenement) {
        if (typeEvenement == null || typeEvenement.isBlank()) {
            return null;
        }
        try {
            return AuditAcces.TypeEvenement.valueOf(typeEvenement);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid typeEvenement");
        }
    }

    private AuditAcces.Resultat parseResultat(String resultat) {
        if (resultat == null || resultat.isBlank()) {
            return null;
        }
        try {
            return AuditAcces.Resultat.valueOf(resultat);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid resultat");
        }
    }
}
