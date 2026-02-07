package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.CongeEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.CongeEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.CongeEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.CongeEmployeFinalizeService;
import com.rsoft.hurmanagement.hurmasterdata.service.CongeEmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/conge-employe")
@RequiredArgsConstructor
public class CongeEmployeController {

    private final CongeEmployeService service;
    private final CongeEmployeFinalizeService finalizeService;

    @GetMapping
    public ResponseEntity<Page<CongeEmployeDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = true) LocalDate dateDebut,
            @RequestParam(required = true) LocalDate dateFin,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) Long typeCongeId,
            @RequestParam(required = false) String statut) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findAllWithFilters(entrepriseId, employeId, typeCongeId, statut, dateDebut, dateFin, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CongeEmployeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<CongeEmployeDTO> create(
            @Valid @RequestBody CongeEmployeCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CongeEmployeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CongeEmployeUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (!dto.getId().equals(id)) {
            dto.setId(id);
        }
        return ResponseEntity.ok(service.update(id, dto, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/soumettre")
    public ResponseEntity<CongeEmployeDTO> soumettre(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.soumettre(id, username));
    }

    @PostMapping("/{id}/approuver")
    public ResponseEntity<CongeEmployeDTO> approuver(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.approuver(id, commentaire, username));
    }

    @PostMapping("/{id}/rejeter")
    public ResponseEntity<CongeEmployeDTO> rejeter(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.rejeter(id, commentaire, username));
    }

    @PostMapping("/{id}/demarrer")
    public ResponseEntity<CongeEmployeDTO> demarrer(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.demarrer(id, username));
    }

    @PostMapping("/{id}/annuler")
    public ResponseEntity<CongeEmployeDTO> annuler(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.annuler(id, username));
    }

    @PostMapping("/{id}/finaliser")
    public ResponseEntity<CongeEmployeDTO> finaliser(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.findById(finalizeService.finalizeConge(id, username, false).getId()));
    }
}
