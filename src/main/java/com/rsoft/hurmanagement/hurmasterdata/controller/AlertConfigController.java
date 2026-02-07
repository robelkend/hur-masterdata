package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.AlertConfigCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AlertConfigDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.AlertConfigUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.AlertConfigService;
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
@RequestMapping("/api/alert-configs")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AlertConfigController {
    
    private final AlertConfigService service;
    
    @GetMapping
    public ResponseEntity<Page<AlertConfigDTO>> findAll(
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
    public ResponseEntity<AlertConfigDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @GetMapping("/code-message/{codeMessage}")
    public ResponseEntity<AlertConfigDTO> findByCodeMessage(@PathVariable String codeMessage) {
        return ResponseEntity.ok(service.findByCodeMessage(codeMessage));
    }
    
    @PostMapping
    public ResponseEntity<AlertConfigDTO> create(
            @Valid @RequestBody AlertConfigCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AlertConfigDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AlertConfigUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (!dto.getId().equals(id)) {
            dto.setId(id);
        }
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
