package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.ReferencePayrollCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ReferencePayrollDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ReferencePayrollUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.ReferencePayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/reference-payrolls")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReferencePayrollController {
    
    private final ReferencePayrollService service;
    
    @GetMapping
    public ResponseEntity<Page<ReferencePayrollDTO>> findAll(
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
        Page<ReferencePayrollDTO> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ReferencePayrollDTO> findById(@PathVariable Long id) {
        ReferencePayrollDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<ReferencePayrollDTO> create(
            @Valid @RequestBody ReferencePayrollCreateDTO dto,
            HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        if (username == null || username.trim().isEmpty()) {
            username = "system";
        }
        
        ReferencePayrollDTO created = service.create(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ReferencePayrollDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ReferencePayrollUpdateDTO dto,
            HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        if (username == null || username.trim().isEmpty()) {
            username = "system";
        }
        
        ReferencePayrollDTO updated = service.update(id, dto, username);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
}
