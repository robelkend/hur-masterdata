package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.MessageDefinitionCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.MessageDefinitionDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.MessageDefinitionUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.MessageDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message-definitions")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class MessageDefinitionController {
    
    private final MessageDefinitionService service;
    
    @GetMapping
    public ResponseEntity<Page<MessageDefinitionDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idMessage") String sortBy,
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
    
    @GetMapping("/dropdown")
    public ResponseEntity<List<MessageDefinitionDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MessageDefinitionDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<MessageDefinitionDTO> create(
            @Valid @RequestBody MessageDefinitionCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MessageDefinitionDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody MessageDefinitionUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (!dto.getIdMessage().equals(id)) {
            dto.setIdMessage(id);
        }
        return ResponseEntity.ok(service.update(dto, username));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
}
