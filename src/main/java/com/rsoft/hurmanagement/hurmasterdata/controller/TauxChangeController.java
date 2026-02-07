package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.TauxChangeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TauxChangeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TauxChangeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.TauxChangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/taux-changes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TauxChangeController {
    
    private final TauxChangeService service;
    
    @GetMapping
    public ResponseEntity<Page<TauxChangeDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateTaux") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String codeDevise) {
        
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<TauxChangeDTO> result = service.findAll(codeDevise, pageable);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/by-devise/{codeDevise}")
    public ResponseEntity<List<TauxChangeDTO>> findByDevise(
            @PathVariable String codeDevise,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        List<TauxChangeDTO> result = service.findByDeviseCodeDevise(codeDevise, dateFrom, dateTo);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TauxChangeDTO> findById(@PathVariable Long id) {
        TauxChangeDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<TauxChangeDTO> create(
            @Valid @RequestBody TauxChangeCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        TauxChangeDTO created = service.create(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TauxChangeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TauxChangeUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        TauxChangeDTO updated = service.update(id, dto, username);
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
