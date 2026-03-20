package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.EmployeMaterielCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.EmployeMaterielDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.EmployeMaterielEvenementDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.EmployeMaterielUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.EmployeMaterielService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employe-materiels")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class EmployeMaterielController {

    private final EmployeMaterielService service;

    @GetMapping
    public ResponseEntity<Page<EmployeMaterielDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) Long materielId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findAll(employeId, materielId, statut, dateDebut, dateFin, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeMaterielDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/{id}/evenements")
    public ResponseEntity<List<EmployeMaterielEvenementDTO>> findEvenements(@PathVariable Long id) {
        return ResponseEntity.ok(service.findEvenements(id));
    }

    @PostMapping
    public ResponseEntity<EmployeMaterielDTO> create(
            @Valid @RequestBody EmployeMaterielCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeMaterielDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeMaterielUpdateDTO dto,
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
