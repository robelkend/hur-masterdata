package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePrestationCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePrestationDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePrestationUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.RubriquePrestationService;
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
@RequestMapping("/api/rubrique-prestations")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class RubriquePrestationController {
    
    private final RubriquePrestationService service;
    
    @GetMapping
    public ResponseEntity<Page<RubriquePrestationDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.ASC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RubriquePrestationDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<RubriquePrestationDTO> create(
            @Valid @RequestBody RubriquePrestationCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RubriquePrestationDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RubriquePrestationUpdateDTO dto,
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
