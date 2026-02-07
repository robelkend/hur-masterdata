package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.service.RegimePaieService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regime-paies")
@RequiredArgsConstructor
public class RegimePaieController {
    
    private final RegimePaieService service;
    
    @GetMapping
    public ResponseEntity<Page<RegimePaieDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<RegimePaieDTO> result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/dropdown")
    public ResponseEntity<List<RegimePaieDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RegimePaieDTO> findById(@PathVariable Long id) {
        RegimePaieDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<RegimePaieDTO> create(
            @RequestBody RegimePaieCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        RegimePaieDTO created = service.create(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RegimePaieDTO> update(
            @PathVariable Long id,
            @RequestBody RegimePaieUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        RegimePaieDTO updated = service.update(id, dto, username);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<RegimePaieDTO> cloneRegimePaie(
            @PathVariable Long id,
            @Valid @RequestBody RegimePaieCloneDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        RegimePaieDTO created = service.cloneRegimePaie(id, dto.getCodeRegimePaie(), username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
}
