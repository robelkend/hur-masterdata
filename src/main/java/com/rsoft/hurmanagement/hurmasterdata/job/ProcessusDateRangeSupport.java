package com.rsoft.hurmanagement.hurmasterdata.job;

import com.rsoft.hurmanagement.hurmasterdata.entity.ProcessusParametre;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class ProcessusDateRangeSupport {

    private ProcessusDateRangeSupport() {
    }

    public static LocalDate resolveTargetDate(ProcessusParametre job, OffsetDateTime now) {
        return applyMarge(now, job).toLocalDate();
    }

    public static DateRange buildDateRange(ProcessusParametre job, OffsetDateTime defaultNow) {
        OffsetDateTime lastExecution = job.getDerniereExecutionAt() != null ? job.getDerniereExecutionAt() : defaultNow;
        OffsetDateTime nextExecution = job.getProchaineExecutionAt() != null ? job.getProchaineExecutionAt() : defaultNow;
        OffsetDateTime adjustedLast = applyMarge(lastExecution, job);
        OffsetDateTime adjustedNext = applyMarge(nextExecution, job);
        LocalDate debut = adjustedLast.toLocalDate();
        LocalDate fin = adjustedNext.toLocalDate();
        if (fin.isBefore(debut)) {
            fin = debut;
        }
        return new DateRange(debut, fin);
    }

    private static OffsetDateTime applyMarge(OffsetDateTime dateTime, ProcessusParametre job) {
        int marge = job.getMarge() != null ? job.getMarge() : 0;
        ProcessusParametre.UniteMarge unite = job.getUniteMarge() != null ? job.getUniteMarge() : ProcessusParametre.UniteMarge.JOUR;
        return switch (unite) {
            case MINUTE -> dateTime.minusMinutes(marge);
            case HEURE -> dateTime.minusHours(marge);
            case JOUR -> dateTime.minusDays(marge);
        };
    }

    public static final class DateRange {
        private final LocalDate dateDebut;
        private final LocalDate dateFin;

        public DateRange(LocalDate dateDebut, LocalDate dateFin) {
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
        }

        public LocalDate getDateDebut() {
            return dateDebut;
        }

        public LocalDate getDateFin() {
            return dateFin;
        }
    }
}
