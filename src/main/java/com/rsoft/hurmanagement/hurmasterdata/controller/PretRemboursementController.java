package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.PretRemboursementCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PretRemboursementDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PretRemboursementUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.PretRemboursementService;
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
@RequestMapping("/api/pret-remboursements")
@RequiredArgsConstructor
public class PretRemboursementController {
    
    private final PretRemboursementService service;
    
    @GetMapping("/pret/{pretEmployeId}")
    public ResponseEntity<List<PretRemboursementDTO>> findByPretEmployeId(@PathVariable Long pretEmployeId) {
        return ResponseEntity.ok(service.findByPretEmployeId(pretEmployeId));
    }
    
    @GetMapping("/pret/{pretEmployeId}/page")
    public ResponseEntity<Page<PretRemboursementDTO>> findByPretEmployeIdPage(
            @PathVariable Long pretEmployeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateRemboursement") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findByPretEmployeId(pretEmployeId, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PretRemboursementDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<PretRemboursementDTO> create(
            @Valid @RequestBody PretRemboursementCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PretRemboursementDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PretRemboursementUpdateDTO dto,
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
