package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.RefFormuleTokenCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefFormuleTokenDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefFormuleTokenUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.RefFormuleTokenService;
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
@RequestMapping("/api/ref-formule-tokens")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class RefFormuleTokenController {
    
    private final RefFormuleTokenService service;
    
    @GetMapping
    public ResponseEntity<Page<RefFormuleTokenDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "codeElement") String sortBy,
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
    
    @GetMapping("/{codeElement}")
    public ResponseEntity<RefFormuleTokenDTO> findById(@PathVariable String codeElement) {
        return ResponseEntity.ok(service.findById(codeElement));
    }
    
    @PostMapping
    public ResponseEntity<RefFormuleTokenDTO> create(
            @Valid @RequestBody RefFormuleTokenCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{codeElement}")
    public ResponseEntity<RefFormuleTokenDTO> update(
            @PathVariable String codeElement,
            @Valid @RequestBody RefFormuleTokenUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (!dto.getCodeElement().equals(codeElement)) {
            dto.setCodeElement(codeElement);
        }
        return ResponseEntity.ok(service.update(codeElement, dto, username));
    }
    
    @DeleteMapping("/{codeElement}")
    public ResponseEntity<Void> delete(
            @PathVariable String codeElement,
            @RequestParam Integer rowscn) {
        service.delete(codeElement, rowscn);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<RefFormuleTokenDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }
}
