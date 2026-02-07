package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.service.AppRoleService;
import com.rsoft.hurmanagement.hurmasterdata.service.RolePermissionService;
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
@RequestMapping("/api/app-roles")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AppRoleController {
    
    private final AppRoleService service;
    private final RolePermissionService rolePermissionService;
    
    @GetMapping
    public ResponseEntity<Page<AppRoleDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AppRoleDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @GetMapping("/all-for-dropdown")
    public ResponseEntity<List<AppRoleDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }
    
    @PostMapping
    public ResponseEntity<AppRoleDTO> create(
            @Valid @RequestBody AppRoleCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AppRoleDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppRoleUpdateDTO dto,
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
    public ResponseEntity<AppRoleDTO> toggleActif(
            @PathVariable Long id,
            @RequestParam Integer rowscn,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.toggleActif(id, rowscn, username));
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<RolePermissionDTO>> findPermissionsByRole(@PathVariable Long id) {
        return ResponseEntity.ok(rolePermissionService.findByRoleId(id));
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<List<RolePermissionDTO>> replacePermissionsForRole(
            @PathVariable Long id,
            @RequestBody List<RolePermissionCreateDTO> dtos,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(rolePermissionService.replaceForRole(id, dtos, username));
    }
}
