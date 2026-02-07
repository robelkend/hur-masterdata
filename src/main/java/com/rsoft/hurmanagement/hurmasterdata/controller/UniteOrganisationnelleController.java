package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.UniteOrganisationnelleCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.UniteOrganisationnelleDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.UniteOrganisationnelleUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.UniteOrganisationnelleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unite-organisationnelles")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UniteOrganisationnelleController {
    
    private final UniteOrganisationnelleService service;
    
    @GetMapping
    public ResponseEntity<Page<UniteOrganisationnelleDTO>> findAll(
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
    
    @GetMapping("/dropdown")
    public ResponseEntity<List<UniteOrganisationnelleDTO>> findAllForDropdown(
            @RequestParam(required = false) Long excludeId) {
        return ResponseEntity.ok(service.findAllForDropdown(excludeId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UniteOrganisationnelleDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<UniteOrganisationnelleDTO> create(
            @Valid @RequestBody UniteOrganisationnelleCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UniteOrganisationnelleDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UniteOrganisationnelleUpdateDTO dto,
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
