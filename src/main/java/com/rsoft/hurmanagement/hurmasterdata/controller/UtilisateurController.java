package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.service.GroupeRoleUtilisateurService;
import com.rsoft.hurmanagement.hurmasterdata.service.UtilisateurService;
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
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UtilisateurController {
    
    private final UtilisateurService service;
    private final GroupeRoleUtilisateurService groupeRoleUtilisateurService;
    
    @GetMapping
    public ResponseEntity<Page<UtilisateurDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        Sort sort = sortDirection.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @GetMapping("/all-for-dropdown")
    public ResponseEntity<List<UtilisateurDTO>> findAllForDropdown() {
        return ResponseEntity.ok(service.findAllForDropdown());
    }

    @GetMapping("/{id}/groupes")
    public ResponseEntity<List<GroupeRoleUtilisateurDTO>> findGroupes(@PathVariable Long id) {
        return ResponseEntity.ok(groupeRoleUtilisateurService.findByUtilisateurId(id));
    }

    @PutMapping("/{id}/groupes")
    public ResponseEntity<List<GroupeRoleUtilisateurDTO>> replaceGroupes(
            @PathVariable Long id,
            @Valid @RequestBody List<GroupeRoleUtilisateurCreateDTO> dtos,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(groupeRoleUtilisateurService.replaceForUtilisateur(id, dtos, username));
    }
    
    @PostMapping
    public ResponseEntity<UtilisateurDTO> create(
            @Valid @RequestBody UtilisateurCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurUpdateDTO dto,
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
    public ResponseEntity<UtilisateurDTO> toggleActif(
            @PathVariable Long id,
            @RequestParam Integer rowscn,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.toggleActif(id, rowscn, username));
    }
}
