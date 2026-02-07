package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Horaire;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireDt;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireSpecial;
import com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireDtRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireSpecialRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PresenceEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PresenceRearrangeService {
    private static final int MAX_CLOSE_DIFF_MINUTES = 4 * 60;

    private final PresenceEmployeRepository presenceRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final HoraireSpecialRepository horaireSpecialRepository;
    private final HoraireDtRepository horaireDtRepository;
    private final PresenceEmployeService presenceEmployeService;

    @Transactional
    public Map<String, Object> closeAndRearrange(String dateDebut,
                                                 String dateFin,
                                                 Long employeId,
                                                 Long entrepriseId,
                                                 String username) {
        LocalDate debut = LocalDate.parse(dateDebut);
        LocalDate fin = LocalDate.parse(dateFin);
        if (fin.isBefore(debut)) {
            throw new RuntimeException("Invalid date range");
        }
        LocalDate today = LocalDate.now();
        List<PresenceEmploye> presences = presenceRepository.findForRearrange(debut, fin, employeId, entrepriseId);

        int total = 0;
        int closed = 0;
        int rearranged = 0;
        int skippedToday = 0;

        for (PresenceEmploye presence : presences) {
            total++;
            if (presence.getDateJour() != null && presence.getDateJour().equals(today)) {
                skippedToday++;
                continue;
            }
            PlanInfo plan = resolvePlan(presence);
            boolean updated = false;
            if (closeIfNeeded(presence, plan)) {
                closed++;
                updated = true;
            }
            if (rearrangeIfNeeded(presence, plan)) {
                rearranged++;
                updated = true;
            }
            if (updated) {
                presence.setAutomatique("Y");
                presence.setFermetureManuelle("N");
                presenceEmployeService.applyDerivedFields(presence);
                presence.setUpdatedBy(username);
                presence.setUpdatedOn(OffsetDateTime.now());
                presence.setRowscn(presence.getRowscn() != null ? presence.getRowscn() + 1 : 1);
                presenceRepository.save(presence);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalRows", total);
        result.put("closedRows", closed);
        result.put("rearrangedRows", rearranged);
        result.put("skippedToday", skippedToday);
        result.put("message", "Closed " + closed + " and rearranged " + rearranged + " presences");
        return result;
    }

    private boolean closeIfNeeded(PresenceEmploye presence, PlanInfo plan) {
        String arrivee = trimToNull(presence.getHeureArrivee());
        String depart = trimToNull(presence.getHeureDepart());
        if (arrivee != null && depart != null) {
            return false;
        }
        String onlyTime = arrivee != null ? arrivee : depart;
        if (onlyTime == null) {
            return false;
        }
        String autoClose = plan.autoClose;
        if (autoClose == null) {
            return false;
        }

        LocalTime time = parseTime(onlyTime);
        LocalTime close = parseTime(autoClose);
        if (time == null || close == null) {
            return false;
        }
        boolean nearClose = minuteDiff(time, close) <= MAX_CLOSE_DIFF_MINUTES;

        if (nearClose) {
            presence.setHeureDepart(formatTime(time));
            String plannedStart = trimToNull(plan.plannedStart);
            presence.setHeureArrivee(plannedStart != null ? plannedStart : autoClose);
        } else {
            presence.setHeureArrivee(formatTime(time));
            presence.setHeureDepart(autoClose);
        }

        if (plan.nightPlan) {
            presence.setDateDepart(presence.getDateJour() != null ? presence.getDateJour().plusDays(1) : null);
        } else {
            presence.setDateDepart(presence.getDateJour());
        }
        return true;
    }

    private boolean rearrangeIfNeeded(PresenceEmploye presence, PlanInfo plan) {
        if (presence.getDateJour() == null || presence.getDateDepart() == null) {
            return false;
        }
        String arrivee = trimToNull(presence.getHeureArrivee());
        String depart = trimToNull(presence.getHeureDepart());
        if (arrivee == null || depart == null) {
            return false;
        }
        boolean isDayPresence = presence.getDateDepart().equals(presence.getDateJour());
        boolean isNightPresence = presence.getDateDepart().isAfter(presence.getDateJour());

        if (plan.hasNightPlan && isDayPresence) {
            presence.setDateDepart(presence.getDateJour().plusDays(1));
            presence.setHeureArrivee(depart);
            presence.setHeureDepart(arrivee);
            return true;
        }
        if (!plan.hasNightPlan && plan.hasDayPlan && isNightPresence) {
            presence.setDateDepart(presence.getDateJour());
            presence.setHeureArrivee(depart);
            presence.setHeureDepart(arrivee);
            return true;
        }
        return false;
    }

    private PlanInfo resolvePlan(PresenceEmploye presence) {
        PlanInfo plan = new PlanInfo();
        if (presence.getEmploye() == null || presence.getDateJour() == null) {
            return plan;
        }
        Long employeId = presence.getEmploye().getId();
        LocalDate date = presence.getDateJour();

        EmploiEmploye emploi = resolveEmploiEmploye(employeId, date);
        Horaire horaire = emploi != null ? emploi.getHoraire() : null;

        HoraireSpecial special = resolveHoraireSpecial(employeId, date);
        if (special != null) {
            plan.horaireSpecial = true;
            plan.hasNightPlan = special.getDateFin() != null && special.getDateFin().isAfter(special.getDateDebut());
            plan.hasDayPlan = !plan.hasNightPlan;
            plan.nightPlan = plan.hasNightPlan;
            plan.plannedStart = special.getHeureDebut();
            plan.plannedEnd = special.getHeureFin();
        } else if (horaire != null) {
            HoraireDt horaireDt = horaireDtRepository.findByHoraireIdAndJour(horaire.getId(), dayIndex(date));
            if (horaireDt != null) {
                boolean hasDay = isNotBlank(horaireDt.getHeureDebutJour()) && isNotBlank(horaireDt.getHeureFinJour());
                boolean hasNight = isNotBlank(horaireDt.getHeureDebutNuit()) && isNotBlank(horaireDt.getHeureFinNuit());
                plan.hasDayPlan = hasDay;
                plan.hasNightPlan = hasNight;
                plan.nightPlan = resolveNightPlan(presence, horaireDt, hasDay, hasNight);
                if (plan.nightPlan) {
                    plan.plannedStart = horaireDt.getHeureDebutNuit();
                    plan.plannedEnd = horaireDt.getHeureFinNuit();
                } else if (hasDay) {
                    plan.plannedStart = horaireDt.getHeureDebutJour();
                    plan.plannedEnd = horaireDt.getHeureFinJour();
                }
            }
        }

        if (horaire != null) {
            plan.autoClose = plan.nightPlan ? horaire.getHeureFermetureAutoNuit() : horaire.getHeureFermetureAutoJour();
        }
        return plan;
    }

    private boolean resolveNightPlan(PresenceEmploye presence, HoraireDt horaireDt, boolean hasDay, boolean hasNight) {
        if (hasNight && !hasDay) {
            return true;
        }
        if (!hasNight) {
            return false;
        }
        String timeValue = trimToNull(presence.getHeureArrivee());
        if (timeValue == null) {
            timeValue = trimToNull(presence.getHeureDepart());
        }
        if (timeValue == null) {
            return false;
        }
        LocalTime time = parseTime(timeValue);
        if (time == null) {
            return false;
        }
        LocalTime start = parseTime(horaireDt.getHeureDebutNuit());
        LocalTime end = parseTime(horaireDt.getHeureFinNuit());
        if (start == null || end == null) {
            return false;
        }
        if (end.isBefore(start)) {
            return !time.isBefore(start) || !time.isAfter(end);
        }
        return !time.isBefore(start) && !time.isAfter(end);
    }

    private EmploiEmploye resolveEmploiEmploye(Long employeId, LocalDate date) {
        List<EmploiEmploye> emplois = emploiEmployeRepository.findActiveForDate(employeId, date);
        return emplois.isEmpty() ? null : emplois.get(0);
    }

    private HoraireSpecial resolveHoraireSpecial(Long employeId, LocalDate date) {
        List<HoraireSpecial> specials = horaireSpecialRepository.findActiveByEmployeIdAndDate(employeId, date);
        return specials.isEmpty() ? null : specials.get(0);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatTime(LocalTime time) {
        return time != null ? time.toString() : null;
    }

    private int minuteDiff(LocalTime left, LocalTime right) {
        int diff = (int) Math.abs(Duration.between(left, right).toMinutes());
        return Math.min(diff, 24 * 60 - diff);
    }

    private int dayIndex(LocalDate date) {
        int val = date.getDayOfWeek().getValue();
        return val == 0 ? 7 : val;
    }

    private static class PlanInfo {
        boolean horaireSpecial;
        boolean hasDayPlan;
        boolean hasNightPlan;
        boolean nightPlan;
        String plannedStart;
        String plannedEnd;
        String autoClose;
    }
}
