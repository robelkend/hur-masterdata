package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.AutreRevenuEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AutreRevenuEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AutreRevenuEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AutreRevenuValidationRangeRequestDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AutreRevenuValidationRangeResultDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.AutreRevenuEmployeService;
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
@RequestMapping("/api/autre-revenu-employe")
@RequiredArgsConstructor
public class AutreRevenuEmployeController {

    private final AutreRevenuEmployeService service;

    @GetMapping
    public ResponseEntity<Page<AutreRevenuEmployeDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = true) LocalDate dateDebut,
            @RequestParam(required = true) LocalDate dateFin,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) Long typeRevenuId,
            @RequestParam(required = false) String statut) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findAllWithFilters(entrepriseId, employeId, typeRevenuId, statut, dateDebut, dateFin, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AutreRevenuEmployeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<AutreRevenuEmployeDTO> create(
            @Valid @RequestBody AutreRevenuEmployeCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AutreRevenuEmployeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AutreRevenuEmployeUpdateDTO dto,
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

    @PostMapping("/{id}/valider")
    public ResponseEntity<AutreRevenuEmployeDTO> valider(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.valider(id, username));
    }

    @PostMapping("/{id}/rejeter")
    public ResponseEntity<AutreRevenuEmployeDTO> rejeter(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.rejeter(id, username));
    }

    @PostMapping("/{id}/annuler")
    public ResponseEntity<AutreRevenuEmployeDTO> annuler(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.annuler(id, username));
    }

    @PostMapping("/valider-par-plage")
    public ResponseEntity<AutreRevenuValidationRangeResultDTO> validerParPlage(
            @Valid @RequestBody AutreRevenuValidationRangeRequestDTO request,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.validerParPlage(request, username));
    }

    @PostMapping("/devalider-par-plage")
    public ResponseEntity<AutreRevenuValidationRangeResultDTO> devaliderParPlage(
            @Valid @RequestBody AutreRevenuValidationRangeRequestDTO request,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.devaliderParPlage(request, username));
    }
}
