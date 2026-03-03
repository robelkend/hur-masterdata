package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Horaire;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireDt;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private PresenceRearrangeService service;

    @Test
    void mergeUsesAutoCloseWhenLastEntryOpen() {
        LocalDate jour = LocalDate.of(2026, 1, 10);
        Employe employe = new Employe();
        employe.setId(1L);

        Horaire horaire = new Horaire();
        horaire.setId(10L);
        horaire.setHeureFermetureAutoJour("18:00");

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setHoraire(horaire);

        PresenceEmploye p1 = new PresenceEmploye();
        p1.setId(1L);
        p1.setEmploye(employe);
        p1.setDateJour(jour);
        p1.setDateDepart(jour);
        p1.setHeureArrivee("06:00");
        p1.setHeureDepart("12:00");
        p1.setRowscn(1);

        PresenceEmploye p2 = new PresenceEmploye();
        p2.setId(2L);
        p2.setEmploye(employe);
        p2.setDateJour(jour);
        p2.setDateDepart(jour);
        p2.setHeureArrivee("13:00");
        p2.setHeureDepart("14:00");
        p2.setRowscn(1);

        PresenceEmploye p3 = new PresenceEmploye();
        p3.setId(3L);
        p3.setEmploye(employe);
        p3.setDateJour(jour);
        p3.setHeureArrivee("17:00");
        p3.setHeureDepart(null);
        p3.setRowscn(1);

        when(presenceRepository.findForRearrange(jour, jour, 1L, null)).thenReturn(List.of(p1, p2, p3));
        when(emploiEmployeRepository.findActiveForDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of());
        when(horaireDtRepository.findByHoraireIdAndJour(anyLong(), anyInt())).thenReturn(null);
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = service.closeAndRearrange("2026-01-10", "2026-01-10", 1L, null, "tester");

        ArgumentCaptor<PresenceEmploye> captor = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(captor.capture());
        PresenceEmploye merged = captor.getValue();

        assertNotNull(merged);
        assertEquals("06:00", merged.getHeureArrivee());
        assertEquals("18:00", merged.getHeureDepart());
        assertEquals(240, merged.getCumulPauseMin());
        assertEquals(jour, merged.getDateDepart());
        assertEquals(1, result.get("mergedRows"));

        verify(presenceRepository, times(2)).delete(any(PresenceEmploye.class));
    }

    @Test
    void mergeNightPlanUsesAutoCloseNextDay() {
        LocalDate jour = LocalDate.of(2026, 1, 10);
        Employe employe = new Employe();
        employe.setId(1L);

        Horaire horaire = new Horaire();
        horaire.setId(10L);
        horaire.setHeureFermetureAutoNuit("06:00");

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setHoraire(horaire);

        HoraireDt horaireDt = new HoraireDt();
        horaireDt.setHeureDebutNuit("22:00");
        horaireDt.setHeureFinNuit("06:00");

        PresenceEmploye p1 = new PresenceEmploye();
        p1.setId(1L);
        p1.setEmploye(employe);
        p1.setDateJour(jour);
        p1.setDateDepart(jour);
        p1.setHeureArrivee("22:00");
        p1.setHeureDepart("23:00");
        p1.setRowscn(1);

        PresenceEmploye p2 = new PresenceEmploye();
        p2.setId(2L);
        p2.setEmploye(employe);
        p2.setDateJour(jour);
        p2.setHeureArrivee("23:30");
        p2.setHeureDepart(null);
        p2.setRowscn(1);

        when(presenceRepository.findForRearrange(jour, jour, 1L, null)).thenReturn(List.of(p1, p2));
        when(emploiEmployeRepository.findActiveForDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of());
        when(horaireDtRepository.findByHoraireIdAndJour(anyLong(), anyInt())).thenReturn(horaireDt);
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = service.closeAndRearrange("2026-01-10", "2026-01-10", 1L, null, "tester");

        ArgumentCaptor<PresenceEmploye> captor = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(captor.capture());
        PresenceEmploye merged = captor.getValue();

        assertNotNull(merged);
        assertEquals("22:00", merged.getHeureArrivee());
        assertEquals("06:00", merged.getHeureDepart());
        assertEquals(jour.plusDays(1), merged.getDateDepart());
        assertEquals(30, merged.getCumulPauseMin());
        assertEquals(1, result.get("mergedRows"));

        verify(presenceRepository, times(1)).delete(any(PresenceEmploye.class));
    }

    @Test
    void mergeSpecialNightPlanUsesAutoCloseNightNextDay() {
        LocalDate jour = LocalDate.of(2026, 1, 10);
        Employe employe = new Employe();
        employe.setId(1L);

        Horaire horaire = new Horaire();
        horaire.setId(10L);
        horaire.setHeureFermetureAutoJour("18:00");
        horaire.setHeureFermetureAutoNuit("06:00");

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setHoraire(horaire);

        HoraireSpecial special = new HoraireSpecial();
        special.setDateDebut(jour);
        special.setDateFin(jour.plusDays(1));
        special.setHeureDebut("22:00");
        special.setHeureFin("06:00");

        PresenceEmploye p1 = new PresenceEmploye();
        p1.setId(1L);
        p1.setEmploye(employe);
        p1.setDateJour(jour);
        p1.setDateDepart(jour);
        p1.setHeureArrivee("22:05");
        p1.setHeureDepart("23:00");
        p1.setRowscn(1);

        PresenceEmploye p2 = new PresenceEmploye();
        p2.setId(2L);
        p2.setEmploye(employe);
        p2.setDateJour(jour);
        p2.setHeureArrivee("23:30");
        p2.setHeureDepart(null);
        p2.setRowscn(1);

        when(presenceRepository.findForRearrange(jour, jour, 1L, null)).thenReturn(List.of(p1, p2));
        when(emploiEmployeRepository.findActiveForDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(special));
        when(horaireDtRepository.findByHoraireIdAndJour(anyLong(), anyInt())).thenReturn(null);
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = service.closeAndRearrange("2026-01-10", "2026-01-10", 1L, null, "tester");

        ArgumentCaptor<PresenceEmploye> captor = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(captor.capture());
        PresenceEmploye merged = captor.getValue();

        assertNotNull(merged);
        assertEquals("22:05", merged.getHeureArrivee());
        assertEquals("06:00", merged.getHeureDepart());
        assertEquals(jour.plusDays(1), merged.getDateDepart());
        assertEquals(30, merged.getCumulPauseMin());
        assertEquals(1, result.get("mergedRows"));

        verify(presenceRepository, times(1)).delete(any(PresenceEmploye.class));
    }

    @Test
    void mergeNightAcrossTwoDatesUsesAutoCloseNight() {
        LocalDate jour = LocalDate.of(2026, 1, 10);
        LocalDate next = jour.plusDays(1);
        Employe employe = new Employe();
        employe.setId(1L);

        Horaire horaire = new Horaire();
        horaire.setId(10L);
        horaire.setHeureFermetureAutoNuit("06:00");

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setHoraire(horaire);

        HoraireDt horaireDt = new HoraireDt();
        horaireDt.setHeureDebutNuit("22:00");
        horaireDt.setHeureFinNuit("06:00");

        PresenceEmploye p1 = new PresenceEmploye();
        p1.setId(1L);
        p1.setEmploye(employe);
        p1.setDateJour(jour);
        p1.setDateDepart(next);
        p1.setHeureArrivee("23:00");
        p1.setHeureDepart("01:00");
        p1.setRowscn(1);

        PresenceEmploye p2 = new PresenceEmploye();
        p2.setId(2L);
        p2.setEmploye(employe);
        p2.setDateJour(jour);
        p2.setDateDepart(next);
        p2.setHeureArrivee("01:30");
        p2.setHeureDepart(null);
        p2.setRowscn(1);

        when(presenceRepository.findForRearrange(jour, jour, 1L, null)).thenReturn(List.of(p1, p2));
        when(emploiEmployeRepository.findActiveForDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of());
        when(horaireDtRepository.findByHoraireIdAndJour(anyLong(), anyInt())).thenReturn(horaireDt);
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = service.closeAndRearrange("2026-01-10", "2026-01-10", 1L, null, "tester");

        ArgumentCaptor<PresenceEmploye> captor = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(captor.capture());
        PresenceEmploye merged = captor.getValue();

        assertNotNull(merged);
        assertEquals("23:00", merged.getHeureArrivee());
        assertEquals("06:00", merged.getHeureDepart());
        assertEquals(next, merged.getDateDepart());
        assertEquals(30, merged.getCumulPauseMin());
        assertEquals(1, result.get("mergedRows"));

        verify(presenceRepository, times(1)).delete(any(PresenceEmploye.class));
    }

    @Test
    void mergeOverlappingEntriesKeepsPauseZero() {
        LocalDate jour = LocalDate.of(2026, 1, 10);
        LocalDate next = jour.plusDays(1);
        Employe employe = new Employe();
        employe.setId(1L);

        Horaire horaire = new Horaire();
        horaire.setId(10L);
        horaire.setHeureFermetureAutoNuit("06:00");

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setHoraire(horaire);

        HoraireDt horaireDt = new HoraireDt();
        horaireDt.setHeureDebutNuit("22:00");
        horaireDt.setHeureFinNuit("06:00");

        PresenceEmploye p1 = new PresenceEmploye();
        p1.setId(1L);
        p1.setEmploye(employe);
        p1.setDateJour(jour);
        p1.setDateDepart(next);
        p1.setHeureArrivee("22:00");
        p1.setHeureDepart("02:00");
        p1.setRowscn(1);

        PresenceEmploye p2 = new PresenceEmploye();
        p2.setId(2L);
        p2.setEmploye(employe);
        p2.setDateJour(jour);
        p2.setDateDepart(next);
        p2.setHeureArrivee("01:30");
        p2.setHeureDepart("03:00");
        p2.setRowscn(1);

        when(presenceRepository.findForRearrange(jour, jour, 1L, null)).thenReturn(List.of(p1, p2));
        when(emploiEmployeRepository.findActiveForDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of());
        when(horaireDtRepository.findByHoraireIdAndJour(anyLong(), anyInt())).thenReturn(horaireDt);
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));

        service.closeAndRearrange("2026-01-10", "2026-01-10", 1L, null, "tester");

        ArgumentCaptor<PresenceEmploye> captor = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(captor.capture());
        PresenceEmploye merged = captor.getValue();

        assertNotNull(merged);
        assertEquals("22:00", merged.getHeureArrivee());
        assertEquals("03:00", merged.getHeureDepart());
        assertEquals(next, merged.getDateDepart());
        assertEquals(0, merged.getCumulPauseMin());
    }
}
