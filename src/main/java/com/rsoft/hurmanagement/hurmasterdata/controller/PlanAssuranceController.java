package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.PlanAssuranceCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PlanAssuranceDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PlanAssuranceUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.PlanAssuranceService;
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
@RequestMapping("/api/plan-assurances")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PlanAssuranceController {
    
    private final PlanAssuranceService service;
    
    @GetMapping
    public ResponseEntity<Page<PlanAssuranceDTO>> findAll(
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
        Page<PlanAssuranceDTO> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PlanAssuranceDTO> findById(@PathVariable Long id) {
        PlanAssuranceDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<PlanAssuranceDTO> create(
            @Valid @RequestBody PlanAssuranceCreateDTO dto,
            HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        if (username == null || username.trim().isEmpty()) {
            username = "system";
        }
        
        PlanAssuranceDTO created = service.create(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PlanAssuranceDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PlanAssuranceUpdateDTO dto,
            HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        if (username == null || username.trim().isEmpty()) {
            username = "system";
        }
        
        PlanAssuranceDTO updated = service.update(id, dto, username);
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
