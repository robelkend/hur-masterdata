package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePaieDeductionCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RubriquePaieDeductionDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.RubriquePaieDeductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rubrique-paie-deductions")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class RubriquePaieDeductionController {
    
    private final RubriquePaieDeductionService service;
    
    @GetMapping("/definition/{definitionDeductionId}")
    public ResponseEntity<List<RubriquePaieDeductionDTO>> findByDefinitionDeductionId(
            @PathVariable Long definitionDeductionId) {
        return ResponseEntity.ok(service.findByDefinitionDeductionId(definitionDeductionId));
    }
    
    @PostMapping
    public ResponseEntity<RubriquePaieDeductionDTO> create(
            @Valid @RequestBody RubriquePaieDeductionCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
}
