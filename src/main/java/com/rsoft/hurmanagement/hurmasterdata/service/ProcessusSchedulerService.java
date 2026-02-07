package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.job.ProcessusJobService;
import com.rsoft.hurmanagement.hurmasterdata.entity.ProcessusParametre;
import com.rsoft.hurmanagement.hurmasterdata.repository.ProcessusParametreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessusSchedulerService {
    private final ProcessusParametreRepository repository;
    private final ProcessusJobService jobService;

    @Scheduled(fixedDelayString = "${processus.scheduler.delay.ms:60000}")
    @Transactional
    public void runDueJobs() {
        OffsetDateTime now = OffsetDateTime.now();
        for (ProcessusParametre job : repository.findDueJobs(now)) {
            executeJob(job, now, "SYSTEM_JOB");
        }
    }

    private void executeJob(ProcessusParametre job, OffsetDateTime now, String username) {
        if (!"Y".equalsIgnoreCase(job.getActif())) {
            return;
        }
        job.setStatut(ProcessusParametre.Statut.EN_EXECUTION);
        job.setDerniereExecutionAt(now);
        job.setUpdatedBy(username);
        job.setUpdatedOn(now);
        repository.save(job);

        try {
            jobService.runProcessus(job, username);
            job.setStatut(ProcessusParametre.Statut.REUSSI);
            job.setDerniereErreur(null);
            job.setNbEchecsConsecutifs(0);
        } catch (Exception e) {
            log.error("Processus {} error", job.getCodeProcessus(), e);
            job.setStatut(ProcessusParametre.Statut.ERREUR);
            job.setDerniereErreur(e.getMessage());
            job.setNbEchecsConsecutifs(job.getNbEchecsConsecutifs() != null ? job.getNbEchecsConsecutifs() + 1 : 1);
        }

        job.setProchaineExecutionAt(computeNextExecution(job, now));
        job.setUpdatedBy(username);
        job.setUpdatedOn(OffsetDateTime.now());
        repository.save(job);
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
}
