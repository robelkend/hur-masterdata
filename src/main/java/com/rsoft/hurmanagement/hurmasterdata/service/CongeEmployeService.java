package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.CongeEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.CongeEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.CongeEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CongeEmployeService {

    private final CongeEmployeRepository repository;
    private final EmployeRepository employeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final TypeCongeRepository typeCongeRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final PresenceEmployeRepository presenceEmployeRepository;
    private final JourCongeRepository jourCongeRepository;

    @Transactional(readOnly = true)
    public Page<CongeEmployeDTO> findAllWithFilters(
            Long entrepriseId,
            Long employeId,
            Long typeCongeId,
            String statut,
            LocalDate dateDebut,
            LocalDate dateFin,
            Pageable pageable) {
        
        return repository.findAllWithFilters(entrepriseId, employeId, typeCongeId, statut, dateDebut, dateFin, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public CongeEmployeDTO findById(Long id) {
        CongeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("CongeEmploye not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional
    public CongeEmployeDTO create(CongeEmployeCreateDTO dto, String username) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));

        TypeConge typeConge = typeCongeRepository.findById(dto.getTypeCongeId())
                .orElseThrow(() -> new RuntimeException("TypeConge not found with id: " + dto.getTypeCongeId()));

        // Validate dates
        if (dto.getDateFinPlan().isBefore(dto.getDateDebutPlan())) {
            throw new RuntimeException("Date fin plan must be >= date debut plan");
        }

        CongeEmploye entity = new CongeEmploye();
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        }

        EmploiEmploye emploi = resolveEmploiEmploye(dto.getEmployeId(), dto.getEmploiEmployeId());
        entity.setEmploye(employe);
        entity.setEmploiEmploye(emploi);
        entity.setTypeConge(typeConge);
        entity.setDateDebutPlan(dto.getDateDebutPlan());
        entity.setDateFinPlan(dto.getDateFinPlan());
        entity.setDateDebutReel(dto.getDateDebutReel());
        entity.setDateFinReel(dto.getDateFinReel());
        entity.setMotif(dto.getMotif());
        entity.setReference(dto.getReference());
        entity.setStatut(CongeEmploye.StatutConge.valueOf(dto.getStatut()));

        // Calculate nb_jours_plan
        entity.setNbJoursPlan(calculateNbJoursExcludingOffAndHolidays(emploi, dto.getDateDebutPlan(), dto.getDateFinPlan()));

        // Calculate nb_jours_reel if dates are provided
        if (dto.getDateDebutReel() != null && dto.getDateFinReel() != null) {
            if (dto.getDateFinReel().isBefore(dto.getDateDebutReel())) {
                throw new RuntimeException("Date fin reel must be >= date debut reel");
            }
            entity.setNbJoursReel(calculateNbJoursExcludingOffAndHolidays(emploi, dto.getDateDebutReel(), dto.getDateFinReel()));
        } else {
            entity.setNbJoursReel(BigDecimal.ZERO);
        }

        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public CongeEmployeDTO update(Long id, CongeEmployeUpdateDTO dto, String username) {
        CongeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("CongeEmploye not found with id: " + id));

        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }

        // Can only update if status is BROUILLON
        if (entity.getStatut() != CongeEmploye.StatutConge.BROUILLON) {
            throw new RuntimeException("Cannot update conge with status: " + entity.getStatut() + ". Only BROUILLON records can be updated.");
        }

        // Validate dates
        if (dto.getDateFinPlan().isBefore(dto.getDateDebutPlan())) {
            throw new RuntimeException("Date fin plan must be >= date debut plan");
        }

        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        } else {
            entity.setEntreprise(null);
        }

        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        entity.setEmploye(employe);
        EmploiEmploye emploi = resolveEmploiEmploye(dto.getEmployeId(), dto.getEmploiEmployeId());
        entity.setEmploiEmploye(emploi);

        TypeConge typeConge = typeCongeRepository.findById(dto.getTypeCongeId())
                .orElseThrow(() -> new RuntimeException("TypeConge not found with id: " + dto.getTypeCongeId()));
        entity.setTypeConge(typeConge);

        entity.setDateDebutPlan(dto.getDateDebutPlan());
        entity.setDateFinPlan(dto.getDateFinPlan());
        entity.setDateDebutReel(dto.getDateDebutReel());
        entity.setDateFinReel(dto.getDateFinReel());
        entity.setMotif(dto.getMotif());
        entity.setReference(dto.getReference());
        entity.setStatut(CongeEmploye.StatutConge.valueOf(dto.getStatut()));

        // Recalculate nb_jours_plan
        entity.setNbJoursPlan(calculateNbJoursExcludingOffAndHolidays(emploi, dto.getDateDebutPlan(), dto.getDateFinPlan()));

        // Recalculate nb_jours_reel if dates are provided
        if (dto.getDateDebutReel() != null && dto.getDateFinReel() != null) {
            if (dto.getDateFinReel().isBefore(dto.getDateDebutReel())) {
                throw new RuntimeException("Date fin reel must be >= date debut reel");
            }
            entity.setNbJoursReel(calculateNbJoursExcludingOffAndHolidays(emploi, dto.getDateDebutReel(), dto.getDateFinReel()));
        } else {
            entity.setNbJoursReel(BigDecimal.ZERO);
        }

        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        CongeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("CongeEmploye not found with id: " + id));

        // Can only delete if status is BROUILLON
        if (entity.getStatut() != CongeEmploye.StatutConge.BROUILLON) {
            throw new RuntimeException("Cannot delete conge with status: " + entity.getStatut() + ". Only BROUILLON records can be deleted.");
        }

        repository.delete(entity);
    }

    @Transactional
    public CongeEmployeDTO soumettre(Long id, String username) {
        CongeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("CongeEmploye not found with id: " + id));

        if (entity.getStatut() != CongeEmploye.StatutConge.BROUILLON) {
            throw new RuntimeException("Cannot submit conge with status: " + entity.getStatut() + ". Only BROUILLON records can be submitted.");
        }

        entity.setStatut(CongeEmploye.StatutConge.SOUMIS);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public CongeEmployeDTO approuver(Long id, String commentaire, String username) {
        CongeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("CongeEmploye not found with id: " + id));

        if (entity.getStatut() != CongeEmploye.StatutConge.SOUMIS) {
            throw new RuntimeException("Cannot approve conge with status: " + entity.getStatut() + ". Only SOUMIS records can be approved.");
        }

        entity.setStatut(CongeEmploye.StatutConge.APPROUVE);
        entity.setApprobateur(username);
        entity.setDateDecision(OffsetDateTime.now());
        entity.setCommentaireDecision(commentaire);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public CongeEmployeDTO rejeter(Long id, String commentaire, String username) {
        CongeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("CongeEmploye not found with id: " + id));

        if (entity.getStatut() != CongeEmploye.StatutConge.SOUMIS) {
            throw new RuntimeException("Cannot reject conge with status: " + entity.getStatut() + ". Only SOUMIS records can be rejected.");
        }

        entity.setStatut(CongeEmploye.StatutConge.REJETE);
        entity.setApprobateur(username);
        entity.setDateDecision(OffsetDateTime.now());
        entity.setCommentaireDecision(commentaire);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public CongeEmployeDTO demarrer(Long id, String username) {
        CongeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("CongeEmploye not found with id: " + id));

        if (entity.getStatut() != CongeEmploye.StatutConge.APPROUVE
                && entity.getStatut() != CongeEmploye.StatutConge.BROUILLON) {
            throw new RuntimeException("Cannot start conge with status: " + entity.getStatut() + ". Only BROUILLON or APPROUVE records can be started.");
        }

        entity.setStatut(CongeEmploye.StatutConge.EN_COURS);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public CongeEmployeDTO annuler(Long id, String username) {
        CongeEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("CongeEmploye not found with id: " + id));

        if (entity.getStatut() != CongeEmploye.StatutConge.APPROUVE
                && entity.getStatut() != CongeEmploye.StatutConge.BROUILLON) {
            throw new RuntimeException("Cannot cancel conge with status: " + entity.getStatut() + ". Only BROUILLON or APPROUVE records can be cancelled.");
        }

        entity.setStatut(CongeEmploye.StatutConge.ANNULE);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public java.util.Map<String, Object> autoStartApprovedForDate(LocalDate targetDate, Long entrepriseId, String username) {
        List<CongeEmploye> conges = repository.findByStatut(CongeEmploye.StatutConge.APPROUVE);
        int totalRows = 0;
        int startedRows = 0;
        int skippedWithPresence = 0;
        for (CongeEmploye conge : conges) {
            if (entrepriseId != null) {
                Long congeEntrepriseId = conge.getEntreprise() != null ? conge.getEntreprise().getId() : null;
                if (!entrepriseId.equals(congeEntrepriseId)) {
                    continue;
                }
            }
            totalRows++;
            Long employeId = conge.getEmploye() != null ? conge.getEmploye().getId() : null;
            LocalDate dateDebutPlan = conge.getDateDebutPlan();
            if (employeId == null || dateDebutPlan == null) {
                continue;
            }
            boolean hasPresence = presenceEmployeRepository.existsByEmployeIdAndDateJourGreaterThanEqualAndStatutPresence(
                    employeId,
                    dateDebutPlan,
                    PresenceEmploye.StatutPresence.VALIDE
            );
            if (hasPresence) {
                skippedWithPresence++;
                continue;
            }
            demarrer(conge.getId(), username);
            startedRows++;
        }
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("totalRows", totalRows);
        result.put("startedRows", startedRows);
        result.put("skippedWithPresence", skippedWithPresence);
        result.put("message", "Started " + startedRows + " leaves");
        return result;
    }

    private BigDecimal calculateNbJoursExcludingOffAndHolidays(EmploiEmploye emploi, LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null || dateFin == null) {
            return BigDecimal.ZERO;
        }
        LocalDate start = dateDebut;
        LocalDate end = dateFin;
        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        long count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (isOffDay(emploi, date)) {
                continue;
            }
            if (jourCongeRepository.existsByDateCongeAndActif(date, JourConge.Actif.Y)) {
                continue;
            }
            count++;
        }
        return BigDecimal.valueOf(count).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isOffDay(EmploiEmploye emploi, LocalDate date) {
        if (emploi == null || date == null) {
            return false;
        }
        int day = date.getDayOfWeek().getValue();
        return java.util.Objects.equals(emploi.getJourOff1(), day)
                || java.util.Objects.equals(emploi.getJourOff2(), day)
                || java.util.Objects.equals(emploi.getJourOff3(), day);
    }

    private CongeEmployeDTO toDTO(CongeEmploye entity) {
        CongeEmployeDTO dto = new CongeEmployeDTO();
        dto.setId(entity.getId());
        
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        
        dto.setEmployeId(entity.getEmploye().getId());
        dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
        dto.setEmployeNom(entity.getEmploye().getNom());
        dto.setEmployePrenom(entity.getEmploye().getPrenom());
        if (entity.getEmploiEmploye() != null) {
            dto.setEmploiEmployeId(entity.getEmploiEmploye().getId());
        }
        
        dto.setTypeCongeId(entity.getTypeConge().getId());
        dto.setTypeCongeCode(entity.getTypeConge().getCodeConge());
        dto.setTypeCongeDescription(entity.getTypeConge().getDescription());
        
        dto.setDateDebutPlan(entity.getDateDebutPlan());
        dto.setDateFinPlan(entity.getDateFinPlan());
        dto.setDateDebutReel(entity.getDateDebutReel());
        dto.setDateFinReel(entity.getDateFinReel());
        dto.setMotif(entity.getMotif());
        dto.setReference(entity.getReference());
        dto.setApprobateur(entity.getApprobateur());
        dto.setDateDecision(entity.getDateDecision());
        dto.setCommentaireDecision(entity.getCommentaireDecision());
        dto.setNbJoursPlan(entity.getNbJoursPlan());
        dto.setNbJoursReel(entity.getNbJoursReel());
        dto.setStatut(entity.getStatut().name());
        
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        return dto;
    }

    private EmploiEmploye resolveEmploiEmploye(Long employeId, Long emploiEmployeId) {
        if (employeId == null) {
            return null;
        }
        if (emploiEmployeId != null) {
            EmploiEmploye emploi = emploiEmployeRepository.findById(emploiEmployeId)
                    .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiEmployeId));
            if (!emploi.getEmploye().getId().equals(employeId)) {
                throw new RuntimeException("EmploiEmploye does not belong to employe " + employeId);
            }
            if (emploi.getStatutEmploi() == EmploiEmploye.StatutEmploi.TERMINE) {
                throw new RuntimeException("EmploiEmploye is terminated and cannot be used.");
            }
            return emploi;
        }

        List<EmploiEmploye> emplois = emploiEmployeRepository.findByEmployeIdAndStatutEmploiNot(
                employeId,
                EmploiEmploye.StatutEmploi.TERMINE);
        if (emplois.isEmpty()) {
            throw new RuntimeException("No non-terminated emploi found for employe " + employeId);
        }
        if (emplois.size() > 1) {
            throw new RuntimeException("Multiple non-terminated emplois found for employe " + employeId + ". Please specify emploiEmployeId.");
        }
        return emplois.get(0);
    }
}
