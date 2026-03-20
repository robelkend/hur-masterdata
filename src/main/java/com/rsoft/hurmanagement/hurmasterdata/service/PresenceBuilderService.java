package com.rsoft.hurmanagement.hurmasterdata.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PresenceBuilderService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int DEFAULT_TOLERANCE_MIN = 5;
    private static final int DEFAULT_SEUIL_DOUBLON_MIN = 2;
    private static final int DEFAULT_MAX_SESSION_HOURS = 16;
    private static final int DEFAULT_MIN_SESSION_MINUTES = 15;

    private final PointageBrutRepository pointageBrutRepository;
    private final PresenceEmployeRepository presenceEmployeRepository;
    private final EmployeRepository employeRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final HoraireSpecialRepository horaireSpecialRepository;
    private final HoraireDtRepository horaireDtRepository;
    private final EmployeSalaireRepository employeSalaireRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Map<String, Object> processPunches(String dateDebut,
                                              String dateFin,
                                              Long employeId,
                                              Long entrepriseId,
                                              String username) {
        List<PointageBrut> punches;
        boolean hasDateRange = dateDebut != null && !dateDebut.isBlank() && dateFin != null && !dateFin.isBlank();
        if (hasDateRange) {
            LocalDate dateDebutLocal = LocalDate.parse(dateDebut);
            LocalDate dateFinLocal = LocalDate.parse(dateFin);
            ZoneId zone = ZoneId.systemDefault();
            OffsetDateTime start = dateDebutLocal.atStartOfDay(zone).toOffsetDateTime();
            OffsetDateTime endExclusive = dateFinLocal.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
            punches = pointageBrutRepository.findForPresenceBuild(
                    start,
                    endExclusive,
                    employeId,
                    entrepriseId,
                    Arrays.asList(PointageBrut.StatutTraitement.BRUT, PointageBrut.StatutTraitement.PRET)
            );
        } else {
            punches = pointageBrutRepository.findForPresenceBuildAll(
                    employeId,
                    entrepriseId,
                    List.of(PointageBrut.StatutTraitement.BRUT)
            );
        }

        int successRows = 0;
        int errorRows = 0;
        int ignoredRows = 0;
        List<String> errors = new ArrayList<>();

        for (PointageBrut punch : punches) {
            try {
                ProcessingResult result = processSingle(punch.getId(), username);
                if (result == ProcessingResult.SUCCESS) {
                    successRows++;
                } else if (result == ProcessingResult.IGNORED) {
                    ignoredRows++;
                } else {
                    errorRows++;
                }
            } catch (Exception e) {
                errorRows++;
                errors.add("Row error: " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("processedRows", punches.size());
        result.put("successRows", successRows);
        result.put("errorRows", errorRows);
        result.put("ignoredRows", ignoredRows);
        result.put("errors", errors);
        result.put("message", "Processed " + successRows + " rows successfully, " + errorRows + " errors");
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessingResult processSingle(Long pointageId, String username) {
        PointageBrut pointage = pointageBrutRepository.findById(pointageId)
                .orElseThrow(() -> new RuntimeException("PointageBrut not found with id: " + pointageId));

        if (pointage.getStatutTraitement() == PointageBrut.StatutTraitement.UTILISE
                || pointage.getStatutTraitement() == PointageBrut.StatutTraitement.IGNORE) {
            return ProcessingResult.IGNORED;
        }

        Employe employe = resolveEmploye(pointage);
        if (employe == null) {
            markPointageError(pointage, "Employe not found for badge: " + pointage.getIdBadge(), username);
            return ProcessingResult.ERROR;
        }
        pointage.setEmploye(employe);
        pointage.setEntreprise(employe.getEntreprise());

        PlanningContext planning = computePlanningForPunch(employe.getId(), pointage.getDateHeurePointage());

        if (isDuplicatePunch(pointage, employe.getId(), planning)) {
            markDuplicate(pointage, username);
            return ProcessingResult.IGNORED;
        }

        LocalDate dateJour = planning.dateJour;
        PresenceEmploye draft = presenceEmployeRepository
                .findTopByEmployeIdAndDateJourAndStatutPresenceOrderByIdDesc(
                        employe.getId(),
                        dateJour,
                        PresenceEmploye.StatutPresence.BROUILLON
                );
        if (draft == null) {
            draft = presenceEmployeRepository.findTopByEmployeIdAndStatutPresenceOrderByIdDesc(
                    employe.getId(),
                    PresenceEmploye.StatutPresence.BROUILLON
            );
        }

        if (draft == null) {
            PresenceEmploye presence = createDraftPresence(pointage, employe, planning, username);
            pointage.setPresenceEmploye(presence);
            markPointageUsed(pointage, username);
            return ProcessingResult.SUCCESS;
        }

        boolean closed = tryCloseDraft(draft, pointage, planning, username);
        if (closed) {
            pointage.setPresenceEmploye(draft);
            markPointageUsed(pointage, username);
            return ProcessingResult.SUCCESS;
        }

        // If not closed, create a new draft for this punch
        PresenceEmploye newDraft = createDraftPresence(pointage, employe, planning, username);
        pointage.setPresenceEmploye(newDraft);
        markPointageUsed(pointage, username);
        return ProcessingResult.SUCCESS;
    }

    public void autoCloseDraftPresences(OffsetDateTime referenceTime, String username) {
        List<PresenceEmploye> drafts = presenceEmployeRepository.findByStatutPresence(PresenceEmploye.StatutPresence.BROUILLON);
        for (PresenceEmploye draft : drafts) {
            try {
                PlanningContext planning = computePlanningForPresence(draft);
                if (!planning.autoCloseAllowed || planning.heureFermetureAuto == null) {
                    continue;
                }
                LocalDate closeDate = planning.isNight ? draft.getDateJour().plusDays(1) : draft.getDateJour();
                LocalDateTime autoCloseTime = LocalDateTime.of(closeDate, planning.heureFermetureAuto);
                if (referenceTime.toLocalDateTime().isAfter(autoCloseTime)) {
                    closeDraftPresenceAuto(draft, planning, username);
                }
            } catch (Exception ignored) {
                // keep draft
            }
        }
    }

    private Employe resolveEmploye(PointageBrut pointage) {
        if (pointage.getEmploye() != null) {
            return pointage.getEmploye();
        }
        String badge = pointage.getIdBadge();
        if (badge == null || badge.trim().isEmpty()) {
            return null;
        }
        return employeRepository.findByCodeEmploye(badge)
                .orElseGet(() -> employeRepository.findByMatriculeInterne(badge).orElse(null));
    }

    private PlanningContext computePlanningForPunch(Long employeId, OffsetDateTime tsPointage) {
        ZonedDateTime localTs = toLocalZoned(tsPointage);
        LocalDate date = localTs.toLocalDate();
        LocalTime time = localTs.toLocalTime();

        EmploiEmploye emploi = resolveEmploiEmploye(employeId, date);
        Horaire horaire = emploi != null ? emploi.getHoraire() : null;

        HoraireSpecial special = resolveHoraireSpecial(employeId, date);
        if (special != null && special.getEmploiEmploye() != null) {
            if (emploi == null) {
                emploi = special.getEmploiEmploye();
            }
            if (horaire == null && special.getEmploiEmploye().getHoraire() != null) {
                horaire = special.getEmploiEmploye().getHoraire();
            }
        }
        HoraireDt horaireDt = horaire != null ? horaireDtRepository.findByHoraireIdAndJour(horaire.getId(), dayOfWeekIndex(date)) : null;

        LocalTime debutJour = parseTime(special != null ? special.getHeureDebut() : (horaireDt != null ? horaireDt.getHeureDebutJour() : (horaire != null ? horaire.getHeureDebut() : null)));
        LocalTime finJour = parseTime(special != null ? special.getHeureFin() : (horaireDt != null ? horaireDt.getHeureFinJour() : (horaire != null ? horaire.getHeureFin() : null)));

        LocalTime debutNuit = parseTime(horaireDt != null ? horaireDt.getHeureDebutNuit() : (horaire != null ? horaire.getHeureDebutNuit() : null));
        LocalTime finNuit = parseTime(horaireDt != null ? horaireDt.getHeureFinNuit() : (horaire != null ? horaire.getHeureFinNuit() : null));

        boolean isNight;
        LocalDate dateJour = date;
        LocalTime heureDebutPrevue;
        LocalTime heureFinPrevue;
        boolean horaireSpecial = special != null;

        if (special != null && debutJour != null && finJour != null) {
            boolean specialNight = crossesMidnight(debutJour, finJour);
            boolean inSpecialWindow = isInWindow(time, debutJour, finJour);
            isNight = specialNight || (inSpecialWindow && specialNight);
            heureDebutPrevue = debutJour;
            heureFinPrevue = finJour;
            int overtimeMinutes = horaire != null && horaire.getDefaultNbHovertime() != null ? horaire.getDefaultNbHovertime() * 60 : 0;
            LocalTime extendedEnd = finJour.plusMinutes(Math.max(overtimeMinutes, 0));
            if (specialNight && isInWindow(time, debutJour, extendedEnd) && time.isBefore(extendedEnd)) {
                dateJour = date.minusDays(1);
            }
        } else {
            isNight = isInWindow(time, debutNuit, finNuit);
            if (!isNight && debutJour != null && finJour != null) {
                isNight = false;
            } else if (isNight) {
                // night stays night
            } else if (debutNuit != null && finNuit != null && debutJour == null) {
                isNight = true;
            }
            heureDebutPrevue = isNight ? debutNuit : debutJour;
            heureFinPrevue = isNight ? finNuit : finJour;
            if (isNight && debutNuit != null && finNuit != null && crossesMidnight(debutNuit, finNuit) && time.isBefore(finNuit)) {
                dateJour = date.minusDays(1);
            }
        }

        PlanningContext context = new PlanningContext();
        context.employeId = employeId;
        context.emploiEmploye = emploi;
        context.horaire = horaire;
        context.dateJour = dateJour;
        context.isNight = isNight;
        context.heureDebutPrevue = heureDebutPrevue;
        context.heureFinPrevue = heureFinPrevue;
        context.horaireSpecial = horaireSpecial;
        context.toleranceRetardMin = horaire != null && horaire.getToleranceRetardMin() != null ? horaire.getToleranceRetardMin() : DEFAULT_TOLERANCE_MIN;
        context.seuilDoublonMin = horaire != null && horaire.getSeuilDoublonMin() != null ? horaire.getSeuilDoublonMin() : DEFAULT_SEUIL_DOUBLON_MIN;
        context.maxSessionHeures = horaire != null && horaire.getMaxSessionHeures() != null ? horaire.getMaxSessionHeures() : DEFAULT_MAX_SESSION_HOURS;
        context.overtimeMinutes = horaire != null && horaire.getDefaultNbHovertime() != null ? horaire.getDefaultNbHovertime() * 60 : 0;
        context.autoCloseAllowed = horaireDt != null && "Y".equalsIgnoreCase(horaireDt.getHeureFermetureAuto());
        context.heureFermetureAuto = parseTime(isNight ? (horaire != null ? horaire.getHeureFermetureAutoNuit() : null)
                                                       : (horaire != null ? horaire.getHeureFermetureAutoJour() : null));
        return context;
    }

    private PlanningContext computePlanningForPresence(PresenceEmploye presence) {
        PlanningContext context = new PlanningContext();
        context.employeId = presence.getEmploye() != null ? presence.getEmploye().getId() : null;
        context.dateJour = presence.getDateJour();
        context.isNight = "Y".equalsIgnoreCase(presence.getNuitPlanifiee());
        context.heureDebutPrevue = parseTime(presence.getHeureDebutPrevue());
        context.heureFinPrevue = parseTime(presence.getHeureFinPrevue());
        context.heureFermetureAuto = parseTime(presence.getHeureFinPrevue());
        context.autoCloseAllowed = true;
        return context;
    }

    private EmploiEmploye resolveEmploiEmploye(Long employeId, LocalDate date) {
        List<EmploiEmploye> emplois = emploiEmployeRepository.findActiveForDate(employeId, date);
        return emplois.isEmpty() ? null : emplois.get(0);
    }

    private HoraireSpecial resolveHoraireSpecial(Long employeId, LocalDate date) {
        List<HoraireSpecial> specials = horaireSpecialRepository.findActiveByEmployeIdAndDate(employeId, date);
        return specials.isEmpty() ? null : specials.get(0);
    }

    private boolean isDuplicatePunch(PointageBrut pointage, Long employeId, PlanningContext planning) {
        PointageBrut last = pointageBrutRepository
                .findTopByEmployeIdAndDateHeurePointageLessThanOrderByDateHeurePointageDesc(
                        employeId, pointage.getDateHeurePointage());
        if (last == null) {
            return false;
        }
        long minutes = Duration.between(
                toLocalZoned(last.getDateHeurePointage()),
                toLocalZoned(pointage.getDateHeurePointage())
        ).toMinutes();
        return minutes >= 0 && minutes <= planning.seuilDoublonMin;
    }

    private PresenceEmploye createDraftPresence(PointageBrut pointage, Employe employe, PlanningContext planning, String username) {
        ZonedDateTime localTs = toLocalZoned(pointage.getDateHeurePointage());
        PresenceEmploye presence = new PresenceEmploye();
        presence.setEmploye(employe);
        presence.setEntreprise(employe.getEntreprise());
        presence.setDateJour(planning.dateJour);
        presence.setHeureArrivee(formatTime(localTs.toLocalTime()));
        presence.setNuitPlanifiee(planning.isNight ? "Y" : "N");
        presence.setHeureDebutPrevue(formatTime(planning.heureDebutPrevue));
        presence.setHeureFinPrevue(formatTime(planning.heureFinPrevue));
        presence.setHoraireSpecial(planning.horaireSpecial ? "Y" : "N");
        presence.setIdHoraire(planning.horaire != null ? planning.horaire.getId() : null);
        presence.setSourceSaisie(PresenceEmploye.SourceSaisie.BADGE);
        presence.setStatutPresence(PresenceEmploye.StatutPresence.BROUILLON);
        presence.setAutomatique("N");

        if (planning.emploiEmploye != null) {
            presence.setTypeEmploye(planning.emploiEmploye.getTypeEmploye());
        }
        EmployeSalaire salaire = resolveSalaire(employe.getId());
        if (salaire != null) {
            presence.setRegimePaie(salaire.getRegimePaie());
        }

        presence.setCreatedBy(username);
        presence.setCreatedOn(OffsetDateTime.now());
        presence.setRowscn(1);

        addAnomaly(presence, "POINTAGE_UNIQUE", "INFO", "Only one punch recorded", pointage, planning);
        addAmbiguityAnomalyIfNeeded(presence, pointage, planning);
        return presenceEmployeRepository.save(presence);
    }

    private boolean tryCloseDraft(PresenceEmploye draft, PointageBrut pointage, PlanningContext planning, String username) {
        ZonedDateTime localTs = toLocalZoned(pointage.getDateHeurePointage());
        LocalDateTime arrival = LocalDateTime.of(draft.getDateJour(), parseTime(draft.getHeureArrivee()));
        LocalDateTime punchTime = localTs.toLocalDateTime();

        long hours = Duration.between(arrival, punchTime).toHours();
        long minutes = Duration.between(arrival, punchTime).toMinutes();

        if (hours > planning.maxSessionHeures) {
            addAnomaly(draft, "DUREE_TROP_LONGUE", "ERROR", "Session duration exceeds max session hours", pointage, planning);
            presenceEmployeRepository.save(draft);
            return false;
        }

        if (minutes < DEFAULT_MIN_SESSION_MINUTES) {
            addAnomaly(draft, "DUREE_TROP_COURTE", "WARN", "Session duration too short", pointage, planning);
        }

        if (!isWithinExpectedWindow(localTs.toLocalTime(), planning)) {
            addAnomaly(draft, "HORS_PLAGE", "WARN", "Punch outside expected window", pointage, planning);
        }

        draft.setHeureDepart(formatTime(localTs.toLocalTime()));
        if (planning.isNight && planning.heureFinPrevue != null && crossesMidnight(planning.heureDebutPrevue, planning.heureFinPrevue)) {
            draft.setDateDepart(localTs.toLocalDate());
        } else {
            // If punch day is after dateJour but still within max session, keep depart on punch date
            LocalDate punchDate = localTs.toLocalDate();
            draft.setDateDepart(punchDate.isAfter(draft.getDateJour()) ? punchDate : draft.getDateJour());
        }
        draft.setStatutPresence(PresenceEmploye.StatutPresence.VALIDE);
        removeAnomaly(draft, "POINTAGE_UNIQUE");
        draft.setUpdatedBy(username);
        draft.setUpdatedOn(OffsetDateTime.now());
        draft.setRowscn(draft.getRowscn() + 1);
        presenceEmployeRepository.save(draft);
        return true;
    }

    private void closeDraftPresenceAuto(PresenceEmploye draft, PlanningContext planning, String username) {
        if (draft.getHeureDepart() != null) {
            return;
        }
        draft.setAutomatique("Y");
        draft.setHeureDepart(formatTime(planning.heureFinPrevue));
        draft.setDateDepart(planning.isNight ? draft.getDateJour().plusDays(1) : draft.getDateJour());
        draft.setStatutPresence(PresenceEmploye.StatutPresence.VALIDE);
        addAnomaly(draft, "AUTO_CLOSE_APPLIED", "INFO", "Auto close applied", null, planning);
        draft.setUpdatedBy(username);
        draft.setUpdatedOn(OffsetDateTime.now());
        draft.setRowscn(draft.getRowscn() + 1);
        presenceEmployeRepository.save(draft);
    }

    private void markPointageUsed(PointageBrut pointage, String username) {
        pointage.setStatutTraitement(PointageBrut.StatutTraitement.UTILISE);
        pointage.setQualitePointage(PointageBrut.QualitePointage.OK);
        pointage.setTraiteLe(OffsetDateTime.now());
        pointage.setTraitePar(username);
        pointage.setRowscn(pointage.getRowscn() != null ? pointage.getRowscn() + 1 : 1);
        pointageBrutRepository.save(pointage);
    }

    private void markDuplicate(PointageBrut pointage, String username) {
        pointage.setQualitePointage(PointageBrut.QualitePointage.DUPLICAT);
        pointage.setStatutTraitement(PointageBrut.StatutTraitement.IGNORE);
        pointage.setMotifRejet("Duplicate punch");
        pointage.setTraiteLe(OffsetDateTime.now());
        pointage.setTraitePar(username);
        pointage.setRowscn(pointage.getRowscn() != null ? pointage.getRowscn() + 1 : 1);
        pointageBrutRepository.save(pointage);
    }

    private void markPointageError(PointageBrut pointage, String message, String username) {
        pointage.setStatutTraitement(PointageBrut.StatutTraitement.ERREUR);
        pointage.setMotifRejet(message);
        pointage.setTraiteLe(OffsetDateTime.now());
        pointage.setTraitePar(username);
        pointage.setRowscn(pointage.getRowscn() != null ? pointage.getRowscn() + 1 : 1);
        pointageBrutRepository.save(pointage);
    }

    private EmployeSalaire resolveSalaire(Long employeId) {
        return employeSalaireRepository
                .findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(employeId, "Y")
                .orElse(null);
    }

    private void addAnomaly(PresenceEmploye presence,
                            String code,
                            String severity,
                            String message,
                            PointageBrut pointage,
                            PlanningContext planning) {
        try {
            JsonNode rootNode = presence.getDetails() != null && !presence.getDetails().trim().isEmpty()
                    ? objectMapper.readTree(presence.getDetails())
                    : objectMapper.createObjectNode();
            ObjectNode root = rootNode.isObject() ? (ObjectNode) rootNode : objectMapper.createObjectNode();
            ArrayNode codes = root.withArray("codes");
            if (!containsCode(codes, code)) {
                codes.add(code);
            }
            ArrayNode details = root.withArray("details");
            ObjectNode detail = objectMapper.createObjectNode();
            detail.put("code", code);
            detail.put("severity", severity);
            detail.put("message", message);
            if (pointage != null) {
                ObjectNode punch = objectMapper.createObjectNode();
                punch.put("ts", pointage.getDateHeurePointage().toString());
                detail.set("punch", punch);
            }
            if (planning != null) {
                ObjectNode plan = objectMapper.createObjectNode();
                plan.put("shift", planning.isNight ? "NUIT" : "JOUR");
                plan.put("debut", formatTime(planning.heureDebutPrevue));
                plan.put("fin", formatTime(planning.heureFinPrevue));
                detail.set("planning", plan);
            }
            ObjectNode meta = objectMapper.createObjectNode();
            meta.put("versionTraitement", 1);
            detail.set("meta", meta);
            details.add(detail);
            presence.setDetails(root.toString());
            try {
                presence.setTypeErreur(PresenceEmploye.TypeErreur.valueOf(code));
            } catch (Exception ignored) {
                // keep existing type_erreur
            }
        } catch (Exception ignored) {
            // ignore details update failures
        }
    }

    private void addAmbiguityAnomalyIfNeeded(PresenceEmploye presence, PointageBrut pointage, PlanningContext planning) {
        if (planning != null && planning.horaireSpecial) {
            return;
        }
        LocalTime time = toLocalZoned(pointage.getDateHeurePointage()).toLocalTime();
        if (isNearBoundary(time, LocalTime.of(18, 0))) {
            addAnomaly(presence, "AMBIGU_18H", "WARN", "Ambiguous punch around 18:00", pointage, null);
        }
        if (isNearBoundary(time, LocalTime.of(6, 0))) {
            addAnomaly(presence, "AMBIGU_6H", "WARN", "Ambiguous punch around 06:00", pointage, null);
        }
    }

    private boolean isNearBoundary(LocalTime time, LocalTime boundary) {
        if (time == null || boundary == null) {
            return false;
        }
        long minutes = Math.abs(Duration.between(boundary, time).toMinutes());
        return minutes <= 30;
    }

    private boolean containsCode(ArrayNode codes, String code) {
        for (JsonNode node : codes) {
            if (code.equalsIgnoreCase(node.asText())) {
                return true;
            }
        }
        return false;
    }

    private String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMAT) : null;
    }

    private ZonedDateTime toLocalZoned(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZoneSameInstant(ZoneId.systemDefault());
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalTime.parse(value, TIME_FORMAT);
    }

    private boolean isInWindow(LocalTime time, LocalTime start, LocalTime end) {
        if (time == null || start == null || end == null) {
            return false;
        }
        if (crossesMidnight(start, end)) {
            return !time.isBefore(start) || time.isBefore(end);
        }
        return !time.isBefore(start) && !time.isAfter(end);
    }

    private boolean isWithinExpectedWindow(LocalTime time, PlanningContext planning) {
        if (time == null || planning == null || planning.heureDebutPrevue == null || planning.heureFinPrevue == null) {
            return true;
        }
        LocalTime start = planning.heureDebutPrevue.minusMinutes(120);
        LocalTime end = planning.heureFinPrevue.plusMinutes(180 + Math.max(planning.overtimeMinutes, 0));
        return isInWindow(time, start, end);
    }

    private void removeAnomaly(PresenceEmploye presence, String code) {
        if (presence == null || presence.getDetails() == null) {
            return;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(presence.getDetails());
            ObjectNode root = rootNode.isObject() ? (ObjectNode) rootNode : objectMapper.createObjectNode();
            ArrayNode codes = root.withArray("codes");
            ArrayNode newCodes = objectMapper.createArrayNode();
            for (JsonNode node : codes) {
                if (!code.equalsIgnoreCase(node.asText())) {
                    newCodes.add(node.asText());
                }
            }
            root.set("codes", newCodes);
            ArrayNode details = root.withArray("details");
            ArrayNode newDetails = objectMapper.createArrayNode();
            for (JsonNode node : details) {
                String nodeCode = node.has("code") ? node.get("code").asText() : "";
                if (!code.equalsIgnoreCase(nodeCode)) {
                    newDetails.add(node);
                }
            }
            root.set("details", newDetails);
            presence.setDetails(root.toString());
            if (newCodes.isEmpty()) {
                presence.setTypeErreur(null);
            }
        } catch (Exception ignored) {
            // ignore details update failures
        }
    }

    private boolean crossesMidnight(LocalTime start, LocalTime end) {
        return start != null && end != null && start.isAfter(end);
    }

    private int dayOfWeekIndex(LocalDate date) {
        int val = date.getDayOfWeek().getValue();
        return val == 0 ? 7 : val;
    }

    private static class PlanningContext {
        Long employeId;
        EmploiEmploye emploiEmploye;
        Horaire horaire;
        LocalDate dateJour;
        boolean isNight;
        LocalTime heureDebutPrevue;
        LocalTime heureFinPrevue;
        boolean horaireSpecial;
        int toleranceRetardMin;
        int seuilDoublonMin;
        int maxSessionHeures;
        int overtimeMinutes;
        boolean autoCloseAllowed;
        LocalTime heureFermetureAuto;
    }

    private enum ProcessingResult {
        SUCCESS, ERROR, IGNORED
    }
}
