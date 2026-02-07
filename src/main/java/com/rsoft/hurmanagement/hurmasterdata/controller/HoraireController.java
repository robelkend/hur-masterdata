package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.HoraireCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.HoraireDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.HoraireUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.HoraireCloneDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.HoraireService;
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
@RequestMapping("/api/horaires")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class HoraireController {
    
    private final HoraireService service;
    
    @GetMapping
    public ResponseEntity<Page<HoraireDTO>> findAll(
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
    public ResponseEntity<HoraireDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<HoraireDTO> create(
            @Valid @RequestBody HoraireCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<HoraireDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody HoraireUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.update(id, dto, username));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<HoraireDTO> cloneHoraire(
            @PathVariable Long id,
            @Valid @RequestBody HoraireCloneDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.cloneHoraire(id, dto.getCodeHoraire(), username));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
}
