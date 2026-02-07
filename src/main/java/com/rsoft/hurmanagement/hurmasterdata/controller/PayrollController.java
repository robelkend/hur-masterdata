package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping
    public ResponseEntity<Page<PayrollDTO>> findAll(
            @RequestParam(value = "regimePaieId", required = false) Long regimePaieId,
            @RequestParam(value = "statut", required = false) String statut,
            @RequestParam(value = "dateFinFrom", required = false) String dateFinFrom,
            @RequestParam(value = "dateFinTo", required = false) String dateFinTo,
            Pageable pageable) {
        return ResponseEntity.ok(payrollService.findByFilters(regimePaieId, statut, dateFinFrom, dateFinTo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PayrollDTO> create(
            @Valid @RequestBody PayrollCreateDTO dto,
            @RequestHeader(value = "X-User", required = false) String username) {
        return ResponseEntity.ok(payrollService.create(dto, username != null ? username : "system"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayrollDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PayrollUpdateDTO dto,
            @RequestHeader(value = "X-User", required = false) String username) {
        return ResponseEntity.ok(payrollService.update(id, dto, username != null ? username : "system"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam("rowscn") Integer rowscn,
            @RequestHeader(value = "X-User", required = false) String username) {
        payrollService.delete(id, rowscn, username != null ? username : "system");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<PayrollDTO> calculate(
            @PathVariable Long id,
            @RequestHeader(value = "X-User", required = false) String username) {
        return ResponseEntity.ok(payrollService.calculate(id, username != null ? username : "system"));
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<PayrollDTO> validate(
            @PathVariable Long id,
            @RequestHeader(value = "X-User", required = false) String username) {
        return ResponseEntity.ok(payrollService.validate(id, username != null ? username : "system"));
    }

    @PostMapping("/{id}/finalise")
    public ResponseEntity<PayrollDTO> finalise(
            @PathVariable Long id,
            @RequestHeader(value = "X-User", required = false) String username) {
        return ResponseEntity.ok(payrollService.finalise(id, username != null ? username : "system"));
    }

    @GetMapping("/{id}/employes")
    public ResponseEntity<List<PayrollEmployeDTO>> employes(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.findEmployes(id));
    }

    @GetMapping("/{id}/gains")
    public ResponseEntity<List<PayrollGainDTO>> gains(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.findGains(id));
    }

    @GetMapping("/{id}/deductions")
    public ResponseEntity<List<PayrollDeductionDTO>> deductions(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.findDeductions(id));
    }

    @GetMapping("/{id}/recouvrements")
    public ResponseEntity<List<PayrollRecouvrementDTO>> recouvrements(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.findRecouvrements(id));
    }

    @GetMapping("/{id}/sanctions")
    public ResponseEntity<List<PayrollSanctionDTO>> sanctions(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.findSanctions(id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<List<PayrollEmployeStatsDTO>> stats(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.findStats(id));
    }
}
