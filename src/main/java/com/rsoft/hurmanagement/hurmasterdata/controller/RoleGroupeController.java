package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.service.RoleGroupeRoleService;
import com.rsoft.hurmanagement.hurmasterdata.service.RoleGroupeService;
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
@RequestMapping("/api/role-groupes")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class RoleGroupeController {
    
    private final RoleGroupeService service;
    private final RoleGroupeRoleService roleGroupeRoleService;
    
    @GetMapping
    public ResponseEntity<Page<RoleGroupeDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RoleGroupeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @GetMapping("/all-for-dropdown")
    public ResponseEntity<List<RoleGroupeDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }
    
    @PostMapping
    public ResponseEntity<RoleGroupeDTO> create(
            @Valid @RequestBody RoleGroupeCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RoleGroupeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleGroupeUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.update(id, dto, username));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/toggle-actif")
    public ResponseEntity<RoleGroupeDTO> toggleActif(
            @PathVariable Long id,
            @RequestParam Integer rowscn,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.toggleActif(id, rowscn, username));
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<List<RoleGroupeRoleDTO>> findRolesByGroupe(@PathVariable Long id) {
        return ResponseEntity.ok(roleGroupeRoleService.findByGroupeId(id));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<List<RoleGroupeRoleDTO>> replaceRolesForGroupe(
            @PathVariable Long id,
            @RequestBody List<RoleGroupeRoleCreateDTO> dtos,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(roleGroupeRoleService.replaceForGroupe(id, dtos, username));
    }
}
