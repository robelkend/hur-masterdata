package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AbsenceEmployeService {

    private final AbsenceEmployeRepository repository;
    private final EmployeRepository employeRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final EmployeSalaireRepository employeSalaireRepository;
    private final BaremeSanctionRepository baremeSanctionRepository;
    private final PresenceEmployeRepository presenceEmployeRepository;
    private final JourCongeRepository jourCongeRepository;
    private final CongeEmployeRepository congeEmployeRepository;
    private final JdbcTemplate jdbcTemplate;
    private final HoraireDtRepository horaireDtRepository;

    @Transactional(readOnly = true)
    public Page<AbsenceEmployeDTO> findByFilters(String dateDebut,
                                                 String dateFin,
                                                 Long employeId,
                                                 String statut,
                                                 String typeEvenement,
                                                 Long entrepriseId,
                                                 Pageable pageable) {
        LocalDate debut = LocalDate.parse(dateDebut);
        LocalDate fin = LocalDate.parse(dateFin);
        AbsenceEmploye.StatutAbsence statutEnum = null;
        if (statut != null && !statut.trim().isEmpty()) {
            try {
                statutEnum = AbsenceEmploye.StatutAbsence.valueOf(statut);
            } catch (IllegalArgumentException ignored) {
            }
        }
        AbsenceEmploye.TypeEvenement typeEnum = null;
        if (typeEvenement != null && !typeEvenement.trim().isEmpty()) {
            try {
                typeEnum = AbsenceEmploye.TypeEvenement.valueOf(typeEvenement);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return repository.findByFilters(debut, fin, employeId, statutEnum, typeEnum, entrepriseId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public AbsenceEmployeDTO findById(Long id) {
        AbsenceEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AbsenceEmploye not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional
    public AbsenceEmployeDTO create(AbsenceEmployeCreateDTO dto, String username) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));

        AbsenceEmploye entity = new AbsenceEmploye();
        entity.setEmploye(employe);
        entity.setEntreprise(employe.getEntreprise());
        entity.setEmploiEmploye(resolveEmploiEmploye(dto.getEmployeId(), dto.getEmploiEmployeId()));
        entity.setTypeEvenement(AbsenceEmploye.TypeEvenement.valueOf(dto.getTypeEvenement()));
        entity.setDateJour(dto.getDateJour());
        entity.setHeureDebut(dto.getHeureDebut());
        entity.setHeureFin(dto.getHeureFin());
        entity.setUniteMesure(parseUniteMesure(dto.getUniteMesure()));
        entity.setQuantite(dto.getQuantite());
        entity.setJustificatif(dto.getJustificatif() != null ? dto.getJustificatif() : "N");
        entity.setMotif(dto.getMotif());
        entity.setSource(parseSource(dto.getSource()));
        entity.setStatut(AbsenceEmploye.StatutAbsence.BROUILLON);
        entity.setPayrollId(dto.getPayrollId());

        validateRetardTimes(entity);
        normalizeQuantite(entity);
        applyDeviseAndMontant(entity);

        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public AbsenceEmployeDTO update(Long id, AbsenceEmployeUpdateDTO dto, String username) {
        AbsenceEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AbsenceEmploye not found with id: " + id));
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        if (entity.getStatut() != AbsenceEmploye.StatutAbsence.BROUILLON) {
            throw new RuntimeException("absenceEmploye.error.cannotEdit");
        }

        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        entity.setEmploye(employe);
        entity.setEntreprise(employe.getEntreprise());
        entity.setEmploiEmploye(resolveEmploiEmploye(dto.getEmployeId(), dto.getEmploiEmployeId()));
        entity.setTypeEvenement(AbsenceEmploye.TypeEvenement.valueOf(dto.getTypeEvenement()));
        entity.setDateJour(dto.getDateJour());
        entity.setHeureDebut(dto.getHeureDebut());
        entity.setHeureFin(dto.getHeureFin());
        entity.setUniteMesure(parseUniteMesure(dto.getUniteMesure()));
        entity.setQuantite(dto.getQuantite());
        entity.setJustificatif(dto.getJustificatif() != null ? dto.getJustificatif() : "N");
        entity.setMotif(dto.getMotif());
        entity.setSource(parseSource(dto.getSource()));
        entity.setPayrollId(dto.getPayrollId());

        validateRetardTimes(entity);
        normalizeQuantite(entity);
        applyDeviseAndMontant(entity);

        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, Integer rowscn) {
        AbsenceEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AbsenceEmploye not found with id: " + id));
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        if (entity.getStatut() == AbsenceEmploye.StatutAbsence.VALIDE) {
            throw new RuntimeException("absenceEmploye.error.cannotDelete");
        }
        repository.delete(entity);
    }

    @Transactional
    public AbsenceGenerationResultDTO generateAbsences(AbsenceGenerationRequestDTO request, String username) {
        LocalDate dateDebut = LocalDate.parse(request.getDateDebut());
        LocalDate dateFin = LocalDate.parse(request.getDateFin());
        LocalDate today = LocalDate.now();
        if (dateFin.isAfter(today)) {
            dateFin = today;
        }
        if (dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("Date fin must be after or equal to date debut");
        }

        int deletedCount = repository.deleteSystemDraftsInRange(
                dateDebut, dateFin, request.getEmployeId(), request.getEntrepriseId());

        List<EmploiEmploye> emplois = emploiEmployeRepository.findEligibleForAbsence(
                request.getEntrepriseId(), request.getEmployeId());

        int generatedCount = 0;
        for (EmploiEmploye emploi : emplois) {
            Employe employe = emploi.getEmploye();
            for (LocalDate date = dateDebut; !date.isAfter(dateFin); date = date.plusDays(1)) {
                if (isOffDay(emploi, date)) {
                    continue;
                }
                if (jourCongeRepository.existsByDateCongeAndActif(date, JourConge.Actif.Y)) {
                    continue;
                }
                if (congeEmployeRepository.existsCongeForDate(employe.getId(), date)) {
                    continue;
                }

                PresenceEmploye presence = presenceEmployeRepository
                        .findTopByEmployeIdAndDateJourAndStatutPresenceOrderByIdDesc(
                                employe.getId(), date, PresenceEmploye.StatutPresence.VALIDE);

                if (presence == null) {
                    if (repository.existsForDate(employe.getId(), date, AbsenceEmploye.TypeEvenement.ABSENCE)) {
                        continue;
                    }
                    AbsenceEmploye absence = buildGeneratedAbsence(emploi, date, AbsenceEmploye.TypeEvenement.ABSENCE);
                    absence.setCreatedBy(username);
                    absence.setCreatedOn(OffsetDateTime.now());
                    absence.setRowscn(1);
                    repository.save(absence);
                    generatedCount++;
                    continue;
                }

                BigDecimal retardMinutes = computeRetardMinutes(presence.getHeureArrivee(), presence.getHeureDebutPrevue());
                if (retardMinutes.compareTo(BigDecimal.ZERO) > 0) {
                    if (repository.existsForDate(employe.getId(), date, AbsenceEmploye.TypeEvenement.RETARD)) {
                        continue;
                    }
                    AbsenceEmploye retard = buildGeneratedAbsence(emploi, date, AbsenceEmploye.TypeEvenement.RETARD);
                    retard.setHeureDebut(presence.getHeureDebutPrevue());
                    retard.setHeureFin(presence.getHeureArrivee());
                    retard.setUniteMesure(AbsenceEmploye.UniteMesure.MINUTE);
                    retard.setQuantite(retardMinutes);
                    applyDeviseAndMontant(retard);
                    retard.setCreatedBy(username);
                    retard.setCreatedOn(OffsetDateTime.now());
                    retard.setRowscn(1);
                    repository.save(retard);
                    generatedCount++;
                }
            }
        }

        return new AbsenceGenerationResultDTO(deletedCount, generatedCount);
    }

    @Transactional
    public AbsenceValidationResultDTO validateAbsences(AbsenceGenerationRequestDTO request, String username) {
        LocalDate dateDebut = LocalDate.parse(request.getDateDebut());
        LocalDate dateFin = LocalDate.parse(request.getDateFin());
        if (dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("Date fin must be after or equal to date debut");
        }
        int validatedCount = repository.validateDraftsInRange(
                dateDebut, dateFin, request.getEmployeId(), request.getEntrepriseId(), username, OffsetDateTime.now());
        return new AbsenceValidationResultDTO(validatedCount);
    }

    @Transactional
    public AbsenceAnnulationResultDTO cancelAbsences(AbsenceGenerationRequestDTO request, String username) {
        LocalDate dateDebut = LocalDate.parse(request.getDateDebut());
        LocalDate dateFin = LocalDate.parse(request.getDateFin());
        if (dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("Date fin must be after or equal to date debut");
        }
        int cancelledCount = repository.cancelValidatedInRange(
                dateDebut, dateFin, request.getEmployeId(), request.getEntrepriseId(), username, OffsetDateTime.now());
        return new AbsenceAnnulationResultDTO(cancelledCount);
    }

    private AbsenceEmploye buildGeneratedAbsence(EmploiEmploye emploi, LocalDate date, AbsenceEmploye.TypeEvenement type) {
        AbsenceEmploye entity = new AbsenceEmploye();
        entity.setEmploye(emploi.getEmploye());
        entity.setEntreprise(emploi.getEmploye().getEntreprise());
        entity.setEmploiEmploye(emploi);
        entity.setTypeEvenement(type);
        entity.setDateJour(date);
        entity.setSource(AbsenceEmploye.SourceAbsence.SYSTEME);
        entity.setStatut(AbsenceEmploye.StatutAbsence.BROUILLON);
        entity.setJustificatif("N");
        entity.setUniteMesure(type == AbsenceEmploye.TypeEvenement.ABSENCE
                ? AbsenceEmploye.UniteMesure.JOUR
                : AbsenceEmploye.UniteMesure.MINUTE);
        entity.setQuantite(type == AbsenceEmploye.TypeEvenement.ABSENCE ? BigDecimal.ONE : BigDecimal.ZERO);
        applyDeviseAndMontant(entity);
        return entity;
    }

    private void validateRetardTimes(AbsenceEmploye entity) {
        if (entity.getTypeEvenement() != AbsenceEmploye.TypeEvenement.RETARD) {
            return;
        }
        if (entity.getHeureDebut() == null || entity.getHeureFin() == null) {
            throw new RuntimeException("Heure debut and heure fin are required for RETARD");
        }
        try {
            LocalTime start = LocalTime.parse(entity.getHeureDebut());
            LocalTime end = LocalTime.parse(entity.getHeureFin());
            if (!end.isAfter(start)) {
                throw new RuntimeException("Heure fin must be > heure debut");
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Invalid time format");
        }
    }

    private void normalizeQuantite(AbsenceEmploye entity) {
        if (entity.getTypeEvenement() == AbsenceEmploye.TypeEvenement.ABSENCE) {
            entity.setUniteMesure(AbsenceEmploye.UniteMesure.JOUR);
            if (entity.getQuantite() == null) {
                entity.setQuantite(BigDecimal.ONE);
            }
            return;
        }
        if (entity.getUniteMesure() == null) {
            entity.setUniteMesure(AbsenceEmploye.UniteMesure.MINUTE);
        }
        if (entity.getQuantite() == null && entity.getHeureDebut() != null && entity.getHeureFin() != null) {
            BigDecimal minutes = computeRetardMinutes(entity.getHeureFin(), entity.getHeureDebut());
            entity.setQuantite(minutes);
        }
    }

    private AbsenceEmploye.UniteMesure parseUniteMesure(String unite) {
        if (unite == null || unite.trim().isEmpty()) {
            return null;
        }
        try {
            return AbsenceEmploye.UniteMesure.valueOf(unite);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private AbsenceEmploye.SourceAbsence parseSource(String source) {
        if (source == null || source.trim().isEmpty()) {
            return AbsenceEmploye.SourceAbsence.MANUEL;
        }
        try {
            return AbsenceEmploye.SourceAbsence.valueOf(source);
        } catch (IllegalArgumentException ex) {
            return AbsenceEmploye.SourceAbsence.MANUEL;
        }
    }

    private BigDecimal computeRetardMinutes(String actual, String planned) {
        if (actual == null || planned == null) {
            return BigDecimal.ZERO;
        }
        try {
            LocalTime a = LocalTime.parse(actual);
            LocalTime p = LocalTime.parse(planned);
            long minutes = Duration.between(p, a).toMinutes();
            if (minutes < 0) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(minutes);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private void applyDeviseAndMontant(AbsenceEmploye entity) {
        EmploiEmploye emploi = entity.getEmploiEmploye();
        Long employeId = entity.getEmploye() != null ? entity.getEmploye().getId() : null;
        if (employeId == null) {
            return;
        }
        EmployeSalaire salaire = employeSalaireRepository
                .findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(employeId, "Y")
                .orElse(null);
        RegimePaie regimePaie = salaire != null ? salaire.getRegimePaie() : null;
        if (regimePaie != null) {
            entity.setDevise(regimePaie.getDevise());
        }
        BigDecimal montant = computeMontantEquivalent(emploi, salaire, regimePaie,
                entity.getTypeEvenement(), entity.getUniteMesure(), entity.getQuantite(), entity.getDateJour());
        entity.setMontantEquivalent(montant);
    }

    private BigDecimal computeMontantEquivalent(EmploiEmploye emploi,
                                                EmployeSalaire salaire,
                                                RegimePaie regimePaie,
                                                AbsenceEmploye.TypeEvenement type,
                                                AbsenceEmploye.UniteMesure unite,
                                                BigDecimal quantite,
                                                LocalDate dateJour) {
        if (type == null || unite == null || quantite == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal hoursPerDay = type == AbsenceEmploye.TypeEvenement.RETARD
                ? computeHoursPerDayForRetard(emploi)
                : computeHoursPerDay(emploi, dateJour);
        SalaryAmounts amounts = computeSalaryAmounts(salaire, regimePaie, hoursPerDay);
        if (emploi != null && emploi.getTypeEmploye() != null) {
            if (tableExists("bareme_sanction")) {
                try {
                    List<BaremeSanction> baremes = baremeSanctionRepository.findByTypeEmployeId(emploi.getTypeEmploye().getId());
                    Optional<BaremeSanction> rule = selectBaremeRule(baremes, type, unite, quantite);
                    if (rule.isPresent()) {
                        return computePenaltyAmount(
                                amounts,
                                BigDecimal.valueOf(rule.get().getPenaliteMinutes()),
                                rule.get().getUnitePenalite());
                    }
                } catch (DataAccessException ex) {
                    // Fall back to salary-based calculation if lookup fails
                }
            }
        }
        return computePenaltyAmount(amounts, quantite, toUnitePenalite(unite));
    }

    private Optional<BaremeSanction> selectBaremeRule(List<BaremeSanction> baremes,
                                                      AbsenceEmploye.TypeEvenement type,
                                                      AbsenceEmploye.UniteMesure unite,
                                                      BigDecimal quantite) {
        if (baremes == null || baremes.isEmpty()) {
            return Optional.empty();
        }
        return baremes.stream()
                .filter(b -> b.getInfractionType().name().equalsIgnoreCase(type.name()))
                .filter(b -> b.getUniteInfraction().name().equalsIgnoreCase(unite.name()))
                .filter(b -> {
                    int min = b.getSeuilMin() != null ? b.getSeuilMin() : 0;
                    Integer max = b.getSeuilMax();
                    int qty = quantite.setScale(0, RoundingMode.FLOOR).intValue();
                    return qty >= min && (max == null || qty <= max);
                })
                .sorted(Comparator.comparing(BaremeSanction::getSeuilMin))
                .findFirst();
    }

    private BigDecimal computePenaltyAmount(SalaryAmounts amounts,
                                            BigDecimal value,
                                            BaremeSanction.UnitePenalite unitePenalite) {
        if (amounts == null || value == null || unitePenalite == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal hourly = amounts.hourly;
        BigDecimal daily = amounts.daily;
        switch (unitePenalite) {
            case MINUTE:
                return hourly.divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP)
                        .multiply(value)
                        .setScale(2, RoundingMode.HALF_UP);
            case HEURE:
                return hourly.multiply(value).setScale(2, RoundingMode.HALF_UP);
            case JOUR:
                return daily.multiply(value).setScale(2, RoundingMode.HALF_UP);
            default:
                return BigDecimal.ZERO;
        }
    }

    private BaremeSanction.UnitePenalite toUnitePenalite(AbsenceEmploye.UniteMesure unite) {
        switch (unite) {
            case MINUTE:
                return BaremeSanction.UnitePenalite.MINUTE;
            case HEURE:
                return BaremeSanction.UnitePenalite.HEURE;
            case JOUR:
                return BaremeSanction.UnitePenalite.JOUR;
            default:
                return BaremeSanction.UnitePenalite.MINUTE;
        }
    }

    private SalaryAmounts computeSalaryAmounts(EmployeSalaire salaire, RegimePaie regimePaie, BigDecimal hoursPerDay) {
        if (salaire == null || salaire.getMontant() == null) {
            return SalaryAmounts.zero();
        }
        BigDecimal montant = salaire.getMontant();
        RegimePaie.Periodicite periodicite = regimePaie != null ? regimePaie.getPeriodicite() : null;
        RegimePaie.ModeRemuneration mode = regimePaie != null ? regimePaie.getModeRemuneration() : null;

        BigDecimal hours = hoursPerDay != null && hoursPerDay.compareTo(BigDecimal.ZERO) > 0
                ? hoursPerDay
                : BigDecimal.valueOf(8);

        BigDecimal daily = BigDecimal.ZERO;
        BigDecimal hourly = BigDecimal.ZERO;

        if (mode == RegimePaie.ModeRemuneration.HORAIRE) {
            hourly = montant;
            daily = hourly.multiply(hours);
        } else if (mode == RegimePaie.ModeRemuneration.JOURNALIER
                || mode == RegimePaie.ModeRemuneration.PIECE
                || mode == RegimePaie.ModeRemuneration.PIECE_FIXE) {
            daily = montant;
            hourly = daily.divide(hours, 6, RoundingMode.HALF_UP);
        } else {
            BigDecimal periodDays = periodDaysFromPeriodicite(periodicite);
            daily = periodDays.compareTo(BigDecimal.ZERO) > 0
                    ? montant.divide(periodDays, 6, RoundingMode.HALF_UP)
                    : montant;
            hourly = daily.divide(hours, 6, RoundingMode.HALF_UP);
        }

        BigDecimal weekly = daily.multiply(BigDecimal.valueOf(7));
        BigDecimal monthly = daily.multiply(BigDecimal.valueOf(30));
        BigDecimal yearly = daily.multiply(BigDecimal.valueOf(365));

        return new SalaryAmounts(montant, hourly, daily, weekly, monthly, yearly);
    }

    private BigDecimal periodDaysFromPeriodicite(RegimePaie.Periodicite periodicite) {
        if (periodicite == null) {
            return BigDecimal.ZERO;
        }
        switch (periodicite) {
            case JOURNALIER:
                return BigDecimal.valueOf(1);
            case HEBDO:
                return BigDecimal.valueOf(7);
            case QUINZAINE:
                return BigDecimal.valueOf(14);
            case QUINZOMADAIRE:
                return BigDecimal.valueOf(15);
            case MENSUEL:
                return BigDecimal.valueOf(30);
            case TRIMESTRIEL:
                return BigDecimal.valueOf(90);
            case SEMESTRIEL:
                return BigDecimal.valueOf(180);
            case ANNUEL:
                return BigDecimal.valueOf(365);
            default:
                return BigDecimal.ZERO;
        }
    }

    private BigDecimal computeHoursPerDay(EmploiEmploye emploi, LocalDate dateJour) {
        if (emploi == null || emploi.getHoraire() == null) {
            return BigDecimal.valueOf(8);
        }
        Horaire horaire = emploi.getHoraire();
        if (horaire.getNbHeuresRef() != null && horaire.getNbHeuresRef().compareTo(BigDecimal.ZERO) > 0) {
            return horaire.getNbHeuresRef();
        }
        if (dateJour != null) {
            int dayIndex = dateJour.getDayOfWeek().getValue();
            HoraireDt horaireDt = horaireDtRepository.findByHoraireIdAndJour(horaire.getId(), dayIndex);
            if (horaireDt != null) {
                BigDecimal dayHours = computeHoursFromTimes(horaireDt.getHeureDebutJour(), horaireDt.getHeureFinJour(), false);
                BigDecimal nightHours = computeHoursFromTimes(horaireDt.getHeureDebutNuit(), horaireDt.getHeureFinNuit(), true);
                BigDecimal total = dayHours.add(nightHours);
                if (total.compareTo(BigDecimal.ZERO) > 0) {
                    return total;
                }
            }
        }
        BigDecimal fallback = computeHoursFromTimes(horaire.getHeureDebut(), horaire.getHeureFin(), false);
        if (fallback.compareTo(BigDecimal.ZERO) > 0) {
            return fallback;
        }
        return BigDecimal.valueOf(8);
    }

    private BigDecimal computeHoursPerDayForRetard(EmploiEmploye emploi) {
        if (emploi == null || emploi.getHoraire() == null) {
            return BigDecimal.valueOf(8);
        }
        Horaire horaire = emploi.getHoraire();
        if (horaire.getNbHeuresRef() != null && horaire.getNbHeuresRef().compareTo(BigDecimal.ZERO) > 0) {
            return horaire.getNbHeuresRef();
        }
        return BigDecimal.valueOf(8);
    }

    private BigDecimal computeHoursFromTimes(String start, String end, boolean allowOvernight) {
        if (start == null || end == null || start.isBlank() || end.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            LocalTime startTime = LocalTime.parse(start);
            LocalTime endTime = LocalTime.parse(end);
            long minutes = Duration.between(startTime, endTime).toMinutes();
            if (minutes < 0 && allowOvernight) {
                minutes += 24 * 60;
            }
            if (minutes <= 0) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(minutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean tableExists(String tableName) {
        try {
            String reg = jdbcTemplate.queryForObject(
                    "SELECT to_regclass(?)",
                    String.class,
                    "public." + tableName);
            return reg != null;
        } catch (Exception ex) {
            return false;
        }
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
            if (emploi.getHoraire() == null || !"Y".equalsIgnoreCase(emploi.getHoraire().getGenererAbsence())) {
                throw new RuntimeException("EmploiEmploye has no eligible horaire for absence generation.");
            }
            return emploi;
        }
        List<EmploiEmploye> emplois = emploiEmployeRepository.findEligibleForAbsence(null, employeId);
        if (emplois.isEmpty()) {
            throw new RuntimeException("No eligible emploi found for employe " + employeId);
        }
        if (emplois.size() > 1) {
            throw new RuntimeException("Multiple eligible emplois found for employe " + employeId + ". Please specify emploiEmployeId.");
        }
        return emplois.get(0);
    }

    private boolean isOffDay(EmploiEmploye emploi, LocalDate date) {
        if (emploi == null) {
            return false;
        }
        int dayIndex = date.getDayOfWeek().getValue();
        return (emploi.getJourOff1() != null && emploi.getJourOff1() == dayIndex)
                || (emploi.getJourOff2() != null && emploi.getJourOff2() == dayIndex)
                || (emploi.getJourOff3() != null && emploi.getJourOff3() == dayIndex);
    }

    private AbsenceEmployeDTO toDTO(AbsenceEmploye entity) {
        AbsenceEmployeDTO dto = new AbsenceEmployeDTO();
        dto.setId(entity.getId());
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        if (entity.getEmploye() != null) {
            dto.setEmployeId(entity.getEmploye().getId());
            dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
            dto.setEmployeNom(entity.getEmploye().getNom());
            dto.setEmployePrenom(entity.getEmploye().getPrenom());
        }
        if (entity.getEmploiEmploye() != null) {
            dto.setEmploiEmployeId(entity.getEmploiEmploye().getId());
        }
        dto.setTypeEvenement(entity.getTypeEvenement() != null ? entity.getTypeEvenement().name() : null);
        dto.setDateJour(entity.getDateJour());
        dto.setHeureDebut(entity.getHeureDebut());
        dto.setHeureFin(entity.getHeureFin());
        dto.setUniteMesure(entity.getUniteMesure() != null ? entity.getUniteMesure().name() : null);
        dto.setQuantite(entity.getQuantite());
        if (entity.getDevise() != null) {
            dto.setDeviseId(entity.getDevise().getId());
            dto.setDeviseCode(entity.getDevise().getCodeDevise());
            dto.setDeviseDescription(entity.getDevise().getDescription());
        }
        dto.setMontantEquivalent(entity.getMontantEquivalent());
        dto.setPayrollId(entity.getPayrollId());
        dto.setJustificatif(entity.getJustificatif());
        dto.setMotif(entity.getMotif());
        dto.setStatut(entity.getStatut() != null ? entity.getStatut().name() : null);
        dto.setSource(entity.getSource() != null ? entity.getSource().name() : null);
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private static class SalaryAmounts {
        private final BigDecimal montant;
        private final BigDecimal hourly;
        private final BigDecimal daily;
        private final BigDecimal weekly;
        private final BigDecimal monthly;
        private final BigDecimal yearly;

        private SalaryAmounts(BigDecimal montant,
                              BigDecimal hourly,
                              BigDecimal daily,
                              BigDecimal weekly,
                              BigDecimal monthly,
                              BigDecimal yearly) {
            this.montant = montant;
            this.hourly = hourly;
            this.daily = daily;
            this.weekly = weekly;
            this.monthly = monthly;
            this.yearly = yearly;
        }

        private static SalaryAmounts zero() {
            BigDecimal zero = BigDecimal.ZERO;
            return new SalaryAmounts(zero, zero, zero, zero, zero, zero);
        }
    }
}
