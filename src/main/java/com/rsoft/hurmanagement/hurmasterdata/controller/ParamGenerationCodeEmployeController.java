package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.ParamGenerationCodeEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ParamGenerationCodeEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ParamGenerationCodeEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.ParamGenerationCodeEmployeService;
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
@RequestMapping("/api/param-generation-code-employes")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class ParamGenerationCodeEmployeController {
    
    private final ParamGenerationCodeEmployeService service;
    
    @GetMapping
    public ResponseEntity<Page<ParamGenerationCodeEmployeDTO>> findAll(
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
    public ResponseEntity<ParamGenerationCodeEmployeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<ParamGenerationCodeEmployeDTO> create(
            @Valid @RequestBody ParamGenerationCodeEmployeCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ParamGenerationCodeEmployeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ParamGenerationCodeEmployeUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (!dto.getId().equals(id)) {
            dto.setId(id);
        }
        return ResponseEntity.ok(service.update(dto, username));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
}
