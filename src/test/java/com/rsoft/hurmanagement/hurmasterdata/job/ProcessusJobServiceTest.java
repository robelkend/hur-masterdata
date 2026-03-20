package com.rsoft.hurmanagement.hurmasterdata.job;

import com.rsoft.hurmanagement.hurmasterdata.entity.ProcessusParametre;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceLoadingRepository;
import com.rsoft.hurmanagement.hurmasterdata.service.CongeEmployeFinalizeService;
import com.rsoft.hurmanagement.hurmasterdata.service.CongeEmployeService;
import com.rsoft.hurmanagement.hurmasterdata.service.HoraireSpecialService;
import com.rsoft.hurmanagement.hurmasterdata.service.InterfaceLoadingService;
import com.rsoft.hurmanagement.hurmasterdata.service.PresenceBuilderService;
import com.rsoft.hurmanagement.hurmasterdata.service.PresenceRearrangeService;
import com.rsoft.hurmanagement.hurmasterdata.service.PrimePresenceService;
import com.rsoft.hurmanagement.hurmasterdata.service.SupplementaireEmployeService;
import com.rsoft.hurmanagement.hurmasterdata.service.MutationEmployeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessusJobServiceTest {

    @Mock
    private InterfaceLoadingRepository interfaceLoadingRepository;
    @Mock
    private InterfaceLoadingService interfaceLoadingService;
    @Mock
    private PresenceBuilderService presenceBuilderService;
    @Mock
    private PresenceRearrangeService presenceRearrangeService;
    @Mock
    private CongeEmployeFinalizeService congeEmployeFinalizeService;
    @Mock
    private CongeEmployeService congeEmployeService;
    @Mock
    private SupplementaireEmployeService supplementaireEmployeService;
    @Mock
    private HoraireSpecialService horaireSpecialService;
    @Mock
    private PrimePresenceService primePresenceService;
    @Mock
    private MutationEmployeService mutationEmployeService;

    @InjectMocks
    private ProcessusJobService service;

    @Test
    void runPrimePresenceAppliesMargeToLastAndNextDates() {
        ProcessusParametre job = new ProcessusParametre();
        job.setCodeProcessus("PRIME_PRESENCE");
        job.setDerniereExecutionAt(OffsetDateTime.parse("2026-03-01T00:00:00Z"));
        job.setProchaineExecutionAt(OffsetDateTime.parse("2026-03-08T00:00:00Z"));
        job.setMarge(1);
        job.setUniteMarge(ProcessusParametre.UniteMarge.JOUR);

        when(primePresenceService.generatePrimePresence(eq(java.time.LocalDate.parse("2026-02-28")),
                eq(java.time.LocalDate.parse("2026-03-07")),
                eq(null),
                eq("tester")))
                .thenReturn(Map.of("success", true));

        service.runProcessus(job, "tester");

        verify(primePresenceService).generatePrimePresence(
                eq(java.time.LocalDate.parse("2026-02-28")),
                eq(java.time.LocalDate.parse("2026-03-07")),
                eq(null),
                eq("tester")
        );
    }

    @Test
    void runProcessLeaveStartsAndFinalizesForTargetDate() {
        ProcessusParametre job = new ProcessusParametre();
        job.setCodeProcessus("process_leave");
        job.setProchaineExecutionAt(OffsetDateTime.parse("2026-03-10T00:00:00Z"));

        when(congeEmployeService.autoStartApprovedForDate(any(java.time.LocalDate.class), eq(null), eq("tester")))
                .thenReturn(Map.of("success", true));
        when(congeEmployeFinalizeService.autoFinalizeForDate(any(java.time.LocalDate.class), eq(null), eq("tester")))
                .thenReturn(Map.of("success", true));

        service.runProcessus(job, "tester");

        org.mockito.ArgumentCaptor<java.time.LocalDate> startDateCaptor =
                org.mockito.ArgumentCaptor.forClass(java.time.LocalDate.class);
        org.mockito.ArgumentCaptor<java.time.LocalDate> finalizeDateCaptor =
                org.mockito.ArgumentCaptor.forClass(java.time.LocalDate.class);
        verify(congeEmployeService).autoStartApprovedForDate(startDateCaptor.capture(), eq(null), eq("tester"));
        verify(congeEmployeFinalizeService).autoFinalizeForDate(finalizeDateCaptor.capture(), eq(null), eq("tester"));
        assertNotNull(startDateCaptor.getValue());
        assertNotNull(finalizeDateCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals(startDateCaptor.getValue(), finalizeDateCaptor.getValue());
    }

    @Test
    void runFinalizeLeaveAliasAlsoRunsProcessLeaveFlow() {
        ProcessusParametre job = new ProcessusParametre();
        job.setCodeProcessus("FINALIZE_LEAVE");
        job.setProchaineExecutionAt(OffsetDateTime.parse("2026-03-11T00:00:00Z"));

        when(congeEmployeService.autoStartApprovedForDate(any(java.time.LocalDate.class), eq(null), eq("tester")))
                .thenReturn(Map.of("success", true));
        when(congeEmployeFinalizeService.autoFinalizeForDate(any(java.time.LocalDate.class), eq(null), eq("tester")))
                .thenReturn(Map.of("success", true));

        service.runProcessus(job, "tester");

        verify(congeEmployeService, times(1)).autoStartApprovedForDate(any(java.time.LocalDate.class), eq(null), eq("tester"));
        verify(congeEmployeFinalizeService, times(1)).autoFinalizeForDate(any(java.time.LocalDate.class), eq(null), eq("tester"));
    }

    @Test
    void runFinalizeMutationAppliesApprovedMutationsForCurrentDate() {
        ProcessusParametre job = new ProcessusParametre();
        job.setCodeProcessus("finalize_mutation");

        when(mutationEmployeService.autoApplyApprovedForCurrentDate(eq(null), eq("tester")))
                .thenReturn(Map.of("success", true));

        service.runProcessus(job, "tester");

        verify(mutationEmployeService, times(1))
                .autoApplyApprovedForCurrentDate(eq(null), eq("tester"));
    }

    @Test
    void runEmployeeReturnCreatesAndAppliesReintegrationForExpiredSuspensions() {
        ProcessusParametre job = new ProcessusParametre();
        job.setCodeProcessus("employee_return");

        when(mutationEmployeService.autoCreateAndApplyReintegrationForExpiredSuspensions(eq(null), eq("tester")))
                .thenReturn(Map.of("success", true));

        service.runProcessus(job, "tester");

        verify(mutationEmployeService, times(1))
                .autoCreateAndApplyReintegrationForExpiredSuspensions(eq(null), eq("tester"));
    }
}
