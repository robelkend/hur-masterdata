package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Horaire;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireDt;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireSpecial;
import com.rsoft.hurmanagement.hurmasterdata.entity.PointageBrut;
import com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireDtRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireSpecialRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PointageBrutRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PresenceEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PresenceMergeService {

    private final PresenceEmployeRepository presenceRepository;
    private final PointageBrutRepository pointageBrutRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final HoraireSpecialRepository horaireSpecialRepository;
    private final HoraireDtRepository horaireDtRepository;
    private final PresenceEmployeService presenceEmployeService;

    @Transactional
    public int mergeForRange(LocalDate dateDebut,
                             LocalDate dateFin,
                             Long employeId,
                             Long entrepriseId,
                             String username) {
        List<PresenceEmploye> all = presenceRepository.findForRearrange(dateDebut, dateFin.plusDays(1), employeId, entrepriseId);
        Map<Long, Map<LocalDate, List<PresenceEmploye>>> byEmployeAndDay = new HashMap<>();
        for (PresenceEmploye presence : all) {
            if (presence.getEmploye() == null || presence.getEmploye().getId() == null || presence.getDateJour() == null) {
                continue;
            }
            byEmployeAndDay
                    .computeIfAbsent(presence.getEmploye().getId(), k -> new HashMap<>())
                    .computeIfAbsent(presence.getDateJour(), k -> new ArrayList<>())
                    .add(presence);
        }

        int mergedCount = 0;
        Set<Long> consumedPresenceIds = new HashSet<>();
        for (Map<LocalDate, List<PresenceEmploye>> byDay : byEmployeAndDay.values()) {
            List<LocalDate> orderedDays = byDay.keySet().stream().sorted().toList();
            for (LocalDate dateJour : orderedDays) {
                List<PresenceEmploye> dayPresences = byDay.getOrDefault(dateJour, List.of());
                dayPresences = dayPresences.stream()
                        .filter(p -> p.getId() != null && !consumedPresenceIds.contains(p.getId()))
                        .toList();
                if (dayPresences.size() < 2) {
                    continue;
                }

                MergePlanContext plan = resolvePlanContext(dayPresences.get(0).getEmploye().getId(), dateJour);
                MergeResult mergeResult = plan.hasNightPlan
                        ? mergeNight(dateJour, dayPresences, byDay.getOrDefault(dateJour.plusDays(1), List.of()), plan, username)
                        : mergeDay(dayPresences, plan, username);

                if (mergeResult == null || mergeResult.keeper == null || mergeResult.mergedIds.size() < 2) {
                    continue;
                }

                for (Long id : mergeResult.mergedIds) {
                    consumedPresenceIds.add(id);
                }
                mergedCount++;
            }
        }
        return mergedCount;
    }

    private MergeResult mergeDay(List<PresenceEmploye> rawCandidates, MergePlanContext plan, String username) {
        List<PresenceEmploye> candidates = normalizeAndSort(rawCandidates);
        if (candidates.size() < 2) {
            return null;
        }

        List<PresenceInterval> intervals = toIntervals(candidates, plan, false, null);
        if (intervals.size() < 2) {
            return null;
        }

        PresenceEmploye keeper = intervals.get(0).presence;
        PresenceInterval first = intervals.get(0);
        LocalDateTime end = resolveDayEnd(intervals, plan, first.start.toLocalDate());
        if (end == null) {
            return null;
        }

        return applyMerge(keeper, intervals, first.start, end, end.toLocalDate(), username);
    }

    private MergeResult mergeNight(LocalDate dateJour,
                                   List<PresenceEmploye> dayPresences,
                                   List<PresenceEmploye> nextDayPresences,
                                   MergePlanContext plan,
                                   String username) {
        LocalDateTime nightWindowStart = computeNightWindowStart(dateJour, plan);
        LocalDateTime nightWindowEnd = computeNightWindowEnd(dateJour, plan);

        boolean hasArrivalInNightWindow = false;
        for (PresenceEmploye presence : dayPresences) {
            LocalDateTime start = resolveStart(presence);
            if (start != null && !start.isBefore(nightWindowStart) && !start.isAfter(nightWindowEnd)) {
                hasArrivalInNightWindow = true;
                break;
            }
        }

        if (!hasArrivalInNightWindow) {
            return mergeDay(dayPresences, plan.asDayPlan(), username);
        }

        LocalTime nextDayLimit = parseTime(plan.nightClosingLimit);
        List<PresenceEmploye> crossDayCandidates = new ArrayList<>(dayPresences);
        for (PresenceEmploye presence : nextDayPresences) {
            LocalTime arrivalTime = parseTime(trimToNull(presence.getHeureArrivee()));
            if (arrivalTime == null || nextDayLimit == null) {
                continue;
            }
            if (!arrivalTime.isBefore(LocalTime.MIDNIGHT) && !arrivalTime.isAfter(nextDayLimit)) {
                crossDayCandidates.add(presence);
            }
        }

        List<PresenceEmploye> candidates = normalizeAndSort(crossDayCandidates);
        if (candidates.size() < 2) {
            return null;
        }

        List<PresenceInterval> intervals = toIntervals(candidates, plan, true, dateJour.plusDays(1));
        if (intervals.size() < 2) {
            return null;
        }

        PresenceInterval first = intervals.get(0);
        PresenceInterval last = intervals.get(intervals.size() - 1);
        PresenceEmploye keeper = first.presence;
        LocalDate dateDepart = dateJour.plusDays(1);
        LocalDateTime end = LocalDateTime.of(dateDepart, last.end.toLocalTime());

        return applyMerge(keeper, intervals, first.start, end, dateDepart, username);
    }

    private MergeResult applyMerge(PresenceEmploye keeper,
                                   List<PresenceInterval> intervals,
                                   LocalDateTime start,
                                   LocalDateTime end,
                                   LocalDate dateDepart,
                                   String username) {
        keeper.setDateJour(start.toLocalDate());
        keeper.setHeureArrivee(formatTime(start.toLocalTime()));
        keeper.setDateDepart(dateDepart);
        keeper.setHeureDepart(formatTime(end.toLocalTime()));
        keeper.setCumulPauseMin(computePauseMinutes(intervals));
        keeper.setAutomatique("Y");
        keeper.setFermetureManuelle("N");
        arrangerPresenceSwapIfNeeded(keeper);
        presenceEmployeService.applyDerivedFields(keeper);
        keeper.setUpdatedBy(username);
        keeper.setUpdatedOn(OffsetDateTime.now());
        keeper.setRowscn(keeper.getRowscn() != null ? keeper.getRowscn() + 1 : 1);
        presenceRepository.save(keeper);

        List<Long> mergedIds = new ArrayList<>();
        for (PresenceInterval interval : intervals) {
            PresenceEmploye source = interval.presence;
            if (source.getId() == null) {
                continue;
            }
            mergedIds.add(source.getId());
            updatePointagesBrutsNoPresenceSafely(source.getId(), keeper);
            if (!source.getId().equals(keeper.getId())) {
                presenceRepository.delete(source);
            }
        }
        return new MergeResult(keeper, mergedIds);
    }

    private List<PresenceEmploye> normalizeAndSort(List<PresenceEmploye> candidates) {
        List<PresenceEmploye> sorted = new ArrayList<>(candidates);
        sorted.sort(Comparator.comparing(this::resolveStartSafe));
        for (PresenceEmploye presence : sorted) {
            arrangerPresenceSwapIfNeeded(presence);
        }
        sorted.sort(Comparator.comparing(this::resolveStartSafe));
        return sorted;
    }

    private LocalDateTime resolveStartSafe(PresenceEmploye presence) {
        LocalDateTime start = resolveStart(presence);
        return start != null ? start : LocalDateTime.MAX;
    }

    private List<PresenceInterval> toIntervals(List<PresenceEmploye> candidates,
                                               MergePlanContext plan,
                                               boolean nightMode,
                                               LocalDate forceDepartureDate) {
        List<PresenceInterval> intervals = new ArrayList<>();
        for (PresenceEmploye presence : candidates) {
            LocalDateTime start = resolveStart(presence);
            if (start == null) {
                continue;
            }
            LocalDateTime end = resolveEnd(presence, plan, nightMode, forceDepartureDate);
            if (end == null || end.isBefore(start)) {
                continue;
            }
            intervals.add(new PresenceInterval(presence, start, end));
        }
        intervals.sort(Comparator.comparing(i -> i.start));
        return intervals;
    }

    private int computePauseMinutes(List<PresenceInterval> intervals) {
        int pause = 0;
        for (int i = 1; i < intervals.size(); i++) {
            LocalDateTime previousEnd = intervals.get(i - 1).end;
            LocalDateTime currentStart = intervals.get(i).start;
            long gap = Duration.between(previousEnd, currentStart).toMinutes();
            if (gap > 0) {
                pause += (int) gap;
            }
        }
        return pause;
    }

    private LocalDateTime resolveDayEnd(List<PresenceInterval> intervals, MergePlanContext plan, LocalDate dateJour) {
        LocalDateTime maxEnd = null;
        LocalTime maxClosedDeparture = null;
        for (PresenceInterval interval : intervals) {
            String depart = trimToNull(interval.presence.getHeureDepart());
            if (depart == null) {
                continue;
            }
            LocalTime depTime = parseTime(depart);
            if (depTime == null) {
                continue;
            }
            if (maxClosedDeparture == null || depTime.isAfter(maxClosedDeparture)) {
                maxClosedDeparture = depTime;
            }
            if (maxEnd == null || interval.end.isAfter(maxEnd)) {
                maxEnd = interval.end;
            }
        }

        boolean hasBlockingOpen = false;
        for (PresenceInterval interval : intervals) {
            if (trimToNull(interval.presence.getHeureDepart()) != null) {
                continue;
            }
            LocalTime arrival = parseTime(trimToNull(interval.presence.getHeureArrivee()));
            if (arrival == null || maxClosedDeparture == null || !arrival.isAfter(maxClosedDeparture)) {
                hasBlockingOpen = true;
                break;
            }
        }

        if (!hasBlockingOpen && maxEnd != null) {
            return maxEnd;
        }
        LocalTime closeTime = parseTime(plan.autoCloseDay);
        return closeTime != null ? LocalDateTime.of(dateJour, closeTime) : maxEnd;
    }

    private void updatePointagesBrutsNoPresenceSafely(Long sourcePresenceId, PresenceEmploye mergedPresence) {
        List<PointageBrut> pointages = pointageBrutRepository.findByPresenceEmployeIdOrderByDateHeurePointageAsc(sourcePresenceId);
        if (pointages.isEmpty()) {
            return;
        }
        for (PointageBrut pointage : pointages) {
            pointage.setPresenceEmploye(mergedPresence);
            if (pointage.getNoPresence() == null) {
                pointage.setNoPresence(mergedPresence.getId());
            }
            pointage.setRowscn(pointage.getRowscn() != null ? pointage.getRowscn() + 1 : 1);
        }
        pointageBrutRepository.saveAll(pointages);
    }

    private void arrangerPresenceSwapIfNeeded(PresenceEmploye presence) {
        if (presence.getDateJour() == null) {
            return;
        }
        String heureArrivee = trimToNull(presence.getHeureArrivee());
        String heureDepart = trimToNull(presence.getHeureDepart());
        if (heureArrivee == null || heureDepart == null) {
            return;
        }
        LocalTime arrivee = parseTime(heureArrivee);
        LocalTime depart = parseTime(heureDepart);
        if (arrivee == null || depart == null) {
            return;
        }
        boolean overnight = presence.getDateDepart() != null && presence.getDateDepart().isAfter(presence.getDateJour());
        if (!overnight && depart.isBefore(arrivee)) {
            presence.setHeureArrivee(heureDepart);
            presence.setHeureDepart(heureArrivee);
            if (presence.getDateDepart() == null) {
                presence.setDateDepart(presence.getDateJour());
            }
        }
    }

    private MergePlanContext resolvePlanContext(Long employeId, LocalDate dateJour) {
        MergePlanContext context = new MergePlanContext();
        context.autoCloseDay = null;
        context.autoCloseNight = null;
        context.nightStart = null;
        context.nightEnd = null;
        context.nightClosingLimit = null;

        EmploiEmploye emploi = resolveEmploiEmploye(employeId, dateJour);
        Horaire horaire = emploi != null ? emploi.getHoraire() : null;
        HoraireSpecial special = resolveHoraireSpecial(employeId, dateJour);
        if (special != null) {
            context.hasNightPlan = special.getDateFin() != null && special.getDateFin().isAfter(special.getDateDebut());
            context.nightStart = trimToNull(special.getHeureDebut());
            context.nightEnd = trimToNull(special.getHeureFin());
        } else if (horaire != null) {
            HoraireDt dt = horaireDtRepository.findByHoraireIdAndJour(horaire.getId(), dayIndex(dateJour));
            if (dt != null) {
                context.dayStart = trimToNull(dt.getHeureDebutJour());
                context.dayEnd = trimToNull(dt.getHeureFinJour());
                context.nightStart = trimToNull(dt.getHeureDebutNuit());
                context.nightEnd = trimToNull(dt.getHeureFinNuit());
                context.hasNightPlan = context.nightStart != null && context.nightEnd != null;
            }
            context.defaultNbHovertime = horaire.getDefaultNbHovertime();
            context.autoCloseDay = trimToNull(horaire.getHeureFermetureAutoJour());
            context.autoCloseNight = trimToNull(horaire.getHeureFermetureAutoNuit());
        }

        context.nightClosingLimit = resolveNightClosingLimit(context);
        if (context.autoCloseDay == null) {
            context.autoCloseDay = context.dayEnd;
        }
        if (context.autoCloseNight == null) {
            context.autoCloseNight = context.nightClosingLimit;
        }
        return context;
    }

    private String resolveNightClosingLimit(MergePlanContext context) {
        if (trimToNull(context.autoCloseNight) != null) {
            return context.autoCloseNight;
        }
        LocalTime end = parseTime(context.nightEnd);
        if (end == null) {
            return context.dayEnd;
        }
        if (context.defaultNbHovertime != null && context.defaultNbHovertime > 0) {
            end = end.plusHours(context.defaultNbHovertime);
        }
        return formatTime(end);
    }

    private EmploiEmploye resolveEmploiEmploye(Long employeId, LocalDate date) {
        List<EmploiEmploye> emplois = emploiEmployeRepository.findActiveForDate(employeId, date);
        return emplois.isEmpty() ? null : emplois.get(0);
    }

    private HoraireSpecial resolveHoraireSpecial(Long employeId, LocalDate date) {
        List<HoraireSpecial> specials = horaireSpecialRepository.findActiveByEmployeIdAndDate(employeId, date);
        return specials.isEmpty() ? null : specials.get(0);
    }

    private LocalDateTime computeNightWindowStart(LocalDate dateJour, MergePlanContext context) {
        LocalTime start = parseTime(context.nightStart);
        if (start == null) {
            start = parseTime(context.dayStart);
        }
        if (start == null) {
            start = LocalTime.MIDNIGHT;
        }
        return LocalDateTime.of(dateJour, start);
    }

    private LocalDateTime computeNightWindowEnd(LocalDate dateJour, MergePlanContext context) {
        LocalTime end = parseTime(context.nightClosingLimit);
        if (end == null) {
            end = parseTime(context.autoCloseNight);
        }
        if (end == null) {
            end = LocalTime.of(6, 0);
        }
        return LocalDateTime.of(dateJour.plusDays(1), end);
    }

    private LocalDateTime resolveStart(PresenceEmploye presence) {
        String arrivee = trimToNull(presence.getHeureArrivee());
        if (arrivee == null || presence.getDateJour() == null) {
            return null;
        }
        LocalTime arrival = parseTime(arrivee);
        return arrival != null ? LocalDateTime.of(presence.getDateJour(), arrival) : null;
    }

    private LocalDateTime resolveEnd(PresenceEmploye presence,
                                     MergePlanContext context,
                                     boolean nightMode,
                                     LocalDate forcedDateDepart) {
        LocalDate endDate = forcedDateDepart != null
                ? forcedDateDepart
                : (presence.getDateDepart() != null ? presence.getDateDepart() : presence.getDateJour());
        String depart = trimToNull(presence.getHeureDepart());
        if (depart != null) {
            LocalTime dep = parseTime(depart);
            if (dep == null || endDate == null) {
                return null;
            }
            LocalDateTime end = LocalDateTime.of(endDate, dep);
            LocalDateTime start = resolveStart(presence);
            if (start != null && end.isBefore(start)) {
                end = end.plusDays(1);
            }
            return end;
        }

        String autoClose = nightMode ? context.autoCloseNight : context.autoCloseDay;
        LocalTime close = parseTime(autoClose);
        if (close == null || endDate == null) {
            return null;
        }
        return LocalDateTime.of(endDate, close);
    }

    private int dayIndex(LocalDate date) {
        int value = date.getDayOfWeek().getValue();
        return value == 0 ? 7 : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalTime parseTime(String value) {
        String clean = trimToNull(value);
        if (clean == null) {
            return null;
        }
        try {
            return LocalTime.parse(clean);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String formatTime(LocalTime time) {
        return time != null ? time.toString() : null;
    }

    private static class PresenceInterval {
        private final PresenceEmploye presence;
        private final LocalDateTime start;
        private final LocalDateTime end;

        private PresenceInterval(PresenceEmploye presence, LocalDateTime start, LocalDateTime end) {
            this.presence = presence;
            this.start = start;
            this.end = end;
        }
    }

    private static class MergeResult {
        private final PresenceEmploye keeper;
        private final List<Long> mergedIds;

        private MergeResult(PresenceEmploye keeper, List<Long> mergedIds) {
            this.keeper = keeper;
            this.mergedIds = mergedIds;
        }
    }

    private static class MergePlanContext {
        private boolean hasNightPlan;
        private String dayStart;
        private String dayEnd;
        private String nightStart;
        private String nightEnd;
        private String autoCloseDay;
        private String autoCloseNight;
        private String nightClosingLimit;
        private Integer defaultNbHovertime;

        private MergePlanContext asDayPlan() {
            MergePlanContext dayPlan = new MergePlanContext();
            dayPlan.hasNightPlan = false;
            dayPlan.dayStart = this.dayStart;
            dayPlan.dayEnd = this.dayEnd;
            dayPlan.autoCloseDay = this.autoCloseDay;
            dayPlan.autoCloseNight = this.autoCloseNight;
            dayPlan.nightStart = this.nightStart;
            dayPlan.nightEnd = this.nightEnd;
            dayPlan.nightClosingLimit = this.nightClosingLimit;
            dayPlan.defaultNbHovertime = this.defaultNbHovertime;
            return dayPlan;
        }
    }
}
