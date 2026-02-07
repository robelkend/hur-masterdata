package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.BalanceCongeAnneeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.BalanceCongeDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.BalanceCongeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/balance-conges")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class BalanceCongeController {

    private final BalanceCongeService service;

    @GetMapping
    public ResponseEntity<Page<BalanceCongeDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) Long typeCongeId) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.ASC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findByFilters(entrepriseId, employeId, typeCongeId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BalanceCongeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/{id}/annees")
    public ResponseEntity<List<BalanceCongeAnneeDTO>> findAnnees(@PathVariable Long id) {
        return ResponseEntity.ok(service.findAnneesByBalanceId(id));
    }
}
