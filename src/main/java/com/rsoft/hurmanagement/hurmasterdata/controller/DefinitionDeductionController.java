package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.DefinitionDeductionCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.DefinitionDeductionDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.DefinitionDeductionUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.DefinitionDeductionCloneDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.DefinitionDeductionService;
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
@RequestMapping("/api/definition-deductions")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DefinitionDeductionController {
    
    private final DefinitionDeductionService service;
    
    @GetMapping
    public ResponseEntity<Page<DefinitionDeductionDTO>> findAll(
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
    
    @GetMapping("/{id}")
    public ResponseEntity<DefinitionDeductionDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<DefinitionDeductionDTO> create(
            @Valid @RequestBody DefinitionDeductionCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DefinitionDeductionDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DefinitionDeductionUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.update(id, dto, username));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<DefinitionDeductionDTO> cloneDefinitionDeduction(
            @PathVariable Long id,
            @Valid @RequestBody DefinitionDeductionCloneDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.cloneDefinitionDeduction(id, dto.getCodeDeduction(), username));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<DefinitionDeductionDTO>> findAllForDropdown() {
        List<DefinitionDeductionDTO> result = service.findAllForDropdown();
        return ResponseEntity.ok(result);
    }
}
