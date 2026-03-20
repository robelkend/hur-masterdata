package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.DefinitionDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Formule;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollBoniDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeAgregat;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeBoni;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollPeriodeBoni;
import com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaie;
import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePaie;
import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePaieDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeRevenu;
import com.rsoft.hurmanagement.hurmasterdata.entity.TrancheBaremeDeduction;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollBoniDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeAgregatRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeBoniRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RubriquePaieDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RubriquePaieRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TrancheBaremeDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeRevenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BoniGenerationServiceTest {

    @Mock
    private PayrollEmployeAgregatRepository payrollEmployeAgregatRepository;
    @Mock
    private PayrollEmployeBoniRepository payrollEmployeBoniRepository;
    @Mock
    private PayrollBoniDeductionRepository payrollBoniDeductionRepository;
    @Mock
    private RubriquePaieRepository rubriquePaieRepository;
    @Mock
    private TypeRevenuRepository typeRevenuRepository;
    @Mock
    private EmploiEmployeRepository emploiEmployeRepository;
    @Mock
    private RubriquePaieDeductionRepository rubriquePaieDeductionRepository;
    @Mock
    private TrancheBaremeDeductionRepository trancheBaremeDeductionRepository;

    @InjectMocks
    private BoniGenerationService service;

    @Test
    void generateComputesDeductionsFromSpecialRubriqueDefinitionsOnly() {
        RubriquePaie rubrique = new RubriquePaie();
        rubrique.setId(50L);
        when(rubriquePaieRepository.findById(50L)).thenReturn(Optional.of(rubrique));
        when(typeRevenuRepository.findActifsByRubriquePaieAndEntreprisePreferEntreprise(50L, null))
                .thenReturn(List.of());

        Employe employe = new Employe();
        employe.setId(10L);
        employe.setCodeEmploye("E10");
        employe.setNom("Doe");
        employe.setPrenom("John");

        RegimePaie regime = new RegimePaie();
        regime.setId(1L);

        PayrollPeriodeBoni periode = new PayrollPeriodeBoni();
        periode.setId(5L);
        periode.setDateFin(LocalDate.of(2026, 12, 31));

        PayrollEmployeAgregat agregat = new PayrollEmployeAgregat();
        agregat.setEmploye(employe);
        agregat.setRegimePaie(regime);
        agregat.setPeriodeBoni(periode);
        agregat.setMontantSalaireBase(new BigDecimal("1000"));
        agregat.setMontantSupplementaire(new BigDecimal("200"));
        agregat.setMontantAutreRevenu(new BigDecimal("300"));
        agregat.setMontantSanctions(new BigDecimal("100"));
        when(payrollEmployeAgregatRepository.findForBoniGeneration(5L, null, null)).thenReturn(List.of(agregat));

        EmploiEmploye emploi = new EmploiEmploye();
        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setBaseCalculBoni(12);
        emploi.setTypeEmploye(typeEmploye);
        when(emploiEmployeRepository.findActiveForDate(10L, LocalDate.of(2026, 12, 31))).thenReturn(List.of(emploi));

        when(payrollEmployeBoniRepository.findByRegimePaieIdAndPeriodeBoniIdAndEmployeIdAndRubriquePaieId(1L, 5L, 10L, 50L))
                .thenReturn(Optional.empty());
        when(payrollEmployeBoniRepository.save(any(PayrollEmployeBoni.class))).thenAnswer(invocation -> {
            PayrollEmployeBoni entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(100L);
            }
            return entity;
        });

        DefinitionDeduction special = new DefinitionDeduction();
        special.setId(700L);
        special.setCodeDeduction("DED-S");
        special.setLibelle("Speciale");
        special.setSpecialise("Y");
        special.setTypeDeduction(DefinitionDeduction.TypeDeduction.POURCENTAGE);
        special.setValeur(new BigDecimal("10"));
        special.setValeurCouvert(BigDecimal.ZERO);
        special.setPctHorsCalcul(BigDecimal.ZERO);
        special.setMinPrelevement(BigDecimal.ZERO);
        special.setMaxPrelevement(BigDecimal.ZERO);
        special.setArrondir(DefinitionDeduction.Arrondir.CENTIEME);

        DefinitionDeduction nonSpecial = new DefinitionDeduction();
        nonSpecial.setId(701L);
        nonSpecial.setCodeDeduction("DED-N");
        nonSpecial.setLibelle("Non speciale");
        nonSpecial.setSpecialise("N");
        nonSpecial.setTypeDeduction(DefinitionDeduction.TypeDeduction.POURCENTAGE);
        nonSpecial.setValeur(new BigDecimal("20"));
        nonSpecial.setValeurCouvert(BigDecimal.ZERO);
        nonSpecial.setPctHorsCalcul(BigDecimal.ZERO);
        nonSpecial.setMinPrelevement(BigDecimal.ZERO);
        nonSpecial.setMaxPrelevement(BigDecimal.ZERO);
        nonSpecial.setArrondir(DefinitionDeduction.Arrondir.CENTIEME);

        RubriquePaieDeduction rpd1 = new RubriquePaieDeduction();
        rpd1.setDefinitionDeduction(special);
        RubriquePaieDeduction rpd2 = new RubriquePaieDeduction();
        rpd2.setDefinitionDeduction(nonSpecial);
        when(rubriquePaieDeductionRepository.findByRubriquePaieId(50L)).thenReturn(List.of(rpd1, rpd2));
        when(trancheBaremeDeductionRepository.findByDefinitionDeductionIdOrderByBorneInfAsc(eq(700L))).thenReturn(List.of());

        Map<String, Object> result = service.generate(5L, 50L, null, null, "tester");

        assertEquals(1, result.get("createdRows"));
        ArgumentCaptor<PayrollBoniDeduction> deductionCaptor = ArgumentCaptor.forClass(PayrollBoniDeduction.class);
        verify(payrollBoniDeductionRepository, times(1)).save(deductionCaptor.capture());
        PayrollBoniDeduction savedDeduction = deductionCaptor.getValue();
        assertEquals("DED-S", savedDeduction.getCodeDeduction());
        assertEquals(new BigDecimal("11.67"), savedDeduction.getMontant());

        ArgumentCaptor<PayrollEmployeBoni> boniCaptor = ArgumentCaptor.forClass(PayrollEmployeBoni.class);
        verify(payrollEmployeBoniRepository, times(2)).save(boniCaptor.capture());
        PayrollEmployeBoni finalSaved = boniCaptor.getAllValues().get(1);
        assertEquals(new BigDecimal("1400.00"), finalSaved.getMontantReference());
        assertEquals(new BigDecimal("116.67"), finalSaved.getMontantBoniBrut());
        assertEquals(new BigDecimal("11.67"), finalSaved.getMontantDeductions());
        assertEquals(new BigDecimal("105.00"), finalSaved.getMontantBoniNet());
    }

    @Test
    void generateDeletesCalculatedBonisBeforeRegenerationForSameCriteria() {
        RubriquePaie rubrique = new RubriquePaie();
        rubrique.setId(51L);
        when(rubriquePaieRepository.findById(51L)).thenReturn(Optional.of(rubrique));
        when(typeRevenuRepository.findActifsByRubriquePaieAndEntreprisePreferEntreprise(51L, null))
                .thenReturn(List.of());

        PayrollEmployeBoni existingCalcule = new PayrollEmployeBoni();
        existingCalcule.setId(900L);
        existingCalcule.setStatut(PayrollEmployeBoni.StatutBoni.CALCULE);
        PayrollEmployeBoni existingValide = new PayrollEmployeBoni();
        existingValide.setId(901L);
        existingValide.setStatut(PayrollEmployeBoni.StatutBoni.VALIDE);
        when(payrollEmployeBoniRepository.findByFilters(9L, 51L, null, null, null))
                .thenReturn(List.of(existingCalcule, existingValide));

        Employe employe = new Employe();
        employe.setId(12L);
        RegimePaie regime = new RegimePaie();
        regime.setId(3L);
        PayrollPeriodeBoni periode = new PayrollPeriodeBoni();
        periode.setId(9L);
        periode.setDateFin(LocalDate.of(2026, 12, 31));
        PayrollEmployeAgregat agregat = new PayrollEmployeAgregat();
        agregat.setEmploye(employe);
        agregat.setRegimePaie(regime);
        agregat.setPeriodeBoni(periode);
        agregat.setMontantSalaireBase(new BigDecimal("1200"));
        agregat.setMontantSupplementaire(BigDecimal.ZERO);
        agregat.setMontantAutreRevenu(BigDecimal.ZERO);
        agregat.setMontantSanctions(BigDecimal.ZERO);
        when(payrollEmployeAgregatRepository.findForBoniGeneration(9L, null, null)).thenReturn(List.of(agregat));

        EmploiEmploye emploi = new EmploiEmploye();
        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setBaseCalculBoni(12);
        emploi.setTypeEmploye(typeEmploye);
        when(emploiEmployeRepository.findActiveForDate(12L, LocalDate.of(2026, 12, 31))).thenReturn(List.of(emploi));
        when(payrollEmployeBoniRepository.findByRegimePaieIdAndPeriodeBoniIdAndEmployeIdAndRubriquePaieId(3L, 9L, 12L, 51L))
                .thenReturn(Optional.empty());
        when(payrollEmployeBoniRepository.save(any(PayrollEmployeBoni.class))).thenAnswer(invocation -> {
            PayrollEmployeBoni entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(950L);
            }
            return entity;
        });
        when(rubriquePaieDeductionRepository.findByRubriquePaieId(51L)).thenReturn(List.of());

        service.generate(9L, 51L, null, null, "tester");

        verify(payrollBoniDeductionRepository, times(1)).deleteByPayrollBoniIds(List.of(900L));
        verify(payrollEmployeBoniRepository, times(1)).deleteAllByIdInBatch(List.of(900L));
        verify(payrollBoniDeductionRepository, never()).deleteByPayrollBoniIds(List.of(901L));
    }

    @Test
    void generateComputesDeductionsUsingTranchesWhenDefined() {
        RubriquePaie rubrique = new RubriquePaie();
        rubrique.setId(60L);
        when(rubriquePaieRepository.findById(60L)).thenReturn(Optional.of(rubrique));
        when(typeRevenuRepository.findActifsByRubriquePaieAndEntreprisePreferEntreprise(60L, null))
                .thenReturn(List.of());

        Employe employe = new Employe();
        employe.setId(11L);
        employe.setCodeEmploye("E11");
        employe.setNom("Doe");
        employe.setPrenom("Jane");

        RegimePaie regime = new RegimePaie();
        regime.setId(2L);

        PayrollPeriodeBoni periode = new PayrollPeriodeBoni();
        periode.setId(6L);
        periode.setDateFin(LocalDate.of(2026, 12, 31));

        PayrollEmployeAgregat agregat = new PayrollEmployeAgregat();
        agregat.setEmploye(employe);
        agregat.setRegimePaie(regime);
        agregat.setPeriodeBoni(periode);
        agregat.setMontantSalaireBase(new BigDecimal("1200"));
        agregat.setMontantSupplementaire(new BigDecimal("0"));
        agregat.setMontantAutreRevenu(new BigDecimal("0"));
        agregat.setMontantSanctions(new BigDecimal("0"));
        when(payrollEmployeAgregatRepository.findForBoniGeneration(6L, null, null)).thenReturn(List.of(agregat));

        EmploiEmploye emploi = new EmploiEmploye();
        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setBaseCalculBoni(12);
        emploi.setTypeEmploye(typeEmploye);
        when(emploiEmployeRepository.findActiveForDate(11L, LocalDate.of(2026, 12, 31))).thenReturn(List.of(emploi));

        when(payrollEmployeBoniRepository.findByRegimePaieIdAndPeriodeBoniIdAndEmployeIdAndRubriquePaieId(2L, 6L, 11L, 60L))
                .thenReturn(Optional.empty());
        when(payrollEmployeBoniRepository.save(any(PayrollEmployeBoni.class))).thenAnswer(invocation -> {
            PayrollEmployeBoni entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(110L);
            }
            return entity;
        });

        DefinitionDeduction special = new DefinitionDeduction();
        special.setId(800L);
        special.setCodeDeduction("DED-T");
        special.setLibelle("Tranche");
        special.setSpecialise("Y");
        special.setTypeDeduction(DefinitionDeduction.TypeDeduction.POURCENTAGE);
        special.setValeur(BigDecimal.ZERO);
        special.setValeurCouvert(BigDecimal.ZERO);
        special.setPctHorsCalcul(BigDecimal.ZERO);
        special.setMinPrelevement(BigDecimal.ZERO);
        special.setMaxPrelevement(BigDecimal.ZERO);
        special.setArrondir(DefinitionDeduction.Arrondir.CENTIEME);

        RubriquePaieDeduction rpd = new RubriquePaieDeduction();
        rpd.setDefinitionDeduction(special);
        when(rubriquePaieDeductionRepository.findByRubriquePaieId(60L)).thenReturn(List.of(rpd));

        TrancheBaremeDeduction t1 = new TrancheBaremeDeduction();
        t1.setBorneInf(BigDecimal.ZERO);
        t1.setBorneSup(new BigDecimal("50"));
        t1.setTypeDeduction(DefinitionDeduction.TypeDeduction.POURCENTAGE);
        t1.setValeur(new BigDecimal("10"));

        TrancheBaremeDeduction t2 = new TrancheBaremeDeduction();
        t2.setBorneInf(new BigDecimal("50"));
        t2.setBorneSup(null);
        t2.setTypeDeduction(DefinitionDeduction.TypeDeduction.POURCENTAGE);
        t2.setValeur(new BigDecimal("20"));

        when(trancheBaremeDeductionRepository.findByDefinitionDeductionIdOrderByBorneInfAsc(eq(800L)))
                .thenReturn(List.of(t1, t2));

        Map<String, Object> result = service.generate(6L, 60L, null, null, "tester");

        assertEquals(1, result.get("createdRows"));
        ArgumentCaptor<PayrollBoniDeduction> deductionCaptor = ArgumentCaptor.forClass(PayrollBoniDeduction.class);
        verify(payrollBoniDeductionRepository, times(1)).save(deductionCaptor.capture());
        PayrollBoniDeduction savedDeduction = deductionCaptor.getValue();
        assertEquals(new BigDecimal("15.00"), savedDeduction.getMontant());

        ArgumentCaptor<PayrollEmployeBoni> boniCaptor = ArgumentCaptor.forClass(PayrollEmployeBoni.class);
        verify(payrollEmployeBoniRepository, times(2)).save(boniCaptor.capture());
        PayrollEmployeBoni finalSaved = boniCaptor.getAllValues().get(1);
        assertEquals(new BigDecimal("100.00"), finalSaved.getMontantBoniBrut());
        assertEquals(new BigDecimal("15.00"), finalSaved.getMontantDeductions());
        assertEquals(new BigDecimal("85.00"), finalSaved.getMontantBoniNet());
    }

    @Test
    void generateSupportsAmtSalRFormulaVariablesForBoni() {
        RubriquePaie rubrique = new RubriquePaie();
        rubrique.setId(62L);
        when(rubriquePaieRepository.findById(62L)).thenReturn(Optional.of(rubrique));

        Formule formule = new Formule();
        formule.setExpression("(${amt.sal.m} + ${amt.sal.r}) / ${n.bon.base}");
        TypeRevenu typeRevenu = new TypeRevenu();
        typeRevenu.setFormule(formule);
        when(typeRevenuRepository.findActifsByRubriquePaieAndEntreprisePreferEntreprise(62L, null))
                .thenReturn(List.of(typeRevenu));

        Employe employe = new Employe();
        employe.setId(21L);

        RegimePaie regime = new RegimePaie();
        regime.setId(4L);
        regime.setNbPeriodePaie(12);
        regime.setPeriodePaieCourante(8);

        PayrollPeriodeBoni periode = new PayrollPeriodeBoni();
        periode.setId(10L);
        periode.setDateFin(LocalDate.of(2026, 12, 31));

        PayrollEmployeAgregat agregat = new PayrollEmployeAgregat();
        agregat.setEmploye(employe);
        agregat.setRegimePaie(regime);
        agregat.setPeriodeBoni(periode);
        agregat.setMontantSalaireBase(new BigDecimal("1200"));
        agregat.setMontantSupplementaire(BigDecimal.ZERO);
        agregat.setMontantAutreRevenu(BigDecimal.ZERO);
        agregat.setMontantSanctions(BigDecimal.ZERO);
        agregat.setNbPaie(6);
        when(payrollEmployeAgregatRepository.findForBoniGeneration(10L, null, null)).thenReturn(List.of(agregat));

        EmploiEmploye emploi = new EmploiEmploye();
        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setBaseCalculBoni(10);
        emploi.setTypeEmploye(typeEmploye);
        when(emploiEmployeRepository.findActiveForDate(21L, LocalDate.of(2026, 12, 31))).thenReturn(List.of(emploi));

        when(payrollEmployeBoniRepository.findByRegimePaieIdAndPeriodeBoniIdAndEmployeIdAndRubriquePaieId(4L, 10L, 21L, 62L))
                .thenReturn(Optional.empty());
        when(payrollEmployeBoniRepository.save(any(PayrollEmployeBoni.class))).thenAnswer(invocation -> {
            PayrollEmployeBoni entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(210L);
            }
            return entity;
        });
        when(rubriquePaieDeductionRepository.findByRubriquePaieId(62L)).thenReturn(List.of());

        Map<String, Object> result = service.generate(10L, 62L, null, null, "tester");

        assertEquals(1, result.get("createdRows"));
        ArgumentCaptor<PayrollEmployeBoni> boniCaptor = ArgumentCaptor.forClass(PayrollEmployeBoni.class);
        verify(payrollEmployeBoniRepository, times(2)).save(boniCaptor.capture());
        PayrollEmployeBoni finalSaved = boniCaptor.getAllValues().get(1);
        assertEquals(new BigDecimal("1200.00"), finalSaved.getMontantReference());
        assertEquals(new BigDecimal("100.00"), finalSaved.getMontantBoniBrut());
        assertEquals(new BigDecimal("0.00"), finalSaved.getMontantDeductions());
        assertEquals(new BigDecimal("100.00"), finalSaved.getMontantBoniNet());
    }
}
