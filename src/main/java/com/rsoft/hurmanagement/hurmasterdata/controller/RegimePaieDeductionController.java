package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.service.RegimePaieDeductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regime-paie-deductions")
@RequiredArgsConstructor
public class RegimePaieDeductionController {
    
    private final RegimePaieDeductionService service;
    
    @GetMapping("/regime-paie/{regimePaieId}")
    public ResponseEntity<List<RegimePaieDeductionDTO>> findByRegimePaieId(@PathVariable Long regimePaieId) {
        List<RegimePaieDeductionDTO> result = service.findByRegimePaieId(regimePaieId);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping
    public ResponseEntity<RegimePaieDeductionDTO> create(
            @RequestBody RegimePaieDeductionCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        RegimePaieDeductionDTO created = service.create(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RegimePaieDeductionDTO> update(
            @PathVariable Long id,
            @RequestBody RegimePaieDeductionUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        RegimePaieDeductionDTO updated = service.update(id, dto, username);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
}
