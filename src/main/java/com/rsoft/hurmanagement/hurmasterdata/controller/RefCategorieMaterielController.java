package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.RefCategorieMaterielCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefCategorieMaterielDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefCategorieMaterielUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.RefCategorieMaterielService;
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
@RequestMapping("/api/ref-categorie-materiels")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class RefCategorieMaterielController {

    private final RefCategorieMaterielService service;

    @GetMapping
    public ResponseEntity<Page<RefCategorieMaterielDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "codeCategorie") String sortBy,
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

    @GetMapping("/dropdown")
    public ResponseEntity<List<RefCategorieMaterielDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }

    @GetMapping("/{codeCategorie}")
    public ResponseEntity<RefCategorieMaterielDTO> findById(@PathVariable String codeCategorie) {
        return ResponseEntity.ok(service.findById(codeCategorie));
    }

    @PostMapping
    public ResponseEntity<RefCategorieMaterielDTO> create(
            @Valid @RequestBody RefCategorieMaterielCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }

    @PutMapping("/{codeCategorie}")
    public ResponseEntity<RefCategorieMaterielDTO> update(
            @PathVariable String codeCategorie,
            @Valid @RequestBody RefCategorieMaterielUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.update(codeCategorie, dto, username));
    }

    @DeleteMapping("/{codeCategorie}")
    public ResponseEntity<Void> delete(@PathVariable String codeCategorie) {
        service.delete(codeCategorie);
        return ResponseEntity.noContent().build();
    }
}
