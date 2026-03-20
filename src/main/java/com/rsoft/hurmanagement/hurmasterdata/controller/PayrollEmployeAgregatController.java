package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollEmployeAgregatDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollEmployeAgregatDeductionDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.PayrollEmployeAgregatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll-employe-agregats")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PayrollEmployeAgregatController {
    private final PayrollEmployeAgregatService service;

    @GetMapping
    public ResponseEntity<Page<PayrollEmployeAgregatDTO>> findAll(
            @RequestParam Long periodeBoniId,
            @RequestParam(required = false) Long regimePaieId,
            @RequestParam(required = false) Long employeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException ex) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findByFilters(periodeBoniId, regimePaieId, employeId, pageable));
    }

    @GetMapping("/{agregatId}/deductions")
    public ResponseEntity<List<PayrollEmployeAgregatDeductionDTO>> findDeductions(@PathVariable Long agregatId) {
        return ResponseEntity.ok(service.findDeductionsByAgregatId(agregatId));
    }
}
