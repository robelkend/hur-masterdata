package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.ExclusionDeductionCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ExclusionDeductionDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ExclusionDeductionUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.ExclusionDeductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exclusion-deductions")
@RequiredArgsConstructor
public class ExclusionDeductionController {

    private final ExclusionDeductionService service;

    @GetMapping
    public ResponseEntity<List<ExclusionDeductionDTO>> findByTypeEmploye(
            @RequestParam Long typeEmployeId) {
        return ResponseEntity.ok(service.findByTypeEmployeId(typeEmployeId));
    }

    @PostMapping
    public ResponseEntity<ExclusionDeductionDTO> create(
            @Valid @RequestBody ExclusionDeductionCreateDTO dto,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return ResponseEntity.ok(service.create(dto, usernameOrDefault(username)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExclusionDeductionDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ExclusionDeductionUpdateDTO dto,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return ResponseEntity.ok(service.update(id, dto, usernameOrDefault(username)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }

    private String usernameOrDefault(String username) {
        return username != null && !username.trim().isEmpty() ? username : "system";
    }
}
