package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.TypePieceCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypePieceDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypePieceUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.TypePieceService;
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
@RequestMapping("/api/type-pieces")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TypePieceController {
    private final TypePieceService service;

    @GetMapping
    public ResponseEntity<Page<TypePieceDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String codePiece,
            @RequestParam(required = false) Long entrepriseId) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.ASC;
        }
        String sortColumn = mapSortColumn(sortBy);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortColumn));
        return ResponseEntity.ok(service.findAll(pageable, codePiece, entrepriseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypePieceDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<TypePieceDTO> create(
            @Valid @RequestBody TypePieceCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypePieceDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TypePieceUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.update(id, dto, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }

    private String mapSortColumn(String sortBy) {
        if (sortBy == null) {
            return "id";
        }
        return switch (sortBy) {
            case "codePiece" -> "code_piece";
            case "createdOn" -> "created_on";
            case "updatedOn" -> "updated_on";
            case "entrepriseId" -> "entreprise_id";
            default -> sortBy;
        };
    }
}
