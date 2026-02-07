package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.JourCongeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.JourCongeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.JourCongeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.JourCongeService;
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
@RequestMapping("/api/jour-conges")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class JourCongeController {
    
    private final JourCongeService service;
    
    @GetMapping
    public ResponseEntity<Page<JourCongeDTO>> findAll(
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
        Page<JourCongeDTO> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<JourCongeDTO> findById(@PathVariable Long id) {
        JourCongeDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<JourCongeDTO> create(
            @Valid @RequestBody JourCongeCreateDTO dto,
            HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        if (username == null || username.trim().isEmpty()) {
            username = "system";
        }
        
        JourCongeDTO created = service.create(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<JourCongeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody JourCongeUpdateDTO dto,
            HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        if (username == null || username.trim().isEmpty()) {
            username = "system";
        }
        
        JourCongeDTO updated = service.update(id, dto, username);
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
