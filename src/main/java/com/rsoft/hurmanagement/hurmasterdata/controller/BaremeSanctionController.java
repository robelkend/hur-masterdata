package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.service.BaremeSanctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bareme-sanctions")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class BaremeSanctionController {
    
    private final BaremeSanctionService service;
    
    @GetMapping("/type-employe/{typeEmployeId}")
    public ResponseEntity<List<BaremeSanctionDTO>> findByTypeEmployeId(@PathVariable Long typeEmployeId) {
        List<BaremeSanctionDTO> result = service.findByTypeEmployeId(typeEmployeId);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping
    public ResponseEntity<BaremeSanctionDTO> create(
            @RequestBody BaremeSanctionCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        BaremeSanctionDTO created = service.create(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BaremeSanctionDTO> update(
            @PathVariable Long id,
            @RequestBody BaremeSanctionUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        BaremeSanctionDTO updated = service.update(id, dto, username);
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
