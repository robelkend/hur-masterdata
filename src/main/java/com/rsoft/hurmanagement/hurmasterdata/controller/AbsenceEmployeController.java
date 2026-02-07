package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.service.AbsenceEmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/absence-employes")
@RequiredArgsConstructor
public class AbsenceEmployeController {

    private final AbsenceEmployeService service;

    @GetMapping
    public ResponseEntity<Page<AbsenceEmployeDTO>> findAll(
            @RequestParam String dateDebut,
            @RequestParam String dateFin,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String typeEvenement,
            @RequestParam(required = false) Long entrepriseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.findByFilters(
                dateDebut, dateFin, employeId, statut, typeEvenement, entrepriseId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbsenceEmployeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<AbsenceEmployeDTO> create(
            @Valid @RequestBody AbsenceEmployeCreateDTO dto,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return ResponseEntity.ok(service.create(dto, usernameOrDefault(username)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AbsenceEmployeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AbsenceEmployeUpdateDTO dto,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return ResponseEntity.ok(service.update(id, dto, usernameOrDefault(username)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate")
    public ResponseEntity<AbsenceGenerationResultDTO> generate(
            @Valid @RequestBody AbsenceGenerationRequestDTO dto,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return ResponseEntity.ok(service.generateAbsences(dto, usernameOrDefault(username)));
    }

    @PostMapping("/validate")
    public ResponseEntity<AbsenceValidationResultDTO> validate(
            @Valid @RequestBody AbsenceGenerationRequestDTO dto,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return ResponseEntity.ok(service.validateAbsences(dto, usernameOrDefault(username)));
    }

    @PostMapping("/cancel")
    public ResponseEntity<AbsenceAnnulationResultDTO> cancel(
            @Valid @RequestBody AbsenceGenerationRequestDTO dto,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return ResponseEntity.ok(service.cancelAbsences(dto, usernameOrDefault(username)));
    }

    private String usernameOrDefault(String username) {
        return username != null && !username.trim().isEmpty() ? username : "system";
    }
}
