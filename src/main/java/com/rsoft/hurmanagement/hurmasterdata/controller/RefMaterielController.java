package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.RefMaterielCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefMaterielDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefMaterielUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.RefMaterielService;
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
@RequestMapping("/api/ref-materiels")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class RefMaterielController {

    private final RefMaterielService service;

    @GetMapping
    public ResponseEntity<Page<RefMaterielDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<List<RefMaterielDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefMaterielDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<RefMaterielDTO> create(
            @Valid @RequestBody RefMaterielCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RefMaterielDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RefMaterielUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (!id.equals(dto.getId())) {
            dto.setId(id);
        }
        return ResponseEntity.ok(service.update(id, dto, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
}
