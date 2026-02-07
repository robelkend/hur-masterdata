package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.TypeUniteOrganisationnelleCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeUniteOrganisationnelleDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypeUniteOrganisationnelleUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.TypeUniteOrganisationnelleService;
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
@RequestMapping("/api/type-unite-organisationnelles")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TypeUniteOrganisationnelleController {
    
    private final TypeUniteOrganisationnelleService service;
    
    @GetMapping
    public ResponseEntity<Page<TypeUniteOrganisationnelleDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        // Convert String to Sort.Direction
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.ASC; // Default to ASC if invalid
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findAll(pageable));
    }
    
    @GetMapping("/all")
    public ResponseEntity<java.util.List<TypeUniteOrganisationnelleDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TypeUniteOrganisationnelleDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<TypeUniteOrganisationnelleDTO> create(
            @Valid @RequestBody TypeUniteOrganisationnelleCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TypeUniteOrganisationnelleDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TypeUniteOrganisationnelleUpdateDTO dto,
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
