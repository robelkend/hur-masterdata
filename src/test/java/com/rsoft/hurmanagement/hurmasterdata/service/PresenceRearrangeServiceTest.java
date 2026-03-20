package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireSpecial;
import com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireDtRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireSpecialRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PresenceEmployeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceRearrangeServiceTest {

    @Mock
    private PresenceEmployeRepository presenceRepository;
    @Mock
    private EmploiEmployeRepository emploiEmployeRepository;
    @Mock
    private HoraireSpecialRepository horaireSpecialRepository;
    @Mock
    private HoraireDtRepository horaireDtRepository;
    @Mock
    private PresenceEmployeService presenceEmployeService;
    @Mock
    private PresenceMergeService presenceMergeService;

    @InjectMocks
    private PresenceRearrangeService service;

    @Test
    void closeAndRearrangeRunsMergeBeforeCloseAndReturnsCounters() {
        LocalDate day = LocalDate.of(2026, 1, 10);
        when(presenceMergeService.mergeForRange(day, day, 1L, 99L, "tester")).thenReturn(3);
        when(presenceRepository.findForRearrange(day, day, 1L, 99L)).thenReturn(List.of());

        Map<String, Object> result = service.closeAndRearrange("2026-01-10", "2026-01-10", 1L, 99L, "tester");

        verify(presenceMergeService).mergeForRange(day, day, 1L, 99L, "tester");
        assertEquals(3, result.get("mergedRows"));
        assertEquals(0, result.get("closedRows"));
        assertEquals(0, result.get("rearrangedRows"));
        assertEquals(0, result.get("totalRows"));
    }

    @Test
    void rearrangeSwapsDayPresenceWithNightHoraireSpecialAndUsesSpecialEndDate() {
        LocalDate day = LocalDate.of(2026, 3, 5);
        LocalDate specialEnd = day.plusDays(2);

        Employe employe = new Employe();
        employe.setId(1L);

        PresenceEmploye presence = new PresenceEmploye();
        presence.setId(10L);
        presence.setEmploye(employe);
        presence.setDateJour(day);
        presence.setDateDepart(day);
        presence.setHeureArrivee("08:00");
        presence.setHeureDepart("17:00");
        presence.setRowscn(1);

        HoraireSpecial special = new HoraireSpecial();
        special.setDateDebut(day);
        special.setDateFin(specialEnd);
        special.setHeureDebut("22:00");
        special.setHeureFin("06:00");

        when(presenceMergeService.mergeForRange(day, day, 1L, null, "tester")).thenReturn(0);
        when(presenceRepository.findForRearrange(day, day, 1L, null)).thenReturn(List.of(presence));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(special));
        when(emploiEmployeRepository.findActiveForDate(anyLong(), any(LocalDate.class))).thenReturn(List.of());
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = service.closeAndRearrange(day.toString(), day.toString(), 1L, null, "tester");

        ArgumentCaptor<PresenceEmploye> captor = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(captor.capture());
        PresenceEmploye updated = captor.getValue();

        assertNotNull(updated);
        assertEquals("17:00", updated.getHeureArrivee());
        assertEquals("08:00", updated.getHeureDepart());
        assertEquals(specialEnd, updated.getDateDepart());
        assertEquals(1, result.get("rearrangedRows"));
    }
}
