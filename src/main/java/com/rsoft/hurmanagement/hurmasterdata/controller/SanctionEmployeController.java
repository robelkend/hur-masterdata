package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.SanctionEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SanctionEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SanctionEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.SanctionEmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sanction-employes")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class SanctionEmployeController {
    
    private final SanctionEmployeService service;
    
    @GetMapping
    public ResponseEntity<Page<SanctionEmployeDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) Long entrepriseId) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<SanctionEmployeDTO> result;
        if (employeId != null || statut != null || entrepriseId != null) {
            result = service.findByFilters(employeId, statut, entrepriseId, pageable);
        } else {
            result = service.findAll(pageable);
        }
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/employe/{employeId}")
    public ResponseEntity<Page<SanctionEmployeDTO>> findByEmployeId(
            @PathVariable Long employeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateSanction") String sortBy,
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
    public ResponseEntity<SanctionEmployeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<SanctionEmployeDTO> create(
            @Valid @RequestBody SanctionEmployeCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SanctionEmployeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SanctionEmployeUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (!dto.getId().equals(id)) {
            dto.setId(id);
        }
        return ResponseEntity.ok(service.update(id, dto, username));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
}
