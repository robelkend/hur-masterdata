package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.CongeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.JourConge;
import com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.BalanceCongeAnneeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.BalanceCongeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.CongeEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.JourCongeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PresenceEmployeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CongeEmployeFinalizeServiceTest {

    @Mock
    private CongeEmployeRepository congeEmployeRepository;
    @Mock
    private BalanceCongeRepository balanceCongeRepository;
    @Mock
    private BalanceCongeAnneeRepository balanceCongeAnneeRepository;
    @Mock
    private EmploiEmployeRepository emploiEmployeRepository;
    @Mock
    private PresenceEmployeRepository presenceEmployeRepository;
    @Mock
    private JourCongeRepository jourCongeRepository;

    @InjectMocks
    private CongeEmployeFinalizeService service;

    @Test
    void autoFinalizeForDateUsesPresenceDateJourFromDateDebutPlan() {
        LocalDate startDate = LocalDate.parse("2026-03-01");
        LocalDate targetDate = LocalDate.parse("2026-03-08");

        CongeEmploye conge = buildEnCoursConge(101L, 23L, startDate);

        when(congeEmployeRepository.findByStatut(CongeEmploye.StatutConge.EN_COURS))
                .thenReturn(List.of(conge));
        when(presenceEmployeRepository
                .existsByEmployeIdAndDateJourGreaterThanEqualAndStatutPresence(
                        23L, startDate, PresenceEmploye.StatutPresence.VALIDE))
                .thenReturn(true);
        when(congeEmployeRepository.findById(101L)).thenReturn(Optional.of(conge));
        when(congeEmployeRepository.save(any(CongeEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.autoFinalizeForDate(targetDate, null, "tester");

        assertEquals(1, result.get("finalizedRows"));
        assertEquals(0, result.get("skippedNoPresence"));
        verify(presenceEmployeRepository)
                .existsByEmployeIdAndDateJourGreaterThanEqualAndStatutPresence(
                        23L, startDate, PresenceEmploye.StatutPresence.VALIDE);
    }

    @Test
    void autoFinalizeForDateDoesNotDependOnTargetDateWhenPresenceExistsAfterStart() {
        LocalDate startDate = LocalDate.parse("2026-03-08");
        LocalDate targetDate = LocalDate.parse("2026-03-01");

        CongeEmploye conge = buildEnCoursConge(102L, 24L, startDate);

        when(congeEmployeRepository.findByStatut(CongeEmploye.StatutConge.EN_COURS))
                .thenReturn(List.of(conge));
        when(presenceEmployeRepository
                .existsByEmployeIdAndDateJourGreaterThanEqualAndStatutPresence(
                        24L, startDate, PresenceEmploye.StatutPresence.VALIDE))
                .thenReturn(true);
        when(congeEmployeRepository.findById(102L)).thenReturn(Optional.of(conge));
        when(congeEmployeRepository.save(any(CongeEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.autoFinalizeForDate(targetDate, null, "tester");

        assertEquals(1, result.get("finalizedRows"));
        assertEquals(0, result.get("skippedNoPresence"));
    }

    @Test
    void finalizeCongeFillsRealDatesFromPlanWhenMissingEvenInManualMode() {
        LocalDate startDate = LocalDate.parse("2026-03-02");
        CongeEmploye conge = buildEnCoursConge(103L, 25L, startDate);
        conge.setDateDebutReel(null);
        conge.setDateFinReel(null);

        when(congeEmployeRepository.findById(103L)).thenReturn(Optional.of(conge));
        when(congeEmployeRepository.save(any(CongeEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CongeEmploye saved = service.finalizeConge(103L, "tester", false);

        assertEquals(startDate, saved.getDateDebutReel());
        assertEquals(startDate.plusDays(2), saved.getDateFinReel());
        assertEquals(CongeEmploye.StatutConge.TERMINE, saved.getStatut());
    }

    @Test
    void finalizeCongeExcludesOffDaysAndHolidaysFromNbJoursReel() {
        LocalDate startDate = LocalDate.parse("2025-12-31");
        LocalDate endDate = LocalDate.parse("2026-01-12");
        CongeEmploye conge = buildEnCoursConge(104L, 26L, startDate);
        conge.setDateFinPlan(endDate);
        conge.setDateDebutReel(startDate);
        conge.setDateFinReel(endDate);

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setJourOff1(7); // Sunday
        conge.setEmploiEmploye(emploi);

        when(jourCongeRepository.existsByDateCongeAndActif(any(LocalDate.class), eq(JourConge.Actif.Y)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(0);
                    return LocalDate.parse("2026-01-01").equals(date) || LocalDate.parse("2026-01-02").equals(date);
                });
        when(congeEmployeRepository.findById(104L)).thenReturn(Optional.of(conge));
        when(congeEmployeRepository.save(any(CongeEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CongeEmploye saved = service.finalizeConge(104L, "tester", false);

        assertEquals("9.00", saved.getNbJoursReel().toPlainString());
        assertEquals(CongeEmploye.StatutConge.TERMINE, saved.getStatut());
    }

    private CongeEmploye buildEnCoursConge(Long congeId, Long employeId, LocalDate dateDebutPlan) {
        Employe employe = new Employe();
        employe.setId(employeId);

        CongeEmploye conge = new CongeEmploye();
        conge.setId(congeId);
        conge.setEmploye(employe);
        conge.setDateDebutPlan(dateDebutPlan);
        conge.setDateFinPlan(dateDebutPlan.plusDays(2));
        conge.setStatut(CongeEmploye.StatutConge.EN_COURS);
        conge.setRowscn(1);
        return conge;
    }
}
