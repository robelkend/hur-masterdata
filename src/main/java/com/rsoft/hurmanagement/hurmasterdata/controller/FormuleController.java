package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.FormuleCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.FormuleDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.FormuleTestRequestDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.FormuleTestResultDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.FormuleUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.FormuleService;
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
@RequestMapping("/api/formules")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class FormuleController {
    
    private final FormuleService service;
    
    @GetMapping
    public ResponseEntity<Page<FormuleDTO>> findAll(
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
    public ResponseEntity<FormuleDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<FormuleDTO> create(
            @Valid @RequestBody FormuleCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<FormuleDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FormuleUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (!dto.getId().equals(id)) {
            dto.setId(id);
        }
        return ResponseEntity.ok(service.update(dto, username));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<FormuleDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }

    @PostMapping("/test")
    public ResponseEntity<FormuleTestResultDTO> testExpression(
            @Valid @RequestBody FormuleTestRequestDTO dto) {
        return ResponseEntity.ok(service.testExpression(dto));
    }
}
