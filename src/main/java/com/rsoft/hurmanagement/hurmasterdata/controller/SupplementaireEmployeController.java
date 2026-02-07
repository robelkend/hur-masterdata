package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireGenerationRequestDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireGenerationResultDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireValidationResultDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.SupplementaireEmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/supplementaire-employes")
@RequiredArgsConstructor
public class SupplementaireEmployeController {
    
    private final SupplementaireEmployeService service;
    
    @GetMapping
    public ResponseEntity<Page<SupplementaireEmployeDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = true) String dateDebut,
            @RequestParam(required = true) String dateFin,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) Long entrepriseId,
            Principal principal) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<SupplementaireEmployeDTO> result = service.findByFilters(
            dateDebut, dateFin, employeId, statut, entrepriseId, pageable);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/employe/{employeId}")
    public ResponseEntity<Page<SupplementaireEmployeDTO>> findByEmployeId(
            @PathVariable Long employeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateJour") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findByEmployeId(employeId, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SupplementaireEmployeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<SupplementaireEmployeDTO> create(
            @Valid @RequestBody SupplementaireEmployeCreateDTO dto,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, principal.getName()));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SupplementaireEmployeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplementaireEmployeUpdateDTO dto,
            Principal principal) {
        if (!dto.getId().equals(id)) {
            dto.setId(id);
        }
        return ResponseEntity.ok(service.update(id, dto, principal.getName()));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate")
    public ResponseEntity<SupplementaireGenerationResultDTO> generate(
            @Valid @RequestBody SupplementaireGenerationRequestDTO request,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.generateSupplementaires(request, username));
    }

    @PostMapping("/validate")
    public ResponseEntity<SupplementaireValidationResultDTO> validate(
            @Valid @RequestBody SupplementaireGenerationRequestDTO request,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.validateSupplementaires(request, username));
    }
}
