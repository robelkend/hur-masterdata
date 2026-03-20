package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PayrollServiceTaxCycleTest {

    @Mock private PayrollRepository payrollRepository;
    @Mock private PayrollEmployeRepository payrollEmployeRepository;
    @Mock private PayrollGainRepository payrollGainRepository;
    @Mock private PayrollDeductionRepository payrollDeductionRepository;
    @Mock private PayrollSanctionRepository payrollSanctionRepository;
    @Mock private PayrollRecouvrementRepository payrollRecouvrementRepository;
    @Mock private PayrollEmployeStatsRepository payrollEmployeStatsRepository;
    @Mock private RegimePaieRepository regimePaieRepository;
    @Mock private EmployeSalaireRepository employeSalaireRepository;
    @Mock private SupplementaireEmployeRepository supplementaireEmployeRepository;
    @Mock private AutreRevenuEmployeRepository autreRevenuEmployeRepository;
    @Mock private TypeRevenuRepository typeRevenuRepository;
    @Mock private PresenceEmployeRepository presenceEmployeRepository;
    @Mock private AbsenceEmployeRepository absenceEmployeRepository;
    @Mock private JourCongeRepository jourCongeRepository;
    @Mock private SanctionEmployeRepository sanctionEmployeRepository;
    @Mock private CoordonneeBancaireEmployeRepository coordonneeBancaireEmployeRepository;
    @Mock private DefinitionDeductionRepository definitionDeductionRepository;
    @Mock private RubriquePaieRepository rubriquePaieRepository;
    @Mock private RubriquePaieDeductionRepository rubriquePaieDeductionRepository;
    @Mock private RegimePaieDeductionRepository regimePaieDeductionRepository;
    @Mock private TrancheBaremeDeductionRepository trancheBaremeDeductionRepository;
    @Mock private ExclusionDeductionRepository exclusionDeductionRepository;
    @Mock private PlanAssuranceRepository planAssuranceRepository;
    @Mock private AssuranceEmployeRepository assuranceEmployeRepository;
    @Mock private PayrollTaxeCycleRepository payrollTaxeCycleRepository;
    @Mock private PretEmployeRepository pretEmployeRepository;
    @Mock private PretRemboursementRepository pretRemboursementRepository;
    @Mock private ProductionPieceRepository productionPieceRepository;
    @Mock private PayrollEmployeAgregatService payrollEmployeAgregatService;
    @Mock private PayrollEmployeAgregatDeductionService payrollEmployeAgregatDeductionService;
    @Mock private JdbcTemplate jdbcTemplate;

    private PayrollService payrollService;

    @BeforeEach
    void setup() {
        payrollService = new PayrollService(
                payrollRepository,
                payrollEmployeRepository,
                payrollGainRepository,
                payrollDeductionRepository,
                payrollSanctionRepository,
                payrollRecouvrementRepository,
                payrollEmployeStatsRepository,
                regimePaieRepository,
                employeSalaireRepository,
                supplementaireEmployeRepository,
                autreRevenuEmployeRepository,
                typeRevenuRepository,
                presenceEmployeRepository,
                absenceEmployeRepository,
                jourCongeRepository,
                sanctionEmployeRepository,
                coordonneeBancaireEmployeRepository,
                definitionDeductionRepository,
                rubriquePaieRepository,
                rubriquePaieDeductionRepository,
                regimePaieDeductionRepository,
                trancheBaremeDeductionRepository,
                exclusionDeductionRepository,
                planAssuranceRepository,
                assuranceEmployeRepository,
                payrollTaxeCycleRepository,
                pretEmployeRepository,
                pretRemboursementRepository,
                productionPieceRepository,
                payrollEmployeAgregatService,
                payrollEmployeAgregatDeductionService,
                jdbcTemplate
        );
    }

    @Test
    void taxesAggregateUntaxedPayrollsAcrossCyclesWhenEmployeeMissing() {
        RegimePaie regime = new RegimePaie();
        regime.setId(1L);
        regime.setPeriodicite(RegimePaie.Periodicite.QUINZAINE);
        regime.setTaxeChaqueNPaies(2);

        Employe employe = new Employe();
        employe.setId(10L);

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEnProbation("N");

        EmployeSalaire salaire = new EmployeSalaire();
        salaire.setEmploye(employe);
        salaire.setEmploi(emploi);
        salaire.setRegimePaie(regime);

        Payroll payroll1 = new Payroll();
        payroll1.setId(1L);
        payroll1.setRegimePaie(regime);
        payroll1.setDateFin(LocalDate.of(2026, 1, 15));

        Payroll payroll3 = new Payroll();
        payroll3.setId(3L);
        payroll3.setRegimePaie(regime);
        payroll3.setDateFin(LocalDate.of(2026, 2, 15));

        Payroll payroll4 = new Payroll();
        payroll4.setId(4L);
        payroll4.setRegimePaie(regime);
        payroll4.setDateFin(LocalDate.of(2026, 2, 28));

        PayrollEmploye pe1 = new PayrollEmploye();
        pe1.setId(11L);
        pe1.setPayroll(payroll1);
        pe1.setEmploye(employe);

        PayrollEmploye pe3 = new PayrollEmploye();
        pe3.setId(13L);
        pe3.setPayroll(payroll3);
        pe3.setEmploye(employe);

        PayrollEmploye current = new PayrollEmploye();
        current.setId(14L);
        current.setPayroll(payroll4);
        current.setEmploye(employe);

        RubriquePaie rubrique = new RubriquePaie();
        rubrique.setId(100L);
        rubrique.setImposable("Y");
        rubrique.setTaxesSpeciaux("N");

        PayrollGain gain1 = new PayrollGain();
        gain1.setRubriquePaie(rubrique);
        gain1.setMontant(new BigDecimal("100"));
        gain1.setImposable("Y");

        PayrollGain gain3 = new PayrollGain();
        gain3.setRubriquePaie(rubrique);
        gain3.setMontant(new BigDecimal("100"));
        gain3.setImposable("Y");

        PayrollGain gain4 = new PayrollGain();
        gain4.setRubriquePaie(rubrique);
        gain4.setMontant(new BigDecimal("100"));
        gain4.setImposable("Y");

        DefinitionDeduction deduction = new DefinitionDeduction();
        deduction.setId(5L);
        deduction.setCodeDeduction("TAX-ANN");
        deduction.setLibelle("Taxe annuelle");
        deduction.setTypeDeduction(DefinitionDeduction.TypeDeduction.PLAT);
        deduction.setBaseLimite(DefinitionDeduction.BaseLimite.ANNUEL);
        deduction.setValeur(new BigDecimal("1200"));
        deduction.setValeurCouvert(BigDecimal.ZERO);
        deduction.setSpecialise("N");
        deduction.setProbatoire("Y");
        deduction.setPctHorsCalcul(BigDecimal.ZERO);
        deduction.setMinPrelevement(BigDecimal.ZERO);
        deduction.setMaxPrelevement(BigDecimal.ZERO);

        when(payrollEmployeRepository.findForTaxeCycle(
                eq(10L),
                eq(1L),
                anyList(),
                isNull(),
                eq(payroll4.getDateFin())
        )).thenReturn(List.of(pe1, pe3));
        when(payrollGainRepository.findByPayrollEmployeId(11L)).thenReturn(List.of(gain1));
        when(payrollGainRepository.findByPayrollEmployeId(13L)).thenReturn(List.of(gain3));
        when(payrollGainRepository.findByPayrollEmployeId(14L)).thenReturn(List.of(gain4));

        RegimePaieDeduction regimeLink = new RegimePaieDeduction();
        regimeLink.setRegimePaie(regime);
        regimeLink.setDeductionCode(deduction);
        when(regimePaieDeductionRepository.findByRegimePaieId(1L)).thenReturn(List.of(regimeLink));
        when(trancheBaremeDeductionRepository.findByDefinitionDeductionIdOrderByBorneInfAsc(5L)).thenReturn(List.of());
        when(exclusionDeductionRepository.findByTypeEmployeId(anyLong())).thenReturn(List.of());
        when(planAssuranceRepository.findAll()).thenReturn(List.of());
        when(assuranceEmployeRepository.findByEmployeIdAndActif(anyLong(), anyString())).thenReturn(List.of());
        when(payrollRepository.countByRegimePaieIdAndStatutInAndDateFinRange(
                eq(1L),
                anyList(),
                isNull(),
                eq(payroll4.getDateFin())
        )).thenReturn(3L);
        when(payrollDeductionRepository.save(any(PayrollDeduction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(payrollTaxeCycleRepository.findByEmployeIdAndRegimePaieId(10L, 1L))
                .thenReturn(Optional.empty());
        when(payrollTaxeCycleRepository.save(any(PayrollTaxeCycle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal total = ReflectionTestUtils.invokeMethod(
                payrollService,
                "computeDeductionsForTaxCycle",
                salaire,
                current,
                payroll4,
                regime,
                "tester"
        );

        ArgumentCaptor<PayrollDeduction> deductionCaptor = ArgumentCaptor.forClass(PayrollDeduction.class);
        verify(payrollDeductionRepository).save(deductionCaptor.capture());

        PayrollDeduction saved = deductionCaptor.getValue();
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(saved.getMontant().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(saved.getBaseMontant().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void validateCallsPayrollEmployeAgregatService() {
        RegimePaie regime = new RegimePaie();
        regime.setId(1L);
        regime.setPeriodicite(RegimePaie.Periodicite.MENSUEL);
        regime.setTaxable("Y");

        Payroll payroll = new Payroll();
        payroll.setId(99L);
        payroll.setRegimePaie(regime);
        payroll.setDateFin(LocalDate.parse("2026-01-31"));
        payroll.setStatut(Payroll.StatutPayroll.CALCULE);
        payroll.setRowscn(1);

        when(payrollRepository.findById(99L)).thenReturn(Optional.of(payroll));
        when(pretRemboursementRepository.findByNoPayroll(99)).thenReturn(List.of());
        when(payrollEmployeRepository.findByPayrollId(99L)).thenReturn(List.of());
        when(regimePaieRepository.findById(1L)).thenReturn(Optional.of(regime));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(regimePaieRepository.save(any(RegimePaie.class))).thenAnswer(invocation -> invocation.getArgument(0));
        payrollService.validate(99L, "tester");

        verify(payrollEmployeAgregatService, times(1))
                .aggregateForValidatedPayroll(eq(payroll), anyList(), eq("tester"));
        verify(payrollEmployeAgregatDeductionService, times(1))
                .aggregateForValidatedPayroll(eq(payroll), anyList(), eq("tester"));
    }
}
