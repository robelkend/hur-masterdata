package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.service.EmployeService;
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

import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/employes")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class EmployeController {
    
    private final EmployeService service;
    
    @GetMapping
    public ResponseEntity<Page<EmployeDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String search,
            // Advanced search parameters
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false, defaultValue = "CONTAINS") String nomPattern,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false, defaultValue = "CONTAINS") String prenomPattern,
            @RequestParam(required = false) Long typeEmployeId,
            @RequestParam(required = false) Long uniteOrganisationnelleId,
            @RequestParam(required = false) Long horaireId,
            @RequestParam(required = false) Long fonctionId,
            @RequestParam(required = false) Long regimePaieId,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) Long gestionnaireId) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.ASC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<EmployeDTO> result;
        
        // Check if advanced search parameters are provided
        boolean hasAdvancedSearch = code != null || nom != null || prenom != null ||
                typeEmployeId != null || uniteOrganisationnelleId != null ||
                horaireId != null || fonctionId != null || regimePaieId != null ||
                entrepriseId != null || gestionnaireId != null;
        
        if (hasAdvancedSearch) {
            // Use advanced search
            result = service.searchAdvanced(
                    code, nom, nomPattern, prenom, prenomPattern,
                    typeEmployeId, uniteOrganisationnelleId, horaireId,
                    fonctionId, regimePaieId, entrepriseId, gestionnaireId,
                    pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            // Use simple search
            result = service.search(search.trim(), pageable);
        } else {
            // No search, return all
            result = service.findAll(pageable);
        }
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<EmployeDTO> create(
            @Valid @RequestBody EmployeCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EmployeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeUpdateDTO dto,
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
    
    @PostMapping("/{id}/photo")
    public ResponseEntity<EmployeDTO> uploadPhoto(
            @PathVariable Long id,
            @RequestBody PhotoUploadDTO photoDto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(service.uploadPhoto(id, photoDto.getPhoto(), username));
    }
    
    public static class PhotoUploadDTO {
        private String photo;
        
        public String getPhoto() {
            return photo;
        }
        
        public void setPhoto(String photo) {
            this.photo = photo;
        }
    }
    
    // ========== Adresses Endpoints ==========
    @GetMapping("/{employeId}/adresses")
    public ResponseEntity<List<EmployeAdresseDTO>> getAdresses(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.getAdresses(employeId));
    }
    
    @PostMapping("/{employeId}/adresses")
    public ResponseEntity<EmployeAdresseDTO> createAdresse(
            @PathVariable Long employeId,
            @RequestBody EmployeAdresseDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createAdresse(employeId, dto, username));
    }
    
    @PutMapping("/{employeId}/adresses/{adresseId}")
    public ResponseEntity<EmployeAdresseDTO> updateAdresse(
            @PathVariable Long employeId,
            @PathVariable Long adresseId,
            @RequestBody EmployeAdresseDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (dto.getId() == null || !dto.getId().equals(adresseId)) {
            dto.setId(adresseId);
        }
        return ResponseEntity.ok(service.updateAdresse(employeId, adresseId, dto, username));
    }
    
    @DeleteMapping("/{employeId}/adresses/{adresseId}")
    public ResponseEntity<Void> deleteAdresse(
            @PathVariable Long employeId,
            @PathVariable Long adresseId,
            @RequestParam Integer rowscn) {
        service.deleteAdresse(employeId, adresseId, rowscn);
        return ResponseEntity.noContent().build();
    }
    
    // ========== Identites Endpoints ==========
    @GetMapping("/{employeId}/identites")
    public ResponseEntity<List<EmployeIdentiteDTO>> getIdentites(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.getIdentites(employeId));
    }
    
    @PostMapping("/{employeId}/identites")
    public ResponseEntity<EmployeIdentiteDTO> createIdentite(
            @PathVariable Long employeId,
            @RequestBody EmployeIdentiteDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createIdentite(employeId, dto, username));
    }
    
    @PutMapping("/{employeId}/identites/{identiteId}")
    public ResponseEntity<EmployeIdentiteDTO> updateIdentite(
            @PathVariable Long employeId,
            @PathVariable Long identiteId,
            @RequestBody EmployeIdentiteDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (dto.getId() == null || !dto.getId().equals(identiteId)) {
            dto.setId(identiteId);
        }
        return ResponseEntity.ok(service.updateIdentite(employeId, identiteId, dto, username));
    }
    
    @DeleteMapping("/{employeId}/identites/{identiteId}")
    public ResponseEntity<Void> deleteIdentite(
            @PathVariable Long employeId,
            @PathVariable Long identiteId) {
        service.deleteIdentite(employeId, identiteId);
        return ResponseEntity.noContent().build();
    }
    
    // ========== Contacts Endpoints ==========
    @GetMapping("/{employeId}/contacts")
    public ResponseEntity<List<EmployeContactDTO>> getContacts(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.getContacts(employeId));
    }
    
    @PostMapping("/{employeId}/contacts")
    public ResponseEntity<EmployeContactDTO> createContact(
            @PathVariable Long employeId,
            @RequestBody EmployeContactDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createContact(employeId, dto, username));
    }
    
    @PutMapping("/{employeId}/contacts/{contactId}")
    public ResponseEntity<EmployeContactDTO> updateContact(
            @PathVariable Long employeId,
            @PathVariable Long contactId,
            @RequestBody EmployeContactDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (dto.getId() == null || !dto.getId().equals(contactId)) {
            dto.setId(contactId);
        }
        return ResponseEntity.ok(service.updateContact(employeId, contactId, dto, username));
    }
    
    @DeleteMapping("/{employeId}/contacts/{contactId}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long employeId,
            @PathVariable Long contactId) {
        service.deleteContact(employeId, contactId);
        return ResponseEntity.noContent().build();
    }
    
    // ========== Notes Endpoints ==========
    @GetMapping("/{employeId}/notes")
    public ResponseEntity<List<EmployeNoteDTO>> getNotes(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.getNotes(employeId));
    }
    
    @PostMapping("/{employeId}/notes")
    public ResponseEntity<EmployeNoteDTO> createNote(
            @PathVariable Long employeId,
            @RequestBody EmployeNoteDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createNote(employeId, dto, username));
    }
    
    @PutMapping("/{employeId}/notes/{noteId}")
    public ResponseEntity<EmployeNoteDTO> updateNote(
            @PathVariable Long employeId,
            @PathVariable Long noteId,
            @RequestBody EmployeNoteDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (dto.getId() == null || !dto.getId().equals(noteId)) {
            dto.setId(noteId);
        }
        return ResponseEntity.ok(service.updateNote(employeId, noteId, dto, username));
    }
    
    @DeleteMapping("/{employeId}/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long employeId,
            @PathVariable Long noteId) {
        service.deleteNote(employeId, noteId);
        return ResponseEntity.noContent().build();
    }
    
    // ========== Documents Endpoints ==========
    @GetMapping("/{employeId}/documents")
    public ResponseEntity<List<EmployeDocumentDTO>> getDocuments(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.getDocuments(employeId));
    }
    
    @PostMapping(value = "/{employeId}/documents", consumes = "multipart/form-data")
    public ResponseEntity<EmployeDocumentDTO> uploadDocument(
            @PathVariable Long employeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("typeDocument") String typeDocument,
            @RequestParam(value = "commentaire", required = false) String commentaire,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        try {
            // Read file bytes once
            byte[] fileBytes = file.getBytes();
            
            // For now, we'll store the file content as base64 in storageRef
            // In production, you would save to S3, disk, or blob storage
            String base64Content = Base64.getEncoder().encodeToString(fileBytes);
            String storageRef = "base64:" + base64Content;
            
            // Calculate SHA256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileBytes);
            StringBuilder hashString = new StringBuilder();
            for (byte b : hashBytes) {
                hashString.append(String.format("%02x", b));
            }
            String hashSha256 = hashString.toString();
            
            EmployeDocumentDTO result = service.uploadDocument(
                employeId,
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown",
                file.getContentType(),
                file.getSize(),
                storageRef,
                hashSha256,
                typeDocument,
                commentaire,
                username
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            throw new RuntimeException("Error uploading document: " + e.getMessage(), e);
        }
    }
    
    @DeleteMapping("/{employeId}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long employeId,
            @PathVariable Long documentId) {
        service.deleteDocument(employeId, documentId);
        return ResponseEntity.noContent().build();
    }
    
    // ========== Coordonnees Bancaires Endpoints ==========
    @GetMapping("/{employeId}/coordonnees-bancaires")
    public ResponseEntity<List<CoordonneeBancaireEmployeDTO>> getCoordonneesBancaires(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.getCoordonneesBancaires(employeId));
    }
    
    @PostMapping("/{employeId}/coordonnees-bancaires")
    public ResponseEntity<CoordonneeBancaireEmployeDTO> createCoordonneeBancaire(
            @PathVariable Long employeId,
            @RequestBody CoordonneeBancaireEmployeDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createCoordonneeBancaire(employeId, dto, username));
    }
    
    @PutMapping("/{employeId}/coordonnees-bancaires/{coordId}")
    public ResponseEntity<CoordonneeBancaireEmployeDTO> updateCoordonneeBancaire(
            @PathVariable Long employeId,
            @PathVariable Long coordId,
            @RequestBody CoordonneeBancaireEmployeDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (dto.getId() == null || !dto.getId().equals(coordId)) {
            dto.setId(coordId);
        }
        return ResponseEntity.ok(service.updateCoordonneeBancaire(employeId, coordId, dto, username));
    }
    
    @DeleteMapping("/{employeId}/coordonnees-bancaires/{coordId}")
    public ResponseEntity<Void> deleteCoordonneeBancaire(
            @PathVariable Long employeId,
            @PathVariable Long coordId) {
        service.deleteCoordonneeBancaire(employeId, coordId);
        return ResponseEntity.noContent().build();
    }
    
    // ========== Assurances Endpoints ==========
    @GetMapping("/{employeId}/assurances")
    public ResponseEntity<List<AssuranceEmployeDTO>> getAssurances(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.getAssurances(employeId));
    }
    
    @PostMapping("/{employeId}/assurances")
    public ResponseEntity<AssuranceEmployeDTO> createAssurance(
            @PathVariable Long employeId,
            @RequestBody AssuranceEmployeDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createAssurance(employeId, dto, username));
    }
    
    @PutMapping("/{employeId}/assurances/{assuranceId}")
    public ResponseEntity<AssuranceEmployeDTO> updateAssurance(
            @PathVariable Long employeId,
            @PathVariable Long assuranceId,
            @RequestBody AssuranceEmployeDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (dto.getId() == null || !dto.getId().equals(assuranceId)) {
            dto.setId(assuranceId);
        }
        return ResponseEntity.ok(service.updateAssurance(employeId, assuranceId, dto, username));
    }
    
    @DeleteMapping("/{employeId}/assurances/{assuranceId}")
    public ResponseEntity<Void> deleteAssurance(
            @PathVariable Long employeId,
            @PathVariable Long assuranceId) {
        service.deleteAssurance(employeId, assuranceId);
        return ResponseEntity.noContent().build();
    }
    
    // ========== Emplois Endpoints ==========
    @GetMapping("/{employeId}/emplois")
    public ResponseEntity<List<EmploiEmployeDTO>> getEmplois(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.getEmplois(employeId));
    }
    
    @PostMapping("/{employeId}/emplois")
    public ResponseEntity<EmploiEmployeDTO> createEmploi(
            @PathVariable Long employeId,
            @RequestBody EmploiEmployeDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createEmploi(employeId, dto, username));
    }
    
    @PutMapping("/{employeId}/emplois/{emploiId}")
    public ResponseEntity<EmploiEmployeDTO> updateEmploi(
            @PathVariable Long employeId,
            @PathVariable Long emploiId,
            @RequestBody EmploiEmployeDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (dto.getId() == null || !dto.getId().equals(emploiId)) {
            dto.setId(emploiId);
        }
        return ResponseEntity.ok(service.updateEmploi(employeId, emploiId, dto, username));
    }
    
    @DeleteMapping("/{employeId}/emplois/{emploiId}")
    public ResponseEntity<Void> deleteEmploi(
            @PathVariable Long employeId,
            @PathVariable Long emploiId) {
        service.deleteEmploi(employeId, emploiId);
        return ResponseEntity.noContent().build();
    }
    
    // ========== Salaires Endpoints ==========
    @GetMapping("/{employeId}/salaires")
    public ResponseEntity<List<EmployeSalaireDTO>> getSalaires(@PathVariable Long employeId) {
        return ResponseEntity.ok(service.getSalaires(employeId));
    }
    
    @PostMapping("/{employeId}/salaires")
    public ResponseEntity<EmployeSalaireDTO> createSalaire(
            @PathVariable Long employeId,
            @RequestBody EmployeSalaireDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createSalaire(employeId, dto, username));
    }
    
    @PutMapping("/{employeId}/salaires/{salaireId}")
    public ResponseEntity<EmployeSalaireDTO> updateSalaire(
            @PathVariable Long employeId,
            @PathVariable Long salaireId,
            @RequestBody EmployeSalaireDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (dto.getId() == null || !dto.getId().equals(salaireId)) {
            dto.setId(salaireId);
        }
        return ResponseEntity.ok(service.updateSalaire(employeId, salaireId, dto, username));
    }
    
    @DeleteMapping("/{employeId}/salaires/{salaireId}")
    public ResponseEntity<Void> deleteSalaire(
            @PathVariable Long employeId,
            @PathVariable Long salaireId) {
        service.deleteSalaire(employeId, salaireId);
        return ResponseEntity.noContent().build();
    }
}
