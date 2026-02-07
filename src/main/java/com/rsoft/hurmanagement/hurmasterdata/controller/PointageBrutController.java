package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.PointageBrutCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PointageBrutDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PointageBrutUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.PresenceBuilderService;
import com.rsoft.hurmanagement.hurmasterdata.service.PointageBrutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pointage-bruts")
@RequiredArgsConstructor
public class PointageBrutController {

    private final PointageBrutService service;
    private final PresenceBuilderService presenceBuilderService;

    @GetMapping
    public ResponseEntity<Page<PointageBrutDTO>> findAll(
            @RequestParam("dateDebut") String dateDebut,
            @RequestParam("dateFin") String dateFin,
            @RequestParam(value = "employeId", required = false) Long employeId,
            @RequestParam(value = "entrepriseId", required = false) Long entrepriseId,
            Pageable pageable) {
        return ResponseEntity.ok(service.findByFilters(dateDebut, dateFin, employeId, entrepriseId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PointageBrutDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<PointageBrutDTO> create(
            @Valid @RequestBody PointageBrutCreateDTO dto,
            @RequestHeader(value = "X-User", required = false) String username) {
        return ResponseEntity.ok(service.create(dto, username != null ? username : "SYSTEM"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PointageBrutDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PointageBrutUpdateDTO dto,
            @RequestHeader(value = "X-User", required = false) String username) {
        return ResponseEntity.ok(service.update(id, dto, username != null ? username : "SYSTEM"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam("rowscn") Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/load-from-interface")
    public ResponseEntity<java.util.Map<String, Object>> loadFromInterface(
            @RequestHeader(value = "X-User", required = false) String username) {
        return ResponseEntity.ok(service.loadFromClockInterface(username != null ? username : "SYSTEM"));
    }

    @PostMapping("/build-presences")
    public ResponseEntity<java.util.Map<String, Object>> buildPresences(
            @RequestParam(value = "dateDebut", required = false) String dateDebut,
            @RequestParam(value = "dateFin", required = false) String dateFin,
            @RequestParam(value = "employeId", required = false) Long employeId,
            @RequestParam(value = "entrepriseId", required = false) Long entrepriseId,
            @RequestHeader(value = "X-User", required = false) String username) {
        return ResponseEntity.ok(
                presenceBuilderService.processPunches(
                        dateDebut,
                        dateFin,
                        employeId,
                        entrepriseId,
                        username != null ? username : "SYSTEM"
                )
        );
    }
}
