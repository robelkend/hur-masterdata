package com.rsoft.hurmanagement.hurmasterdata.job;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoading;
import com.rsoft.hurmanagement.hurmasterdata.entity.ProcessusParametre;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceLoadingRepository;
import com.rsoft.hurmanagement.hurmasterdata.service.InterfaceLoadingService;
import com.rsoft.hurmanagement.hurmasterdata.service.PresenceBuilderService;
import com.rsoft.hurmanagement.hurmasterdata.service.PresenceRearrangeService;
import com.rsoft.hurmanagement.hurmasterdata.service.CongeEmployeFinalizeService;
import com.rsoft.hurmanagement.hurmasterdata.service.SupplementaireEmployeService;
import com.rsoft.hurmanagement.hurmasterdata.service.HoraireSpecialService;
import com.rsoft.hurmanagement.hurmasterdata.service.PrimePresenceService;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireGenerationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProcessusJobService {
    private static final String PROCESSUS_DB_CLOCKIN_OUT = "DB_CLOCKIN_OUT";
    private static final String PROCESSUS_ATTENDANCES = "PROCESS_ATTENDANCES";
    private static final String PROCESSUS_REARRANGE_ATTENDANCE = "REARRANGE_ATTENDANCE";
    private static final String PROCESSUS_FINALIZE_LEAVE = "FINALIZE_LEAVE";
    private static final String PROCESSUS_OVERTIMES = "process_overtimes";
    private static final String PROCESSUS_OVERTIME = "process_overtime";
    private static final String PROCESSUS_CREATE_SCHEDULES = "create_schedules";
    private static final String PROCESSUS_PRIME_PRESENCE = "PRIME_PRESENCE";

    private final InterfaceLoadingRepository interfaceLoadingRepository;
    private final InterfaceLoadingService interfaceLoadingService;
    private final PresenceBuilderService presenceBuilderService;
    private final PresenceRearrangeService presenceRearrangeService;
    private final CongeEmployeFinalizeService congeEmployeFinalizeService;
    private final SupplementaireEmployeService supplementaireEmployeService;
    private final HoraireSpecialService horaireSpecialService;
    private final PrimePresenceService primePresenceService;

    public void runProcessus(ProcessusParametre job, String username) {
        String code = job.getCodeProcessus() != null ? job.getCodeProcessus().trim() : "";
        if (PROCESSUS_DB_CLOCKIN_OUT.equalsIgnoreCase(code)) {
            runPointageLoading(job, username);
            return;
        }
        if (PROCESSUS_ATTENDANCES.equalsIgnoreCase(code)) {
            runAttendancesBuild(job, username);
            return;
        }
        if (PROCESSUS_REARRANGE_ATTENDANCE.equalsIgnoreCase(code)) {
            runAttendancesRearrange(job, username);
            return;
        }
        if (PROCESSUS_FINALIZE_LEAVE.equalsIgnoreCase(code)) {
            runFinalizeLeave(job, username);
            return;
        }
        if (PROCESSUS_OVERTIMES.equalsIgnoreCase(code) || PROCESSUS_OVERTIME.equalsIgnoreCase(code)) {
            runOvertimesGeneration(job, username);
            return;
        }
        if (PROCESSUS_CREATE_SCHEDULES.equalsIgnoreCase(code)) {
            runSchedulesDuplication(job, username);
            return;
        }
        if (PROCESSUS_PRIME_PRESENCE.equalsIgnoreCase(code)) {
            runPrimePresence(job, username);
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown process code: " + job.getCodeProcessus());
    }

    private void runPointageLoading(ProcessusParametre job, String username) {
        InterfaceLoading loading = interfaceLoadingRepository.findByCodeLoading(PROCESSUS_DB_CLOCKIN_OUT)
                .orElseGet(() -> interfaceLoadingRepository.findByCodeLoading("CLOCK_INOUT").orElse(null));
        if (loading == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Interface loading DB_CLOCKIN_OUT not configured");
        }
        Map<String, String> params = buildDateParams(job);
        Map<String, Object> result;
        if (loading.getSource() == InterfaceLoading.Source.RDB) {
            result = interfaceLoadingService.loadFromDatabase(loading.getId(), username, params);
        } else if (loading.getSource() == InterfaceLoading.Source.API) {
            result = interfaceLoadingService.loadFromApi(loading.getId(), username);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DB_CLOCKIN_OUT must be configured as RDB or API source");
        }
        if (result != null && Boolean.FALSE.equals(result.get("success"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.valueOf(result.get("message")));
        }
    }

    private void runAttendancesBuild(ProcessusParametre job, String username) {
        Long entrepriseId = job.getEntreprise() != null ? job.getEntreprise().getId() : null;
        Map<String, Object> result = presenceBuilderService.processPunches(
                null,
                null,
                null,
                entrepriseId,
                username
        );
        if (result != null && Boolean.FALSE.equals(result.get("success"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.valueOf(result.get("message")));
        }
    }

    private void runAttendancesRearrange(ProcessusParametre job, String username) {
        LocalDate targetDate = resolveTargetDate(job);
        Long entrepriseId = job.getEntreprise() != null ? job.getEntreprise().getId() : null;
        Map<String, Object> result = presenceRearrangeService.closeAndRearrange(
                targetDate.toString(),
                targetDate.toString(),
                null,
                entrepriseId,
                username
        );
        if (result != null && Boolean.FALSE.equals(result.get("success"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.valueOf(result.get("message")));
        }
    }

    private void runFinalizeLeave(ProcessusParametre job, String username) {
        LocalDate targetDate = resolveTargetDate(job);
        Long entrepriseId = job.getEntreprise() != null ? job.getEntreprise().getId() : null;
        Map<String, Object> result = congeEmployeFinalizeService.autoFinalizeForDate(targetDate, entrepriseId, username);
        if (result != null && Boolean.FALSE.equals(result.get("success"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.valueOf(result.get("message")));
        }
    }

    private void runOvertimesGeneration(ProcessusParametre job, String username) {
        DateRange range = buildDateRange(job);
        SupplementaireGenerationRequestDTO request = new SupplementaireGenerationRequestDTO(
                range.dateDebut.toString(),
                range.dateFin.toString(),
                job.getEntreprise() != null ? job.getEntreprise().getId() : null,
                null
        );
        supplementaireEmployeService.generateSupplementaires(request, username);
    }

    private void runSchedulesDuplication(ProcessusParametre job, String username) {
        DateRange range = buildDateRange(job);
        Long entrepriseId = job.getEntreprise() != null ? job.getEntreprise().getId() : null;
        horaireSpecialService.dupliquerParCritere(
                entrepriseId,
                null,
                range.dateDebut,
                range.dateFin,
                username
        );
    }

    private void runPrimePresence(ProcessusParametre job, String username) {
        DateRange range = buildDateRange(job);
        Long entrepriseId = job.getEntreprise() != null ? job.getEntreprise().getId() : null;
        Map<String, Object> result = primePresenceService.generatePrimePresence(
                range.dateDebut,
                range.dateFin,
                entrepriseId,
                username
        );
        if (result != null && Boolean.FALSE.equals(result.get("success"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.valueOf(result.get("message")));
        }
    }

    private Map<String, String> buildDateParams(ProcessusParametre job) {
        LocalDate targetDate = resolveTargetDate(job);
        String date = targetDate.toString();
        Map<String, String> params = new HashMap<>();
        params.put("process_date", date);
        params.put("process_date_quoted", "'" + date + "'");
        if (job.getEntreprise() != null && job.getEntreprise().getId() != null) {
            String entrepriseId = job.getEntreprise().getId().toString();
            params.put("entreprise_id", entrepriseId);
            params.put("entreprise_id_quoted", "'" + entrepriseId + "'");
        }
        return params;
    }

    private LocalDate resolveTargetDate(ProcessusParametre job) {
        OffsetDateTime now = OffsetDateTime.now();
        int marge = job.getMarge() != null ? job.getMarge() : 0;
        ProcessusParametre.UniteMarge unite = job.getUniteMarge() != null ? job.getUniteMarge() : ProcessusParametre.UniteMarge.JOUR;
        OffsetDateTime target = switch (unite) {
            case MINUTE -> now.minusMinutes(marge);
            case HEURE -> now.minusHours(marge);
            case JOUR -> now.minusDays(marge);
        };
        return target.toLocalDate();
    }

    private DateRange buildDateRange(ProcessusParametre job) {
        OffsetDateTime lastExecution = job.getDerniereExecutionAt() != null ? job.getDerniereExecutionAt() : OffsetDateTime.now();
        OffsetDateTime nextExecution = job.getProchaineExecutionAt() != null ? job.getProchaineExecutionAt() : OffsetDateTime.now();
        OffsetDateTime adjustedLast = applyMarge(lastExecution, job);
        OffsetDateTime adjustedNext = applyMarge(nextExecution, job);
        LocalDate debut = adjustedLast.toLocalDate().plusDays(1);
        LocalDate fin = adjustedNext.toLocalDate();
        if (fin.isBefore(debut)) {
            fin = debut;
        }
        return new DateRange(debut, fin);
    }

    private OffsetDateTime applyMarge(OffsetDateTime dateTime, ProcessusParametre job) {
        int marge = job.getMarge() != null ? job.getMarge() : 0;
        ProcessusParametre.UniteMarge unite = job.getUniteMarge() != null ? job.getUniteMarge() : ProcessusParametre.UniteMarge.JOUR;
        return switch (unite) {
            case MINUTE -> dateTime.minusMinutes(marge);
            case HEURE -> dateTime.minusHours(marge);
            case JOUR -> dateTime.minusDays(marge);
        };
    }

    private static class DateRange {
        private final LocalDate dateDebut;
        private final LocalDate dateFin;

        private DateRange(LocalDate dateDebut, LocalDate dateFin) {
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
        }
    }
}
