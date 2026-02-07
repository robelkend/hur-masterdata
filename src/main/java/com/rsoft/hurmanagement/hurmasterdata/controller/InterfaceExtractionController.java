package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.InterfaceExtractionCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.InterfaceExtractionDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.InterfaceExtractionUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.InterfaceExtractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interface-extractions")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class InterfaceExtractionController {
    
    private final InterfaceExtractionService service;
    
    @GetMapping
    public ResponseEntity<Page<InterfaceExtractionDTO>> findAll(
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
    public ResponseEntity<InterfaceExtractionDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<InterfaceExtractionDTO> create(
            @Valid @RequestBody InterfaceExtractionCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<InterfaceExtractionDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody InterfaceExtractionUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (!dto.getId().equals(id)) {
            dto.setId(id);
        }
        return ResponseEntity.ok(service.update(dto, username));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<InterfaceExtractionDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }
    
    @PostMapping("/{id}/export-csv")
    public ResponseEntity<byte[]> exportToCsv(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        byte[] csvData = service.exportToCsv(id, username);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "extraction_" + id + ".csv");
        headers.setContentLength(csvData.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
}
