package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeAgregatDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeAgregatRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollPeriodeBoniRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollEmployeAgregatServiceTest {

    @Mock
    private PayrollEmployeAgregatRepository agregatRepository;

    @Mock
    private PayrollPeriodeBoniRepository periodeBoniRepository;

    @Mock
    private PayrollEmployeAgregatDeductionRepository agregatDeductionRepository;

    @InjectMocks
    private PayrollEmployeAgregatService service;

    @Test
    void aggregateUsesExistingPeriodeBoniWhenDateInsideRange() {
        Payroll payroll = buildPayroll(LocalDate.parse("2026-03-15"));
        PayrollEmploye payrollEmploye = buildPayrollEmploye(10L, "100", "20", "30", "5");

        PayrollPeriodeBoni periode = new PayrollPeriodeBoni();
        periode.setId(1L);
        periode.setDateDebut(LocalDate.parse("2026-01-01"));
        periode.setDateFin(LocalDate.parse("2026-12-31"));
        when(periodeBoniRepository.findContainingDate(payroll.getDateFin())).thenReturn(Optional.of(periode));
        when(agregatRepository.findByRegimePaieIdAndEmployeIdAndPeriodeBoniId(1L, 10L, 1L)).thenReturn(Optional.empty());

        service.aggregateForValidatedPayroll(payroll, List.of(payrollEmploye), "tester");

        ArgumentCaptor<PayrollEmployeAgregat> captor = ArgumentCaptor.forClass(PayrollEmployeAgregat.class);
        verify(agregatRepository).save(captor.capture());
        PayrollEmployeAgregat saved = captor.getValue();
        assertEquals(0, new BigDecimal("100").compareTo(saved.getMontantSalaireBase()));
        assertEquals(0, new BigDecimal("20").compareTo(saved.getMontantSupplementaire()));
        assertEquals(0, new BigDecimal("30").compareTo(saved.getMontantAutreRevenu()));
        assertEquals(0, new BigDecimal("5").compareTo(saved.getMontantSanctions()));
        assertEquals(5, saved.getNoPeriode());
        assertEquals(1, saved.getNbPaie());
    }

    @Test
    void aggregateSkipsWhenNoPeriodeBoniExists() {
        Payroll payroll = buildPayroll(LocalDate.parse("2026-03-15"));
        PayrollEmploye payrollEmploye = buildPayrollEmploye(10L, "100", "20", "30", "5");

        when(periodeBoniRepository.findContainingDate(payroll.getDateFin())).thenReturn(Optional.empty());
        when(periodeBoniRepository.findTopByOrderByDateFinDesc()).thenReturn(Optional.empty());

        service.aggregateForValidatedPayroll(payroll, List.of(payrollEmploye), "tester");

        verify(agregatRepository, never()).save(any(PayrollEmployeAgregat.class));
    }

    @Test
    void aggregateCreatesNewPeriodeWhenYearChangedAndThenAggregates() {
        Payroll payroll = buildPayroll(LocalDate.parse("2026-01-31"));
        PayrollEmploye payrollEmploye = buildPayrollEmploye(10L, "80", "10", "5", "2");

        PayrollPeriodeBoni latest = new PayrollPeriodeBoni();
        latest.setId(2L);
        latest.setCode("2025-2025");
        latest.setLibelle("Periode annuelle");
        latest.setDateDebut(LocalDate.parse("2025-01-01"));
        latest.setDateFin(LocalDate.parse("2025-12-31"));
        latest.setStatut(PayrollPeriodeBoni.Statut.INACTIF);

        PayrollPeriodeBoni created = new PayrollPeriodeBoni();
        created.setId(3L);
        created.setCode("2026-2026");
        created.setLibelle("Periode annuelle");
        created.setDateDebut(LocalDate.parse("2026-01-01"));
        created.setDateFin(LocalDate.parse("2026-12-31"));
        created.setStatut(PayrollPeriodeBoni.Statut.INACTIF);

        when(periodeBoniRepository.findContainingDate(payroll.getDateFin())).thenReturn(Optional.empty());
        when(periodeBoniRepository.findTopByOrderByDateFinDesc()).thenReturn(Optional.of(latest));
        when(periodeBoniRepository.save(any(PayrollPeriodeBoni.class))).thenReturn(created);
        when(agregatRepository.findByRegimePaieIdAndEmployeIdAndPeriodeBoniId(1L, 10L, 3L)).thenReturn(Optional.empty());

        service.aggregateForValidatedPayroll(payroll, List.of(payrollEmploye), "tester");

        verify(periodeBoniRepository).save(any(PayrollPeriodeBoni.class));
        verify(agregatRepository).save(any(PayrollEmployeAgregat.class));
    }

    @Test
    void aggregateIncrementsNbPaieWhenAgregatAlreadyExists() {
        Payroll payroll = buildPayroll(LocalDate.parse("2026-03-15"));
        PayrollEmploye payrollEmploye = buildPayrollEmploye(10L, "100", "20", "30", "5");

        PayrollPeriodeBoni periode = new PayrollPeriodeBoni();
        periode.setId(1L);
        periode.setDateDebut(LocalDate.parse("2026-01-01"));
        periode.setDateFin(LocalDate.parse("2026-12-31"));

        PayrollEmployeAgregat existing = new PayrollEmployeAgregat();
        existing.setRegimePaie(payroll.getRegimePaie());
        existing.setEmploye(payrollEmploye.getEmploye());
        existing.setPeriodeBoni(periode);
        existing.setMontantSalaireBase(new BigDecimal("200"));
        existing.setMontantSupplementaire(new BigDecimal("40"));
        existing.setMontantAutreRevenu(new BigDecimal("60"));
        existing.setMontantSanctions(new BigDecimal("10"));
        existing.setNoPeriode(2);
        existing.setNbPaie(3);

        when(periodeBoniRepository.findContainingDate(payroll.getDateFin())).thenReturn(Optional.of(periode));
        when(agregatRepository.findByRegimePaieIdAndEmployeIdAndPeriodeBoniId(1L, 10L, 1L)).thenReturn(Optional.of(existing));

        service.aggregateForValidatedPayroll(payroll, List.of(payrollEmploye), "tester");

        ArgumentCaptor<PayrollEmployeAgregat> captor = ArgumentCaptor.forClass(PayrollEmployeAgregat.class);
        verify(agregatRepository).save(captor.capture());
        PayrollEmployeAgregat saved = captor.getValue();
        assertEquals(5, saved.getNoPeriode());
        assertEquals(4, saved.getNbPaie());
        assertEquals(0, new BigDecimal("300").compareTo(saved.getMontantSalaireBase()));
    }

    private Payroll buildPayroll(LocalDate dateFin) {
        RegimePaie regime = new RegimePaie();
        regime.setId(1L);
        regime.setPeriodePaieCourante(5);
        Payroll payroll = new Payroll();
        payroll.setId(100L);
        payroll.setRegimePaie(regime);
        payroll.setDateFin(dateFin);
        return payroll;
    }

    private PayrollEmploye buildPayrollEmploye(Long employeId, String base, String supp, String revenu, String sanctions) {
        Employe employe = new Employe();
        employe.setId(employeId);
        PayrollEmploye payrollEmploye = new PayrollEmploye();
        payrollEmploye.setEmploye(employe);
        payrollEmploye.setMontantSalaireBase(new BigDecimal(base));
        payrollEmploye.setMontantSupplementaire(new BigDecimal(supp));
        payrollEmploye.setMontantAutreRevenu(new BigDecimal(revenu));
        payrollEmploye.setMontantSanctions(new BigDecimal(sanctions));
        return payrollEmploye;
    }
}
