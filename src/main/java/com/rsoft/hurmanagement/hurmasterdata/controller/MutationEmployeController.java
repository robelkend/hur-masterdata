package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.EmploiEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.MutationEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.MutationEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.MutationEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.service.MutationEmployeService;
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
import java.util.List;

@RestController
@RequestMapping("/api/mutation-employes")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class MutationEmployeController {
    
    private final MutationEmployeService service;
    
    @GetMapping
    public ResponseEntity<Page<MutationEmployeDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = true) LocalDate dateDebut,
            @RequestParam(required = true) LocalDate dateFin,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String typeMutation,
            @RequestParam(required = false) String statut) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.ASC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findAllWithFilters(employeId, typeMutation, statut, dateDebut, dateFin, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MutationEmployeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<MutationEmployeDTO> create(
            @Valid @RequestBody MutationEmployeCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MutationEmployeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody MutationEmployeUpdateDTO dto,
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
    
    @PostMapping("/{id}/appliquer")
    public ResponseEntity<MutationEmployeDTO> appliquer(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.appliquer(id, username));
    }
    
    @PostMapping("/{id}/annuler")
    public ResponseEntity<MutationEmployeDTO> annuler(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.annuler(id, username));
    }
    
    @GetMapping("/emplois-disponibles")
    public ResponseEntity<List<EmploiEmployeDTO>> getEmploisDisponibles(
            @RequestParam Long employeId,
            @RequestParam(required = false) String typeMutation) {
        return ResponseEntity.ok(service.getEmploisDisponibles(employeId, typeMutation));
    }
}
