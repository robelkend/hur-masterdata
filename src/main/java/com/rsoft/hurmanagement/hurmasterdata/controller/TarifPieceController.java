package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.TarifPieceCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TarifPieceDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TarifPieceUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.TarifPieceService;
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
@RequestMapping("/api/tarif-pieces")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TarifPieceController {
    private final TarifPieceService service;

    @GetMapping("/type-piece/{typePieceId}")
    public ResponseEntity<Page<TarifPieceDTO>> findByTypePieceId(
            @PathVariable Long typePieceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateEffectif") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findByTypePieceId(typePieceId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarifPieceDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<TarifPieceDTO> create(
            @Valid @RequestBody TarifPieceCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarifPieceDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TarifPieceUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.update(id, dto, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
}
