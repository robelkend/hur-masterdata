package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollPeriodeBoniCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollPeriodeBoniDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollPeriodeBoniUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.PayrollPeriodeBoniService;
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
@RequestMapping({"/api/periode-paies", "/api/payroll-periode-bonis"})
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PayrollPeriodeBoniController {

    private final PayrollPeriodeBoniService service;

    @GetMapping
    public ResponseEntity<Page<PayrollPeriodeBoniDTO>> findAll(
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
    public ResponseEntity<PayrollPeriodeBoniDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<PayrollPeriodeBoniDTO> create(
            @Valid @RequestBody PayrollPeriodeBoniCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayrollPeriodeBoniDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PayrollPeriodeBoniUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.update(id, dto, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
}
