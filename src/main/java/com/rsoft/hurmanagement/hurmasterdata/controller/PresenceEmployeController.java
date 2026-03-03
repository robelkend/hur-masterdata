package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.PresenceEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PresenceEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PresenceEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PointageBrutUsageDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.PointageBrutService;
import com.rsoft.hurmanagement.hurmasterdata.service.PresenceAutoFillService;
import com.rsoft.hurmanagement.hurmasterdata.service.PresenceEmployeService;
import com.rsoft.hurmanagement.hurmasterdata.service.PresenceRearrangeService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
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
@RequestMapping("/api/presence-employes")
@RequiredArgsConstructor
public class PresenceEmployeController {

    private final PresenceEmployeService service;
    private final PresenceRearrangeService rearrangeService;
    private final PresenceAutoFillService autoFillService;
    private final PointageBrutService pointageBrutService;

    @GetMapping
    public ResponseEntity<Page<PresenceEmployeDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = true) String dateDebut,
            @RequestParam(required = true) String dateFin,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) Long entrepriseId) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(service.findByFilters(dateDebut, dateFin, employeId, statut, entrepriseId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PresenceEmployeDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/{id}/pointages")
    public ResponseEntity<java.util.List<PointageBrutUsageDTO>> findPointagesByPresence(@PathVariable Long id) {
        return ResponseEntity.ok(pointageBrutService.findByPresenceEmployeId(id));
    }

    @PostMapping
    public ResponseEntity<PresenceEmployeDTO> create(
            @Valid @RequestBody PresenceEmployeCreateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PresenceEmployeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PresenceEmployeUpdateDTO dto,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (!dto.getId().equals(id)) {
            dto.setId(id);
        }
        return ResponseEntity.ok(service.update(id, dto, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        service.delete(id, rowscn);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rearrange-close")
    public ResponseEntity<java.util.Map<String, Object>> rearrangeAndClose(
            @Valid @RequestBody RearrangeRequest request,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(rearrangeService.closeAndRearrange(
                request.getDateDebut(),
                request.getDateFin(),
                request.getEmployeId(),
                request.getEntrepriseId(),
                username
        ));
    }

    @PostMapping("/auto-fill-schedule")
    public ResponseEntity<java.util.Map<String, Object>> autoFillFromSchedule(
            @Valid @RequestBody AutoFillRequest request,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        return ResponseEntity.ok(autoFillService.autoFillPresences(
                request.getDateDebut(),
                request.getDateFin(),
                request.getEntrepriseId(),
                request.getTypeEmployeId(),
                request.getUniteOrganisationnelleId(),
                request.getGestionnaireId(),
                request.getRegimePaieId(),
                username
        ));
    }

    @Data
    private static class RearrangeRequest {
        @NotBlank
        private String dateDebut;
        @NotBlank
        private String dateFin;
        private Long employeId;
        private Long entrepriseId;
    }

    @Data
    private static class AutoFillRequest {
        @NotBlank
        private String dateDebut;
        @NotBlank
        private String dateFin;
        private Long entrepriseId;
        private Long typeEmployeId;
        private Long uniteOrganisationnelleId;
        private Long gestionnaireId;
        private Long regimePaieId;
    }
}
