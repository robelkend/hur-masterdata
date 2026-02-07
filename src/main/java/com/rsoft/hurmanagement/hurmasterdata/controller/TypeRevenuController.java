package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.TypeRevenuCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeRevenuDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeRevenuUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.TypeRevenuService;
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
@RequestMapping("/api/type-revenus")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TypeRevenuController {
    
    private final TypeRevenuService service;
    
    @GetMapping
    public ResponseEntity<Page<TypeRevenuDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) Long rubriquePaieId,
            @RequestParam(required = false) String actif) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.ASC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        // If filters are provided, use filtered search
        if (entrepriseId != null || rubriquePaieId != null || (actif != null && !actif.trim().isEmpty())) {
            return ResponseEntity.ok(service.findAllWithFilters(entrepriseId, rubriquePaieId, actif, pageable));
        }
        
        return ResponseEntity.ok(service.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TypeRevenuDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<TypeRevenuDTO> create(
            @Valid @RequestBody TypeRevenuCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TypeRevenuDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TypeRevenuUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
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
