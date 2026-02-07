package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.InterfaceLoadingCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.InterfaceLoadingDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.InterfaceLoadingUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.InterfaceLoadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interface-loadings")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class InterfaceLoadingController {
    
    private final InterfaceLoadingService service;
    
    @GetMapping
    public ResponseEntity<Page<InterfaceLoadingDTO>> findAll(
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
    public ResponseEntity<InterfaceLoadingDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<InterfaceLoadingDTO> create(
            @Valid @RequestBody InterfaceLoadingCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<InterfaceLoadingDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody InterfaceLoadingUpdateDTO dto,
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
    public ResponseEntity<List<InterfaceLoadingDTO>> findAllForDropdown() {
        List<InterfaceLoadingDTO> result = service.findAllForDropdown();
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/{id}/load-csv")
    public ResponseEntity<Map<String, Object>> loadCsv(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        try {
            Map<String, Object> result = service.loadCsv(id, file, username);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage(), "errors", List.of(e.getMessage())));
        }
    }
    
    @PostMapping("/{id}/load-db")
    public ResponseEntity<Map<String, Object>> loadFromDatabase(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        try {
            Map<String, Object> result = service.loadFromDatabase(id, username);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage(), "errors", List.of(e.getMessage())));
        }
    }

    @PostMapping("/{id}/test-rdb")
    public ResponseEntity<Map<String, Object>> testRdbConnection(@PathVariable Long id) {
        try {
            Map<String, Object> result = service.testRdbConnection(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage(), "errors", List.of(e.getMessage())));
        }
    }
    
    @PostMapping("/{id}/load-api")
    public ResponseEntity<Map<String, Object>> loadFromApi(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        try {
            Map<String, Object> result = service.loadFromApi(id, username);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage(), "errors", List.of(e.getMessage())));
        }
    }
    
    @GetMapping("/table-columns")
    public ResponseEntity<List<Map<String, Object>>> getTableColumns(@RequestParam String tableName) {
        try {
            List<Map<String, Object>> columns = service.getTableColumns(tableName);
            return ResponseEntity.ok(columns);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of());
        }
    }
}
