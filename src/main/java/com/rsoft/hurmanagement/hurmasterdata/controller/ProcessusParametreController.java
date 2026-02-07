package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.ProcessusParametre;
import com.rsoft.hurmanagement.hurmasterdata.job.ProcessusJobService;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.ProcessusParametreRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/processus-parametres")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class ProcessusParametreController {
    private final ProcessusParametreRepository repository;
    private final EntrepriseRepository entrepriseRepository;
    private final ProcessusJobService jobService;

    @GetMapping
    public ResponseEntity<Page<ProcessusParametre>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "codeProcessus") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.ASC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessusParametre> findById(@PathVariable Long id) {
        return ResponseEntity.ok(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProcessusParametre not found with id: " + id)));
    }

    @PostMapping
    public ResponseEntity<ProcessusParametre> create(
            @Valid @RequestBody ProcessusParametreCreateRequest request,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        if (repository.findByCodeProcessus(request.getCodeProcessus()).isPresent()) {
            throw new RuntimeException("ProcessusParametre with code " + request.getCodeProcessus() + " already exists");
        }
        ProcessusParametre entity = new ProcessusParametre();
        entity.setCodeProcessus(request.getCodeProcessus());
        entity.setDescription(request.getDescription());
        entity.setActif(request.getActif() != null ? request.getActif() : "Y");
        entity.setDerniereExecutionAt(request.getDerniereExecutionAt());
        entity.setProchaineExecutionAt(request.getProchaineExecutionAt());
        entity.setFrequence(request.getFrequence());
        entity.setNombre(request.getNombre() != null ? request.getNombre() : 1);
        entity.setMarge(request.getMarge() != null ? request.getMarge() : 0);
        entity.setUniteMarge(request.getUniteMarge());
        entity.setStatut(ProcessusParametre.Statut.PRET);
        entity.setNbEchecsConsecutifs(0);
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());

        if (request.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(request.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + request.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessusParametre> update(
            @PathVariable Long id,
            @Valid @RequestBody ProcessusParametreUpdateRequest request,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        ProcessusParametre entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProcessusParametre not found with id: " + id));

        entity.setCodeProcessus(request.getCodeProcessus());
        entity.setDescription(request.getDescription());
        entity.setActif(request.getActif() != null ? request.getActif() : entity.getActif());
        entity.setDerniereExecutionAt(request.getDerniereExecutionAt());
        entity.setProchaineExecutionAt(request.getProchaineExecutionAt());
        entity.setFrequence(request.getFrequence());
        entity.setNombre(request.getNombre() != null ? request.getNombre() : entity.getNombre());
        entity.setMarge(request.getMarge() != null ? request.getMarge() : entity.getMarge());
        entity.setUniteMarge(request.getUniteMarge());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());

        if (request.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(request.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + request.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        } else {
            entity.setEntreprise(null);
        }

        return ResponseEntity.ok(repository.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Integer rowscn) {
        ProcessusParametre entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProcessusParametre not found with id: " + id));
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        repository.delete(entity);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/trigger")
    public ResponseEntity<ProcessusParametre> triggerExecution(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", defaultValue = "system") String username) {
        ProcessusParametre entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProcessusParametre not found with id: " + id));
        if (!"Y".equalsIgnoreCase(entity.getActif())) {
            throw new RuntimeException("Processus is not active");
        }
        if (entity.getStatut() == ProcessusParametre.Statut.EN_EXECUTION) {
            throw new RuntimeException("Processus is already running");
        }
        entity.setStatut(ProcessusParametre.Statut.EN_EXECUTION);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        repository.save(entity);

        try {
            jobService.runProcessus(entity, username);
            entity.setStatut(ProcessusParametre.Statut.REUSSI);
            entity.setDerniereErreur(null);
            entity.setNbEchecsConsecutifs(0);
        } catch (Exception e) {
            entity.setStatut(ProcessusParametre.Statut.ERREUR);
            entity.setDerniereErreur(e.getMessage());
            entity.setNbEchecsConsecutifs(entity.getNbEchecsConsecutifs() != null ? entity.getNbEchecsConsecutifs() + 1 : 1);
        }

        entity.setProchaineExecutionAt(computeNextExecution(entity, OffsetDateTime.now()));
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        return ResponseEntity.ok(repository.save(entity));
    }

    private OffsetDateTime computeNextExecution(ProcessusParametre job, OffsetDateTime base) {
        if (job.getFrequence() == null || job.getNombre() == null) {
            return null;
        }
        int amount = Math.max(1, job.getNombre());
        return switch (job.getFrequence()) {
            case MINUTE -> base.plusMinutes(amount);
            case HEURE -> base.plusHours(amount);
            case JOUR -> base.plusDays(amount);
            case SEMAINE -> base.plusWeeks(amount);
            case MOIS -> base.plusMonths(amount);
            case ANNEE -> base.plusYears(amount);
        };
    }

    @Data
    private static class ProcessusParametreCreateRequest {
        @NotBlank
        private String codeProcessus;
        @NotBlank
        private String description;
        private String actif;
        private OffsetDateTime derniereExecutionAt;
        private OffsetDateTime prochaineExecutionAt;
        @NotNull
        private ProcessusParametre.Frequence frequence;
        private Integer nombre;
        private Integer marge;
        @NotNull
        private ProcessusParametre.UniteMarge uniteMarge;
        private Long entrepriseId;
    }

    @Data
    private static class ProcessusParametreUpdateRequest {
        @NotNull
        private Integer rowscn;
        @NotBlank
        private String codeProcessus;
        @NotBlank
        private String description;
        private String actif;
        private OffsetDateTime derniereExecutionAt;
        private OffsetDateTime prochaineExecutionAt;
        @NotNull
        private ProcessusParametre.Frequence frequence;
        private Integer nombre;
        private Integer marge;
        @NotNull
        private ProcessusParametre.UniteMarge uniteMarge;
        private Long entrepriseId;
    }
}
