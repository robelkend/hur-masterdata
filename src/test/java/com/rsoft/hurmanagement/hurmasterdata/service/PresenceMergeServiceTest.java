package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceMergeServiceTest {

    @Mock
    private PresenceEmployeRepository presenceRepository;
    @Mock
    private PointageBrutRepository pointageBrutRepository;
    @Mock
    private EmploiEmployeRepository emploiEmployeRepository;
    @Mock
    private HoraireSpecialRepository horaireSpecialRepository;
    @Mock
    private HoraireDtRepository horaireDtRepository;
    @Mock
    private PresenceEmployeService presenceEmployeService;

    @InjectMocks
    private PresenceMergeService service;

    @Test
    void mergeDayAggregatesIntervalsAndPauseAndKeepsExistingNoPresence() {
        LocalDate day = LocalDate.of(2026, 1, 10);
        Employe employe = employe(1L);
        EmploiEmploye emploi = emploiWithHoraire(employe, "18:00", null, 2);
        HoraireDt dt = new HoraireDt();
        dt.setHeureDebutJour("08:00");
        dt.setHeureFinJour("17:00");

        PresenceEmploye p1 = presence(100L, employe, day, day, "08:00", "10:00");
        PresenceEmploye p2 = presence(101L, employe, day, day, "11:00", "13:00");

        PointageBrut pbNull = new PointageBrut();
        pbNull.setId(1L);
        pbNull.setRowscn(1);
        pbNull.setDateHeurePointage(OffsetDateTime.now());
        PointageBrut pbExisting = new PointageBrut();
        pbExisting.setId(2L);
        pbExisting.setRowscn(1);
        pbExisting.setDateHeurePointage(OffsetDateTime.now());

        when(presenceRepository.findForRearrange(day, day.plusDays(1), 1L, null)).thenReturn(List.of(p1, p2));
        when(emploiEmployeRepository.findActiveForDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of());
        when(horaireDtRepository.findByHoraireIdAndJour(anyLong(), anyInt())).thenReturn(dt);
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pointageBrutRepository.findByPresenceEmployeIdOrderByDateHeurePointageAsc(100L)).thenReturn(List.of());
        when(pointageBrutRepository.findByPresenceEmployeIdOrderByDateHeurePointageAsc(101L)).thenReturn(List.of(pbNull, pbExisting));
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));

        int merged = service.mergeForRange(day, day, 1L, null, "tester");

        assertEquals(1, merged);
        ArgumentCaptor<PresenceEmploye> savedPresence = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(savedPresence.capture());
        PresenceEmploye keeper = savedPresence.getValue();
        assertEquals("08:00", keeper.getHeureArrivee());
        assertEquals("13:00", keeper.getHeureDepart());
        assertEquals(day, keeper.getDateDepart());
        assertEquals(60, keeper.getCumulPauseMin());

        ArgumentCaptor<List<PointageBrut>> pointagesCaptor = ArgumentCaptor.forClass(List.class);
        verify(pointageBrutRepository).saveAll(pointagesCaptor.capture());
        List<PointageBrut> savedPointages = pointagesCaptor.getValue();
        assertNotNull(savedPointages);
        assertEquals(2, savedPointages.size());
        assertEquals(100L, savedPointages.stream().filter(p -> p.getId().equals(1L)).findFirst().orElseThrow().getPresenceEmploye().getId());
        assertEquals(100L, savedPointages.stream().filter(p -> p.getId().equals(2L)).findFirst().orElseThrow().getPresenceEmploye().getId());
    }

    @Test
    void mergeDayUsesAutoCloseWhenLastEntryIsOpen() {
        LocalDate day = LocalDate.of(2026, 1, 10);
        Employe employe = employe(1L);
        EmploiEmploye emploi = emploiWithHoraire(employe, "18:00", null, 2);
        HoraireDt dt = new HoraireDt();
        dt.setHeureDebutJour("08:00");
        dt.setHeureFinJour("17:00");

        PresenceEmploye p1 = presence(200L, employe, day, day, "08:00", "12:00");
        PresenceEmploye p2 = presence(201L, employe, day, day, "13:00", "14:00");
        PresenceEmploye p3 = presence(202L, employe, day, day, "17:00", null);

        when(presenceRepository.findForRearrange(day, day.plusDays(1), 1L, null)).thenReturn(List.of(p1, p2, p3));
        when(emploiEmployeRepository.findActiveForDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of());
        when(horaireDtRepository.findByHoraireIdAndJour(anyLong(), anyInt())).thenReturn(dt);
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pointageBrutRepository.findByPresenceEmployeIdOrderByDateHeurePointageAsc(anyLong())).thenReturn(List.of());
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));

        int merged = service.mergeForRange(day, day, 1L, null, "tester");

        assertEquals(1, merged);
        ArgumentCaptor<PresenceEmploye> savedPresence = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(savedPresence.capture());
        PresenceEmploye keeper = savedPresence.getValue();
        assertEquals("08:00", keeper.getHeureArrivee());
        assertEquals("18:00", keeper.getHeureDepart());
    }

    @Test
    void mergeNightPlanFallsBackToDayWhenNoArrivalInsideNightWindow() {
        LocalDate day = LocalDate.of(2026, 1, 10);
        Employe employe = employe(1L);
        EmploiEmploye emploi = emploiWithHoraire(employe, "18:00", "06:00", 2);
        HoraireDt dt = new HoraireDt();
        dt.setHeureDebutJour("08:00");
        dt.setHeureFinJour("17:00");
        dt.setHeureDebutNuit("22:00");
        dt.setHeureFinNuit("06:00");

        PresenceEmploye p1 = presence(300L, employe, day, day, "09:00", "12:00");
        PresenceEmploye p2 = presence(301L, employe, day, day, "13:00", "17:00");

        when(presenceRepository.findForRearrange(day, day.plusDays(1), 1L, null)).thenReturn(List.of(p1, p2));
        when(emploiEmployeRepository.findActiveForDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of());
        when(horaireDtRepository.findByHoraireIdAndJour(anyLong(), anyInt())).thenReturn(dt);
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pointageBrutRepository.findByPresenceEmployeIdOrderByDateHeurePointageAsc(anyLong())).thenReturn(List.of());
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));

        int merged = service.mergeForRange(day, day, 1L, null, "tester");

        assertEquals(1, merged);
        ArgumentCaptor<PresenceEmploye> savedPresence = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(savedPresence.capture());
        PresenceEmploye keeper = savedPresence.getValue();
        assertEquals(day, keeper.getDateDepart());
        assertEquals("17:00", keeper.getHeureDepart());
    }

    @Test
    void mergeNightCrossDayUsesNextDayIntervalsAndDateDepartNextDay() {
        LocalDate day = LocalDate.of(2026, 1, 10);
        LocalDate next = day.plusDays(1);
        Employe employe = employe(1L);
        EmploiEmploye emploi = emploiWithHoraire(employe, "18:00", "06:00", 2);
        HoraireDt dt = new HoraireDt();
        dt.setHeureDebutNuit("22:00");
        dt.setHeureFinNuit("06:00");

        PresenceEmploye p1 = presence(400L, employe, day, day, "22:30", "23:30");
        PresenceEmploye p1b = presence(403L, employe, day, day, "23:30", "23:30");
        PresenceEmploye p2 = presence(401L, employe, next, next, "02:00", "04:00");
        PresenceEmploye outOfRange = presence(402L, employe, next, next, "08:00", "09:00");

        when(presenceRepository.findForRearrange(day, day.plusDays(1), 1L, null)).thenReturn(List.of(p1, p1b, p2, outOfRange));
        when(emploiEmployeRepository.findActiveForDate(eq(1L), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of());
        when(horaireDtRepository.findByHoraireIdAndJour(anyLong(), anyInt())).thenReturn(dt);
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pointageBrutRepository.findByPresenceEmployeIdOrderByDateHeurePointageAsc(anyLong())).thenReturn(List.of());
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));

        int merged = service.mergeForRange(day, day, 1L, null, "tester");

        assertEquals(1, merged);
        ArgumentCaptor<PresenceEmploye> savedPresence = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(savedPresence.capture());
        PresenceEmploye keeper = savedPresence.getValue();
        assertEquals("22:30", keeper.getHeureArrivee());
        assertEquals("04:00", keeper.getHeureDepart());
        assertEquals(next, keeper.getDateDepart());
        assertEquals(150, keeper.getCumulPauseMin());

        verify(presenceRepository, times(2)).delete(any(PresenceEmploye.class));
    }

    @Test
    void mergeNightSpecialWithOnlySameDayPresencesTreatsMergeAsDay() {
        LocalDate day = LocalDate.of(2026, 1, 10);
        LocalDate specialEnd = day.plusDays(1);
        Employe employe = employe(1L);
        EmploiEmploye emploi = emploiWithHoraire(employe, "18:00", "06:00", 2);

        HoraireSpecial special = new HoraireSpecial();
        special.setDateDebut(day);
        special.setDateFin(specialEnd);
        special.setHeureDebut("22:00");
        special.setHeureFin("06:00");

        PresenceEmploye p1 = presence(500L, employe, day, day, "21:00", "22:00");
        PresenceEmploye p2 = presence(501L, employe, day, day, "23:30", null);

        when(presenceRepository.findForRearrange(day, day.plusDays(1), 1L, null)).thenReturn(List.of(p1, p2));
        when(emploiEmployeRepository.findActiveForDate(eq(1L), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(special));
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pointageBrutRepository.findByPresenceEmployeIdOrderByDateHeurePointageAsc(anyLong())).thenReturn(List.of());
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));

        int merged = service.mergeForRange(day, day, 1L, null, "tester");

        assertEquals(1, merged);
        ArgumentCaptor<PresenceEmploye> savedPresence = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(savedPresence.capture());
        PresenceEmploye keeper = savedPresence.getValue();
        assertEquals("18:00", keeper.getHeureArrivee());
        assertEquals("23:30", keeper.getHeureDepart());
        assertEquals(day, keeper.getDateDepart());
    }

    @Test
    void mergeNightSpecialSampleClosesLastThenMergesAsDay() {
        LocalDate day = LocalDate.of(2026, 2, 23);
        Employe employe = employe(23L);
        EmploiEmploye emploi = emploiWithHoraire(employe, "18:00", "06:00", 2);

        HoraireSpecial special = new HoraireSpecial();
        special.setDateDebut(day);
        special.setDateFin(day.plusDays(1));
        special.setHeureDebut("22:00");
        special.setHeureFin("06:00");

        PresenceEmploye pOpen = presence(600L, employe, day, day, "17:23", null);
        PresenceEmploye p1 = presence(601L, employe, day, day, "05:47", "11:36");
        PresenceEmploye p2 = presence(602L, employe, day, day, "12:00", "13:05");
        PresenceEmploye p3 = presence(603L, employe, day, day, "14:00", "15:00");

        when(presenceRepository.findForRearrange(day, day.plusDays(1), 23L, null)).thenReturn(List.of(pOpen, p1, p2, p3));
        when(emploiEmployeRepository.findActiveForDate(eq(23L), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(special));
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pointageBrutRepository.findByPresenceEmployeIdOrderByDateHeurePointageAsc(anyLong())).thenReturn(List.of());
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));

        int merged = service.mergeForRange(day, day, 23L, null, "tester");

        assertEquals(1, merged);
        ArgumentCaptor<PresenceEmploye> savedPresence = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(savedPresence.capture());
        PresenceEmploye keeper = savedPresence.getValue();
        assertEquals("05:47", keeper.getHeureArrivee());
        assertEquals("18:00", keeper.getHeureDepart());
        assertEquals(day, keeper.getDateDepart());
    }

    @Test
    void mergeNightPlannedCrossDayAllClosedKeepsFirstStartAndLastNextDayEnd() {
        LocalDate day = LocalDate.of(2026, 2, 23);
        LocalDate next = day.plusDays(1);
        Employe employe = employe(23L);
        EmploiEmploye emploi = emploiWithHoraire(employe, "18:00", "06:00", 2);

        HoraireSpecial special = new HoraireSpecial();
        special.setDateDebut(day);
        special.setDateFin(next);
        special.setHeureDebut("17:00");
        special.setHeureFin("04:00");

        PresenceEmploye p1 = presence(700L, employe, day, day, "17:00", "19:00");
        PresenceEmploye p2 = presence(701L, employe, day, day, "20:00", "23:45");
        PresenceEmploye p3 = presence(702L, employe, day, next, "01:00", "06:00");

        when(presenceRepository.findForRearrange(day, day.plusDays(1), 23L, null)).thenReturn(List.of(p1, p2, p3));
        when(emploiEmployeRepository.findActiveForDate(eq(23L), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(special));
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pointageBrutRepository.findByPresenceEmployeIdOrderByDateHeurePointageAsc(anyLong())).thenReturn(List.of());
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));

        int merged = service.mergeForRange(day, day, 23L, null, "tester");

        assertEquals(1, merged);
        ArgumentCaptor<PresenceEmploye> savedPresence = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(savedPresence.capture());
        PresenceEmploye keeper = savedPresence.getValue();
        assertEquals(day, keeper.getDateJour());
        assertEquals(next, keeper.getDateDepart());
        assertEquals("17:00", keeper.getHeureArrivee());
        assertEquals("06:00", keeper.getHeureDepart());
    }

    @Test
    void mergeNightPlannedCrossDayClosesLastOpenWithAutoCloseNightThenMerges() {
        LocalDate day = LocalDate.of(2026, 2, 23);
        LocalDate next = day.plusDays(1);
        Employe employe = employe(23L);
        EmploiEmploye emploi = emploiWithHoraire(employe, "18:00", "06:00", 2);

        HoraireSpecial special = new HoraireSpecial();
        special.setDateDebut(day);
        special.setDateFin(next);
        special.setHeureDebut("17:00");
        special.setHeureFin("04:00");

        PresenceEmploye p1 = presence(710L, employe, day, day, "17:45", "19:25");
        PresenceEmploye p2 = presence(711L, employe, day, day, "20:07", "23:32");
        PresenceEmploye p3 = presence(712L, employe, day, next, "01:00", "04:00");
        PresenceEmploye p4 = presence(713L, employe, day, next, "04:25", null);

        when(presenceRepository.findForRearrange(day, day.plusDays(1), 23L, null)).thenReturn(List.of(p1, p2, p3, p4));
        when(emploiEmployeRepository.findActiveForDate(eq(23L), any(LocalDate.class))).thenReturn(List.of(emploi));
        when(horaireSpecialRepository.findActiveByEmployeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(List.of(special));
        when(presenceRepository.save(any(PresenceEmploye.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pointageBrutRepository.findByPresenceEmployeIdOrderByDateHeurePointageAsc(anyLong())).thenReturn(List.of());
        doNothing().when(presenceEmployeService).applyDerivedFields(any(PresenceEmploye.class));

        int merged = service.mergeForRange(day, day, 23L, null, "tester");

        assertEquals(1, merged);
        ArgumentCaptor<PresenceEmploye> savedPresence = ArgumentCaptor.forClass(PresenceEmploye.class);
        verify(presenceRepository, atLeastOnce()).save(savedPresence.capture());
        PresenceEmploye keeper = savedPresence.getValue();
        assertEquals(day, keeper.getDateJour());
        assertEquals(next, keeper.getDateDepart());
        assertEquals("17:45", keeper.getHeureArrivee());
        assertEquals("06:00", keeper.getHeureDepart());
    }

    private Employe employe(Long id) {
        Employe employe = new Employe();
        employe.setId(id);
        return employe;
    }

    private EmploiEmploye emploiWithHoraire(Employe employe, String autoCloseDay, String autoCloseNight, Integer defaultOvertime) {
        Horaire horaire = new Horaire();
        horaire.setId(10L);
        horaire.setHeureFermetureAutoJour(autoCloseDay);
        horaire.setHeureFermetureAutoNuit(autoCloseNight);
        horaire.setDefaultNbHovertime(defaultOvertime);

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setHoraire(horaire);
        return emploi;
    }

    private PresenceEmploye presence(Long id,
                                     Employe employe,
                                     LocalDate dateJour,
                                     LocalDate dateDepart,
                                     String heureArrivee,
                                     String heureDepart) {
        PresenceEmploye presence = new PresenceEmploye();
        presence.setId(id);
        presence.setEmploye(employe);
        presence.setDateJour(dateJour);
        presence.setDateDepart(dateDepart);
        presence.setHeureArrivee(heureArrivee);
        presence.setHeureDepart(heureDepart);
        presence.setRowscn(1);
        return presence;
    }
}
