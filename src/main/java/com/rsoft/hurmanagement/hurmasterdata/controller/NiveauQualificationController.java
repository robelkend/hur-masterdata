package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.NiveauQualificationCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.NiveauQualificationDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.NiveauQualificationUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.NiveauQualificationService;
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
@RequestMapping("/api/niveau-qualifications")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class NiveauQualificationController {
    
    private final NiveauQualificationService service;
    
    @GetMapping
    public ResponseEntity<Page<NiveauQualificationDTO>> findAll(
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
    
    @GetMapping("/all")
    public ResponseEntity<java.util.List<NiveauQualificationDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NiveauQualificationDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @GetMapping("/by-code/{codeNiveau}")
    public ResponseEntity<NiveauQualificationDTO> findByCodeNiveau(@PathVariable String codeNiveau) {
        return ResponseEntity.ok(service.findByCodeNiveau(codeNiveau));
    }
    
    @PostMapping
    public ResponseEntity<NiveauQualificationDTO> create(
            @Valid @RequestBody NiveauQualificationCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<NiveauQualificationDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody NiveauQualificationUpdateDTO dto,
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
