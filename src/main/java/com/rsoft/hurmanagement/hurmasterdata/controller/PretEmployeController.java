package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.PretEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PretEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PretEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.PretEmployeService;
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
@RequestMapping("/api/pret-employes")
@RequiredArgsConstructor
public class PretEmployeController {
    
    private final PretEmployeService service;
    
    @GetMapping
    public ResponseEntity<Page<PretEmployeDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = true) LocalDate dateDebut,
            @RequestParam(required = true) LocalDate dateFin,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String avance) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findAllWithFilters(employeId, statut, avance, dateDebut, dateFin, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PretEmployeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<PretEmployeDTO> create(
            @Valid @RequestBody PretEmployeCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PretEmployeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PretEmployeUpdateDTO dto,
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
    
    @PostMapping("/{id}/activer")
    public ResponseEntity<PretEmployeDTO> activer(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.activer(id, username));
    }
    
    @PostMapping("/{id}/suspendre")
    public ResponseEntity<PretEmployeDTO> suspendre(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.suspendre(id, username));
    }
    
    @PostMapping("/{id}/reprendre")
    public ResponseEntity<PretEmployeDTO> reprendre(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.reprendre(id, username));
    }
    
    @PostMapping("/{id}/annuler")
    public ResponseEntity<PretEmployeDTO> annuler(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.annuler(id, username));
    }
}
