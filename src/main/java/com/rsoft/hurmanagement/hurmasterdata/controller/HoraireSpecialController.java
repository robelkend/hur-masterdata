package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.HoraireSpecialCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.HoraireSpecialDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.HoraireSpecialUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.HoraireSpecialService;
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
@RequestMapping("/api/horaire-specials")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class HoraireSpecialController {
    
    private final HoraireSpecialService service;
    
    @GetMapping
    public ResponseEntity<Page<HoraireSpecialDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String dateDebutFrom,
            @RequestParam(required = false) String dateDebutTo) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.ASC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findAll(
                pageable,
                employeId,
                parseDate(dateDebutFrom),
                parseDate(dateDebutTo)));
    }
    
    @GetMapping("/employe/{employeId}")
    public ResponseEntity<Page<HoraireSpecialDTO>> findByEmployeId(
            @PathVariable Long employeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateDebut") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findByEmployeId(employeId, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<HoraireSpecialDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<HoraireSpecialDTO> create(
            @Valid @RequestBody HoraireSpecialCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<HoraireSpecialDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody HoraireSpecialUpdateDTO dto,
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

    @PostMapping("/duplicate")
    public ResponseEntity<Integer> duplicate(
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String dateDebutFrom,
            @RequestParam(required = false) String dateDebutTo,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        int count = service.dupliquerParCritere(
                employeId,
                parseDate(dateDebutFrom),
                parseDate(dateDebutTo),
                username);
        return ResponseEntity.ok(count);
    }

    private java.time.LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return java.time.LocalDate.parse(value);
    }
}
