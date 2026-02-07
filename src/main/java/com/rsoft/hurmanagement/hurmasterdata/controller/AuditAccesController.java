package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.AuditAccesDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.AuditAccesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/audit-acces")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuditAccesController {
    
    private final AuditAccesService service;
    
    @GetMapping
    public ResponseEntity<Page<AuditAccesDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFin,
            @RequestParam(required = false) String utilisateur,
            @RequestParam(required = false) String typeEvenement,
            @RequestParam(required = false) String resultat,
            @RequestParam(required = false) Long entrepriseId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateEvenement"));
        
        if (dateDebut != null && dateFin != null) {
            return ResponseEntity.ok(service.findByDateRange(dateDebut, dateFin, pageable));
        } else if (utilisateur != null) {
            return ResponseEntity.ok(service.findByUtilisateur(utilisateur, pageable));
        } else if (typeEvenement != null) {
            return ResponseEntity.ok(service.findByTypeEvenement(typeEvenement, pageable));
        } else if (resultat != null) {
            return ResponseEntity.ok(service.findByResultat(resultat, pageable));
        } else if (entrepriseId != null) {
            return ResponseEntity.ok(service.findByEntrepriseId(entrepriseId, pageable));
        }
        
        return ResponseEntity.ok(service.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AuditAccesDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
