package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.PrestationDepartDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.PrestationDepartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prestations-depart")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PrestationDepartController {

    private final PrestationDepartService service;

    @GetMapping
    public ResponseEntity<Page<PrestationDepartDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String statut) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findAll(employeId, statut, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestationDepartDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping("/calculate-from-mutation/{mutationId}")
    public ResponseEntity<PrestationDepartDTO> calculateFromMutation(
            @PathVariable Long mutationId,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.calculateFromMutation(mutationId, username));
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<PrestationDepartDTO> validate(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.validate(id, username));
    }
}
