package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.Horaire;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireDt;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireSpecial;
import com.rsoft.hurmanagement.hurmasterdata.entity.JourConge;
import com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeSalaireRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireDtRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireSpecialRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.JourCongeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PresenceEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PresenceAutoFillService {
    private static final int PAGE_SIZE = 200;

    private final EmployeRepository employeRepository;
    private final EmployeSalaireRepository employeSalaireRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final HoraireSpecialRepository horaireSpecialRepository;
    private final HoraireDtRepository horaireDtRepository;
    private final JourCongeRepository jourCongeRepository;
    private final PresenceEmployeRepository presenceRepository;
    private final PresenceEmployeService presenceEmployeService;

    @Transactional
    public Map<String, Object> autoFillPresences(String dateDebut,
                                                 String dateFin,
                                                 Long entrepriseId,
                                                 Long typeEmployeId,
                                                 Long uniteOrganisationnelleId,
                                                 Long gestionnaireId,
                                                 Long regimePaieId,
                                                 String username) {
        LocalDate debut = LocalDate.parse(dateDebut);
        LocalDate fin = LocalDate.parse(dateFin);
        if (fin.isBefore(debut)) {
            throw new RuntimeException("Invalid date range");
        }

        List<Employe> employes = fetchEmployes(typeEmployeId, uniteOrganisationnelleId, gestionnaireId, regimePaieId, entrepriseId);

        int totalCandidates = 0;
        int created = 0;
        int skippedExisting = 0;
        int skippedOff = 0;
        int skippedHoliday = 0;
        int skippedNoSchedule = 0;

        for (Employe employe : employes) {
            if (!"Y".equalsIgnoreCase(employe.getActif())) {
                continue;
            }
            if (!employeSalaireRepository.existsByEmployeIdAndActif(employe.getId(), "Y")) {
                continue;
            }
            LocalDate date = debut;
            while (!date.isAfter(fin)) {
                totalCandidates++;
                if (presenceRepository.existsByEmployeIdAndDateJour(employe.getId(), date)) {
                    skippedExisting++;
                    date = date.plusDays(1);
                    continue;
                }
                if (jourCongeRepository.existsByDateCongeAndActif(date, JourConge.Actif.Y)) {
                    skippedHoliday++;
                    date = date.plusDays(1);
                    continue;
                }
                EmploiEmploye emploi = resolveEmploiEmploye(employe.getId(), date);
                if (emploi == null || emploi.getStatutEmploi() != EmploiEmploye.StatutEmploi.ACTIF || isOffDay(emploi, date)) {
                    skippedOff++;
                    date = date.plusDays(1);
                    continue;
                }
                Plan plan = resolvePlan(employe.getId(), date, emploi);
                if (plan.plannedStart == null) {
                    skippedNoSchedule++;
                    date = date.plusDays(1);
                    continue;
                }

                PresenceEmploye presence = new PresenceEmploye();
                presence.setEmploye(employe);
                presence.setEntreprise(employe.getEntreprise());
                presence.setDateJour(date);
                presence.setHeureArrivee(plan.plannedStart);

                String depart = plan.autoClose != null ? plan.autoClose : plan.plannedEnd;
                if (depart != null) {
                    presence.setHeureDepart(depart);
                    presence.setDateDepart(plan.nightPlan ? date.plusDays(1) : date);
                } else {
                    presence.setDateDepart(null);
                }

                presence.setHoraireSpecial(plan.horaireSpecial ? "Y" : "N");
                presence.setNuitPlanifiee(plan.nightPlan ? "Y" : "N");
                presence.setHeureDebutPrevue(plan.plannedStart);
                presence.setHeureFinPrevue(plan.plannedEnd);
                presence.setIdHoraire(plan.horaireId);
                presence.setAutomatique("Y");
                presence.setFermetureManuelle("N");
                presence.setSourceSaisie(PresenceEmploye.SourceSaisie.API);

                presenceEmployeService.applyDerivedFields(presence);
                presence.setCreatedBy(username);
                presence.setCreatedOn(OffsetDateTime.now());
                presence.setRowscn(1);
                presenceRepository.save(presence);
                created++;

                date = date.plusDays(1);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalCandidates", totalCandidates);
        result.put("createdRows", created);
        result.put("skippedExisting", skippedExisting);
        result.put("skippedOff", skippedOff);
        result.put("skippedHoliday", skippedHoliday);
        result.put("skippedNoSchedule", skippedNoSchedule);
        result.put("message", "Created " + created + " presences");
        return result;
    }

    private List<Employe> fetchEmployes(Long typeEmployeId,
                                        Long uniteOrganisationnelleId,
                                        Long gestionnaireId,
                                        Long regimePaieId,
                                        Long entrepriseId) {
        List<Employe> results = new ArrayList<>();
        int page = 0;
        while (true) {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<Employe> result = employeRepository.searchAdvanced(
                    null,
                    null,
                    "CONTAINS",
                    null,
                    "CONTAINS",
                    typeEmployeId,
                    uniteOrganisationnelleId,
                    null,
                    null,
                    regimePaieId,
                    entrepriseId,
                    gestionnaireId,
                    pageable
            );
            results.addAll(result.getContent());
            if (!result.hasNext()) {
                break;
            }
            page++;
        }
        return results;
    }

    private EmploiEmploye resolveEmploiEmploye(Long employeId, LocalDate date) {
        List<EmploiEmploye> emplois = emploiEmployeRepository.findActiveForDate(employeId, date);
        return emplois.isEmpty() ? null : emplois.get(0);
    }

    private boolean isOffDay(EmploiEmploye emploi, LocalDate date) {
        int dayIndex = date.getDayOfWeek().getValue();
        return (emploi.getJourOff1() != null && emploi.getJourOff1() == dayIndex)
                || (emploi.getJourOff2() != null && emploi.getJourOff2() == dayIndex)
                || (emploi.getJourOff3() != null && emploi.getJourOff3() == dayIndex);
    }

    private Plan resolvePlan(Long employeId, LocalDate date, EmploiEmploye emploi) {
        Plan plan = new Plan();
        if (emploi == null) {
            return plan;
        }

        Horaire horaire = emploi.getHoraire();
        HoraireSpecial special = resolveHoraireSpecial(employeId, date);
        if (special != null) {
            plan.horaireSpecial = true;
            plan.nightPlan = special.getDateFin() != null && special.getDateFin().isAfter(special.getDateDebut());
            plan.plannedStart = special.getHeureDebut();
            plan.plannedEnd = special.getHeureFin();
            plan.horaireId = special.getId();
        } else if (horaire != null) {
            HoraireDt horaireDt = horaireDtRepository.findByHoraireIdAndJour(horaire.getId(), dayIndex(date));
            if (horaireDt != null) {
                boolean hasDay = isNotBlank(horaireDt.getHeureDebutJour()) && isNotBlank(horaireDt.getHeureFinJour());
                boolean hasNight = isNotBlank(horaireDt.getHeureDebutNuit()) && isNotBlank(horaireDt.getHeureFinNuit());
                if (!hasDay && hasNight) {
                    plan.nightPlan = true;
                    plan.plannedStart = horaireDt.getHeureDebutNuit();
                    plan.plannedEnd = horaireDt.getHeureFinNuit();
                } else if (hasDay) {
                    plan.nightPlan = false;
                    plan.plannedStart = horaireDt.getHeureDebutJour();
                    plan.plannedEnd = horaireDt.getHeureFinJour();
                }
                plan.horaireId = horaire.getId();
            }
        }

        if (horaire != null) {
            plan.autoClose = plan.nightPlan ? horaire.getHeureFermetureAutoNuit() : horaire.getHeureFermetureAutoJour();
        }
        return plan;
    }

    private HoraireSpecial resolveHoraireSpecial(Long employeId, LocalDate date) {
        List<HoraireSpecial> specials = horaireSpecialRepository.findActiveByEmployeIdAndDate(employeId, date);
        return specials.isEmpty() ? null : specials.get(0);
    }

    private int dayIndex(LocalDate date) {
        int val = date.getDayOfWeek().getValue();
        return val == 0 ? 7 : val;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static class Plan {
        boolean horaireSpecial;
        boolean nightPlan;
        String plannedStart;
        String plannedEnd;
        String autoClose;
        Long horaireId;
    }
}
