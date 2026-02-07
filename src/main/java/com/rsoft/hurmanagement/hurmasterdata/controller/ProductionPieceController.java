package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.ProductionPieceCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ProductionPieceDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ProductionPieceUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.ProductionPieceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/production-pieces")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductionPieceController {
    private final ProductionPieceService service;

    @GetMapping("/search")
    public ResponseEntity<Page<ProductionPieceDTO>> findByFilters(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateJour") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findByFilters(dateDebut, dateFin, employeId, entrepriseId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductionPieceDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProductionPieceDTO> create(
            @Valid @RequestBody ProductionPieceCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductionPieceDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductionPieceUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.update(id, dto, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<Integer> validateByFilters(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) Long entrepriseId,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        int updated = service.validateByFilters(dateDebut, dateFin, employeId, entrepriseId, username);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Integer> cancelByFilters(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) Long entrepriseId,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        int updated = service.cancelByFilters(dateDebut, dateFin, employeId, entrepriseId, username);
        return ResponseEntity.ok(updated);
    }
}
