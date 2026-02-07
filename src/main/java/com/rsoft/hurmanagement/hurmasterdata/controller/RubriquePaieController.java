package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePaieCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePaieDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePaieUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.RubriquePaieService;
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
@RequestMapping("/api/rubrique-paies")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class RubriquePaieController {
    
    private final RubriquePaieService service;
    
    @GetMapping
    public ResponseEntity<Page<RubriquePaieDTO>> findAll(
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
    
    @GetMapping("/imposable")
    public ResponseEntity<List<RubriquePaieDTO>> findAllImposable() {
        return ResponseEntity.ok(service.findAllImposable());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RubriquePaieDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<RubriquePaieDTO> create(
            @Valid @RequestBody RubriquePaieCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RubriquePaieDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RubriquePaieUpdateDTO dto,
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
