package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.AutreRevenuEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Devise;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeSalaire;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.Formule;
import com.rsoft.hurmanagement.hurmasterdata.entity.Horaire;
import com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaie;
import com.rsoft.hurmanagement.hurmasterdata.entity.SupplementaireEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeRevenu;
import com.rsoft.hurmanagement.hurmasterdata.repository.AutreRevenuEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.CongeEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeSalaireRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.FormuleRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireDtRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.JourCongeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeAgregatRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PresenceEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RegimePaieDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.SupplementaireEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeRevenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrimePresenceServiceTest {

    @Mock
    private PresenceEmployeRepository presenceEmployeRepository;
    @Mock
    private AutreRevenuEmployeRepository autreRevenuEmployeRepository;
    @Mock
    private TypeRevenuRepository typeRevenuRepository;
    @Mock
    private EmployeSalaireRepository employeSalaireRepository;
    @Mock
    private EmploiEmployeRepository emploiEmployeRepository;
    @Mock
    private JourCongeRepository jourCongeRepository;
    @Mock
    private CongeEmployeRepository congeEmployeRepository;
    @Mock
    private SupplementaireEmployeRepository supplementaireEmployeRepository;
    @Mock
    private HoraireDtRepository horaireDtRepository;
    @Mock
    private FormuleRepository formuleRepository;
    @Mock
    private PayrollEmployeRepository payrollEmployeRepository;
    @Mock
    private PayrollEmployeAgregatRepository payrollEmployeAgregatRepository;
    @Mock
    private RegimePaieDeductionRepository regimePaieDeductionRepository;

    @InjectMocks
    private PrimePresenceService service;

    @BeforeEach
    void setUp() {
        when(payrollEmployeRepository.sumMontantSalaireBaseByEmployeAndDateFinBetween(
                anyLong(), anyList(), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(BigDecimal.ZERO);
        when(payrollEmployeRepository.countPayrollsByEmployeAndDateFinBetween(
                anyLong(), anyList(), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(0L);
    }

    @Test
    void generatePrimePresenceUsesPrimePresenceFormulaAndWeeklyHoursToken() {
        LocalDate debut = LocalDate.of(2026, 1, 1);
        LocalDate fin = LocalDate.of(2026, 1, 7);

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);

        Employe employe = new Employe();
        employe.setId(23L);
        employe.setEntreprise(entreprise);

        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setAjouterBonusApresNbMinutePresence(60);
        typeEmploye.setPourcentageJourBonus(new BigDecimal("5.50"));
        typeEmploye.setCalculerSupplementaireApres(40);

        Horaire horaire = new Horaire();
        horaire.setId(5L);
        horaire.setHeureDebut("08:00");
        horaire.setHeureFin("18:00");
        horaire.setHeureFermetureAutoJour("20:00");

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setTypeEmploye(typeEmploye);
        emploi.setHoraire(horaire);

        Devise devise = new Devise();
        devise.setId(2L);
        RegimePaie regimePaie = new RegimePaie();
        regimePaie.setDevise(devise);

        EmployeSalaire salaire = new EmployeSalaire();
        salaire.setEmploye(employe);
        salaire.setEmploi(emploi);
        salaire.setRegimePaie(regimePaie);
        salaire.setMontant(new BigDecimal("1000.00"));

        PresenceEmploye presence = new PresenceEmploye();
        presence.setEmploye(employe);
        presence.setEntreprise(entreprise);
        presence.setDateJour(LocalDate.of(2026, 1, 3));
        presence.setDateDepart(LocalDate.of(2026, 1, 3));
        presence.setHeureArrivee("08:00");
        presence.setHeureDepart("20:00");
        presence.setTypeEmploye(typeEmploye);
        presence.setDetails("{\"nb_heures_jour\":12.10}");

        SupplementaireEmploye supp = new SupplementaireEmploye();
        supp.setMontantCalcule(new BigDecimal("25.00"));
        supp.setDetails("{\"nb_heures\":3.00}");

        Formule linkedToTypeRevenu = new Formule();
        linkedToTypeRevenu.setActif("Y");
        linkedToTypeRevenu.setDateEffectif(LocalDate.of(2025, 1, 1));
        linkedToTypeRevenu.setExpression("1");

        Formule primePresenceFormule = new Formule();
        primePresenceFormule.setActif("Y");
        primePresenceFormule.setDateEffectif(LocalDate.of(2025, 1, 1));
        primePresenceFormule.setExpression(
                "h.work.w + h.ferie.conge.pp + sal.brut + amt.supp.pp + h.plan.day + t.bon.min + t.bon.pct + t.sup.apr"
        );

        TypeRevenu typeRevenu = new TypeRevenu();
        typeRevenu.setId(10L);
        typeRevenu.setCodeRevenu("PRIME_PRESENCE");
        typeRevenu.setFormule(linkedToTypeRevenu);

        when(typeRevenuRepository.findByCodeRevenuAndEntrepriseId("PRIME_PRESENCE", 1L)).thenReturn(Optional.of(typeRevenu));
        when(presenceEmployeRepository.findValidesForPrimePresence(debut, fin, 1L)).thenReturn(List.of(presence));
        when(employeSalaireRepository.findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(23L, "Y")).thenReturn(Optional.of(salaire));
        when(emploiEmployeRepository.findActiveForDate(23L, fin)).thenReturn(List.of(emploi));
        when(autreRevenuEmployeRepository.existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(23L, 10L, debut, fin)).thenReturn(false);
        when(supplementaireEmployeRepository.findValidesForPayroll(23L, debut, fin)).thenReturn(List.of(supp));
        when(formuleRepository.findByCodeVariableIgnoreCase("prime_presence")).thenReturn(Optional.of(primePresenceFormule));
        when(jourCongeRepository.existsByDateCongeAndActif(any(LocalDate.class), any())).thenReturn(false);
        when(congeEmployeRepository.existsActiveCongeForDate(eq(23L), any(LocalDate.class))).thenReturn(false);
        when(jourCongeRepository.existsByDateCongeAndActif(LocalDate.of(2026, 1, 2), com.rsoft.hurmanagement.hurmasterdata.entity.JourConge.Actif.Y)).thenReturn(true);
        when(congeEmployeRepository.existsActiveCongeForDate(23L, LocalDate.of(2026, 1, 4))).thenReturn(true);
        when(autreRevenuEmployeRepository.save(any(AutreRevenuEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.generatePrimePresence(debut, fin, 1L, "tester");

        assertEquals(1, result.get("createdRows"));
        ArgumentCaptor<AutreRevenuEmploye> captor = ArgumentCaptor.forClass(AutreRevenuEmploye.class);
        verify(autreRevenuEmployeRepository).save(captor.capture());
        AutreRevenuEmploye saved = captor.getValue();
        assertEquals(new BigDecimal("1178.50"), saved.getMontant());
        assertEquals(AutreRevenuEmploye.StatutAutreRevenu.BROUILLON, saved.getStatut());
    }

    @Test
    void generatePrimePresenceSetsWorkHoursTokenFromTotalMinutesForAnyRange() {
        LocalDate debut = LocalDate.of(2026, 1, 1);
        LocalDate fin = LocalDate.of(2026, 1, 3);

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);

        Employe employe = new Employe();
        employe.setId(23L);
        employe.setEntreprise(entreprise);

        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setAjouterBonusApresNbMinutePresence(1);

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setTypeEmploye(typeEmploye);

        Devise devise = new Devise();
        devise.setId(2L);
        RegimePaie regimePaie = new RegimePaie();
        regimePaie.setDevise(devise);

        EmployeSalaire salaire = new EmployeSalaire();
        salaire.setEmploye(employe);
        salaire.setEmploi(emploi);
        salaire.setRegimePaie(regimePaie);
        salaire.setMontant(new BigDecimal("100.00"));

        PresenceEmploye presence = new PresenceEmploye();
        presence.setEmploye(employe);
        presence.setEntreprise(entreprise);
        presence.setDateJour(LocalDate.of(2026, 1, 2));
        presence.setDateDepart(LocalDate.of(2026, 1, 2));
        presence.setHeureArrivee("08:00");
        presence.setHeureDepart("10:00");
        presence.setTypeEmploye(typeEmploye);
        presence.setDetails("{\"nb_heures_jour\":6.00}");

        Formule primePresenceFormule = new Formule();
        primePresenceFormule.setActif("Y");
        primePresenceFormule.setDateEffectif(LocalDate.of(2025, 1, 1));
        primePresenceFormule.setExpression("h.work.w");

        TypeRevenu typeRevenu = new TypeRevenu();
        typeRevenu.setId(10L);
        typeRevenu.setCodeRevenu("PRIME_PRESENCE");

        when(typeRevenuRepository.findByCodeRevenuAndEntrepriseId("PRIME_PRESENCE", 1L)).thenReturn(Optional.of(typeRevenu));
        when(presenceEmployeRepository.findValidesForPrimePresence(debut, fin, 1L)).thenReturn(List.of(presence));
        when(employeSalaireRepository.findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(23L, "Y")).thenReturn(Optional.of(salaire));
        when(emploiEmployeRepository.findActiveForDate(23L, fin)).thenReturn(List.of(emploi));
        when(autreRevenuEmployeRepository.existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(23L, 10L, debut, fin)).thenReturn(false);
        when(formuleRepository.findByCodeVariableIgnoreCase("prime_presence")).thenReturn(Optional.of(primePresenceFormule));
        when(supplementaireEmployeRepository.findValidesForPayroll(23L, debut, fin)).thenReturn(List.of());
        when(autreRevenuEmployeRepository.save(any(AutreRevenuEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.generatePrimePresence(debut, fin, 1L, "tester");

        assertEquals(1, result.get("createdRows"));
        ArgumentCaptor<AutreRevenuEmploye> captor = ArgumentCaptor.forClass(AutreRevenuEmploye.class);
        verify(autreRevenuEmployeRepository).save(captor.capture());
        AutreRevenuEmploye saved = captor.getValue();
        assertEquals(new BigDecimal("2.00"), saved.getMontant());
        assertEquals(AutreRevenuEmploye.StatutAutreRevenu.BROUILLON, saved.getStatut());
    }

    @Test
    void generatePrimePresenceComputesHoursUsingDatesNotOnlyTimes() {
        LocalDate debut = LocalDate.of(2026, 3, 1);
        LocalDate fin = LocalDate.of(2026, 3, 2);

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);

        Employe employe = new Employe();
        employe.setId(45L);
        employe.setEntreprise(entreprise);

        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setAjouterBonusApresNbMinutePresence(60);

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setTypeEmploye(typeEmploye);

        Devise devise = new Devise();
        devise.setId(3L);
        RegimePaie regimePaie = new RegimePaie();
        regimePaie.setDevise(devise);

        EmployeSalaire salaire = new EmployeSalaire();
        salaire.setEmploye(employe);
        salaire.setEmploi(emploi);
        salaire.setRegimePaie(regimePaie);
        salaire.setMontant(new BigDecimal("500.00"));

        PresenceEmploye presence = new PresenceEmploye();
        presence.setEmploye(employe);
        presence.setEntreprise(entreprise);
        presence.setDateJour(LocalDate.of(2026, 3, 1));
        presence.setDateDepart(LocalDate.of(2026, 3, 2));
        presence.setHeureArrivee("06:00");
        presence.setHeureDepart("06:00");
        presence.setTypeEmploye(typeEmploye);
        presence.setDetails("{}");

        Formule primePresenceFormule = new Formule();
        primePresenceFormule.setActif("Y");
        primePresenceFormule.setDateEffectif(LocalDate.of(2025, 1, 1));
        primePresenceFormule.setExpression("h.work.pp");

        TypeRevenu typeRevenu = new TypeRevenu();
        typeRevenu.setId(20L);
        typeRevenu.setCodeRevenu("PRIME_PRESENCE");

        when(typeRevenuRepository.findByCodeRevenuAndEntrepriseId("PRIME_PRESENCE", 1L)).thenReturn(Optional.of(typeRevenu));
        when(presenceEmployeRepository.findValidesForPrimePresence(debut, fin, 1L)).thenReturn(List.of(presence));
        when(employeSalaireRepository.findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(45L, "Y")).thenReturn(Optional.of(salaire));
        when(emploiEmployeRepository.findActiveForDate(45L, fin)).thenReturn(List.of(emploi));
        when(autreRevenuEmployeRepository.existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(45L, 20L, debut, fin)).thenReturn(false);
        when(formuleRepository.findByCodeVariableIgnoreCase("prime_presence")).thenReturn(Optional.of(primePresenceFormule));
        when(supplementaireEmployeRepository.findValidesForPayroll(45L, debut, fin)).thenReturn(List.of());
        when(autreRevenuEmployeRepository.save(any(AutreRevenuEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.generatePrimePresence(debut, fin, 1L, "tester");

        assertEquals(1, result.get("createdRows"));
        ArgumentCaptor<AutreRevenuEmploye> captor = ArgumentCaptor.forClass(AutreRevenuEmploye.class);
        verify(autreRevenuEmployeRepository).save(captor.capture());
        AutreRevenuEmploye saved = captor.getValue();
        assertEquals(new BigDecimal("24.00"), saved.getMontant());
        assertEquals(AutreRevenuEmploye.StatutAutreRevenu.BROUILLON, saved.getStatut());
    }

    @Test
    void generatePrimePresenceIgnoresFerieCongeHoursWhenDateIsOffDay() {
        LocalDate debut = LocalDate.of(2026, 3, 2); // Monday
        LocalDate fin = LocalDate.of(2026, 3, 2);

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);

        Employe employe = new Employe();
        employe.setId(60L);
        employe.setEntreprise(entreprise);

        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setAjouterBonusApresNbMinutePresence(1);

        Horaire horaire = new Horaire();
        horaire.setId(6L);
        horaire.setHeureDebut("08:00");
        horaire.setHeureFin("16:00");

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setTypeEmploye(typeEmploye);
        emploi.setHoraire(horaire);
        emploi.setJourOff1(1); // Monday is OFF

        Devise devise = new Devise();
        devise.setId(3L);
        RegimePaie regimePaie = new RegimePaie();
        regimePaie.setDevise(devise);

        EmployeSalaire salaire = new EmployeSalaire();
        salaire.setEmploye(employe);
        salaire.setEmploi(emploi);
        salaire.setRegimePaie(regimePaie);
        salaire.setMontant(new BigDecimal("500.00"));

        PresenceEmploye presence = new PresenceEmploye();
        presence.setEmploye(employe);
        presence.setEntreprise(entreprise);
        presence.setDateJour(LocalDate.of(2026, 3, 2));
        presence.setDateDepart(LocalDate.of(2026, 3, 2));
        presence.setHeureArrivee("08:00");
        presence.setHeureDepart("12:00");
        presence.setTypeEmploye(typeEmploye);
        presence.setDetails("{}");

        Formule primePresenceFormule = new Formule();
        primePresenceFormule.setActif("Y");
        primePresenceFormule.setDateEffectif(LocalDate.of(2025, 1, 1));
        primePresenceFormule.setExpression("h.ferie.conge.pp");

        TypeRevenu typeRevenu = new TypeRevenu();
        typeRevenu.setId(40L);
        typeRevenu.setCodeRevenu("PRIME_PRESENCE");

        when(typeRevenuRepository.findByCodeRevenuAndEntrepriseId("PRIME_PRESENCE", 1L)).thenReturn(Optional.of(typeRevenu));
        when(presenceEmployeRepository.findValidesForPrimePresence(debut, fin, 1L)).thenReturn(List.of(presence));
        when(employeSalaireRepository.findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(60L, "Y")).thenReturn(Optional.of(salaire));
        when(emploiEmployeRepository.findActiveForDate(60L, fin)).thenReturn(List.of(emploi));
        when(autreRevenuEmployeRepository.existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(60L, 40L, debut, fin)).thenReturn(false);
        when(formuleRepository.findByCodeVariableIgnoreCase("prime_presence")).thenReturn(Optional.of(primePresenceFormule));
        when(supplementaireEmployeRepository.findValidesForPayroll(60L, debut, fin)).thenReturn(List.of());

        Map<String, Object> result = service.generatePrimePresence(debut, fin, 1L, "tester");

        assertEquals(0, result.get("createdRows"));
    }

    @Test
    void generatePrimePresenceIgnoresFerieCongeHoursWhenEmployeHasPresence() {
        LocalDate debut = LocalDate.of(2026, 3, 3); // Tuesday
        LocalDate fin = LocalDate.of(2026, 3, 3);

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);

        Employe employe = new Employe();
        employe.setId(61L);
        employe.setEntreprise(entreprise);

        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setAjouterBonusApresNbMinutePresence(1);

        Horaire horaire = new Horaire();
        horaire.setId(7L);
        horaire.setHeureDebut("08:00");
        horaire.setHeureFin("16:00");

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setTypeEmploye(typeEmploye);
        emploi.setHoraire(horaire);

        Devise devise = new Devise();
        devise.setId(3L);
        RegimePaie regimePaie = new RegimePaie();
        regimePaie.setDevise(devise);

        EmployeSalaire salaire = new EmployeSalaire();
        salaire.setEmploye(employe);
        salaire.setEmploi(emploi);
        salaire.setRegimePaie(regimePaie);
        salaire.setMontant(new BigDecimal("500.00"));

        PresenceEmploye presence = new PresenceEmploye();
        presence.setEmploye(employe);
        presence.setEntreprise(entreprise);
        presence.setDateJour(LocalDate.of(2026, 3, 3));
        presence.setDateDepart(LocalDate.of(2026, 3, 3));
        presence.setHeureArrivee("08:00");
        presence.setHeureDepart("12:00");
        presence.setTypeEmploye(typeEmploye);
        presence.setDetails("{}");

        Formule primePresenceFormule = new Formule();
        primePresenceFormule.setActif("Y");
        primePresenceFormule.setDateEffectif(LocalDate.of(2025, 1, 1));
        primePresenceFormule.setExpression("h.ferie.conge.pp");

        TypeRevenu typeRevenu = new TypeRevenu();
        typeRevenu.setId(41L);
        typeRevenu.setCodeRevenu("PRIME_PRESENCE");

        when(typeRevenuRepository.findByCodeRevenuAndEntrepriseId("PRIME_PRESENCE", 1L)).thenReturn(Optional.of(typeRevenu));
        when(presenceEmployeRepository.findValidesForPrimePresence(debut, fin, 1L)).thenReturn(List.of(presence));
        when(employeSalaireRepository.findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(61L, "Y")).thenReturn(Optional.of(salaire));
        when(emploiEmployeRepository.findActiveForDate(61L, fin)).thenReturn(List.of(emploi));
        when(autreRevenuEmployeRepository.existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(61L, 41L, debut, fin)).thenReturn(false);
        when(formuleRepository.findByCodeVariableIgnoreCase("prime_presence")).thenReturn(Optional.of(primePresenceFormule));
        when(supplementaireEmployeRepository.findValidesForPayroll(61L, debut, fin)).thenReturn(List.of());

        Map<String, Object> result = service.generatePrimePresence(debut, fin, 1L, "tester");

        assertEquals(0, result.get("createdRows"));
    }

    @Test
    void generatePrimePresenceExposesDailySalaryFromModeAndPeriodicite() {
        LocalDate debut = LocalDate.of(2026, 3, 1);
        LocalDate fin = LocalDate.of(2026, 3, 1);

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);

        Employe employe = new Employe();
        employe.setId(50L);
        employe.setEntreprise(entreprise);

        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setAjouterBonusApresNbMinutePresence(1);

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setTypeEmploye(typeEmploye);

        Devise devise = new Devise();
        devise.setId(3L);
        RegimePaie regimePaie = new RegimePaie();
        regimePaie.setDevise(devise);
        regimePaie.setModeRemuneration(RegimePaie.ModeRemuneration.SALAIRE);
        regimePaie.setPeriodicite(RegimePaie.Periodicite.QUINZAINE);

        EmployeSalaire salaire = new EmployeSalaire();
        salaire.setEmploye(employe);
        salaire.setEmploi(emploi);
        salaire.setRegimePaie(regimePaie);
        salaire.setMontant(new BigDecimal("1400.00"));

        PresenceEmploye presence = new PresenceEmploye();
        presence.setEmploye(employe);
        presence.setEntreprise(entreprise);
        presence.setDateJour(LocalDate.of(2026, 3, 1));
        presence.setDateDepart(LocalDate.of(2026, 3, 1));
        presence.setHeureArrivee("08:00");
        presence.setHeureDepart("12:00");
        presence.setTypeEmploye(typeEmploye);
        presence.setDetails("{\"nb_heures_jour\":4.00}");

        Formule primePresenceFormule = new Formule();
        primePresenceFormule.setActif("Y");
        primePresenceFormule.setDateEffectif(LocalDate.of(2025, 1, 1));
        primePresenceFormule.setExpression("amt.sal.d");

        TypeRevenu typeRevenu = new TypeRevenu();
        typeRevenu.setId(30L);
        typeRevenu.setCodeRevenu("PRIME_PRESENCE");

        when(typeRevenuRepository.findByCodeRevenuAndEntrepriseId("PRIME_PRESENCE", 1L)).thenReturn(Optional.of(typeRevenu));
        when(presenceEmployeRepository.findValidesForPrimePresence(debut, fin, 1L)).thenReturn(List.of(presence));
        when(employeSalaireRepository.findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(50L, "Y")).thenReturn(Optional.of(salaire));
        when(emploiEmployeRepository.findActiveForDate(50L, fin)).thenReturn(List.of(emploi));
        when(autreRevenuEmployeRepository.existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(50L, 30L, debut, fin)).thenReturn(false);
        when(formuleRepository.findByCodeVariableIgnoreCase("prime_presence")).thenReturn(Optional.of(primePresenceFormule));
        when(supplementaireEmployeRepository.findValidesForPayroll(50L, debut, fin)).thenReturn(List.of());
        when(autreRevenuEmployeRepository.save(any(AutreRevenuEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.generatePrimePresence(debut, fin, 1L, "tester");

        assertEquals(1, result.get("createdRows"));
        ArgumentCaptor<AutreRevenuEmploye> captor = ArgumentCaptor.forClass(AutreRevenuEmploye.class);
        verify(autreRevenuEmployeRepository).save(captor.capture());
        AutreRevenuEmploye saved = captor.getValue();
        assertEquals(new BigDecimal("100.00"), saved.getMontant());
        assertEquals(AutreRevenuEmploye.StatutAutreRevenu.BROUILLON, saved.getStatut());
    }

    @Test
    void generatePrimePresenceExposesRemainingAnnualForfaitaryAmount() {
        LocalDate debut = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);

        Employe employe = new Employe();
        employe.setId(70L);
        employe.setEntreprise(entreprise);

        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setAjouterBonusApresNbMinutePresence(1);
        typeEmploye.setBaseCalculBoni(12);

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setTypeEmploye(typeEmploye);

        Devise devise = new Devise();
        devise.setId(3L);
        RegimePaie regimePaie = new RegimePaie();
        regimePaie.setDevise(devise);
        regimePaie.setModeRemuneration(RegimePaie.ModeRemuneration.SALAIRE);
        regimePaie.setPeriodicite(RegimePaie.Periodicite.MENSUEL);

        EmployeSalaire salaire = new EmployeSalaire();
        salaire.setEmploye(employe);
        salaire.setEmploi(emploi);
        salaire.setRegimePaie(regimePaie);
        salaire.setMontant(new BigDecimal("15000.00"));

        PresenceEmploye presence = new PresenceEmploye();
        presence.setEmploye(employe);
        presence.setEntreprise(entreprise);
        presence.setDateJour(LocalDate.of(2026, 11, 15));
        presence.setDateDepart(LocalDate.of(2026, 11, 15));
        presence.setHeureArrivee("08:00");
        presence.setHeureDepart("12:00");
        presence.setTypeEmploye(typeEmploye);
        presence.setDetails("{}");

        Formule primePresenceFormule = new Formule();
        primePresenceFormule.setActif("Y");
        primePresenceFormule.setDateEffectif(LocalDate.of(2025, 1, 1));
        primePresenceFormule.setExpression("amt.sal.r");

        TypeRevenu typeRevenu = new TypeRevenu();
        typeRevenu.setId(71L);
        typeRevenu.setCodeRevenu("PRIME_PRESENCE");

        when(typeRevenuRepository.findByCodeRevenuAndEntrepriseId("PRIME_PRESENCE", 1L)).thenReturn(Optional.of(typeRevenu));
        when(presenceEmployeRepository.findValidesForPrimePresence(debut, fin, 1L)).thenReturn(List.of(presence));
        when(employeSalaireRepository.findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(70L, "Y")).thenReturn(Optional.of(salaire));
        when(emploiEmployeRepository.findActiveForDate(70L, fin)).thenReturn(List.of(emploi));
        when(autreRevenuEmployeRepository.existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(70L, 71L, debut, fin)).thenReturn(false);
        when(formuleRepository.findByCodeVariableIgnoreCase("prime_presence")).thenReturn(Optional.of(primePresenceFormule));
        when(supplementaireEmployeRepository.findValidesForPayroll(70L, debut, fin)).thenReturn(List.of());
        when(payrollEmployeRepository.sumMontantSalaireBaseByEmployeAndDateFinBetween(
                eq(70L), anyList(), eq(LocalDate.of(2026, 1, 1)), eq(fin)
        )).thenReturn(new BigDecimal("165000.00"));
        when(autreRevenuEmployeRepository.save(any(AutreRevenuEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.generatePrimePresence(debut, fin, 1L, "tester");

        assertEquals(1, result.get("createdRows"));
        ArgumentCaptor<AutreRevenuEmploye> captor = ArgumentCaptor.forClass(AutreRevenuEmploye.class);
        verify(autreRevenuEmployeRepository).save(captor.capture());
        AutreRevenuEmploye saved = captor.getValue();
        assertEquals(new BigDecimal("15000.00"), saved.getMontant());
    }

    @Test
    void generatePrimePresenceExposesAnnualSalaryTargetFromBoniBase() {
        LocalDate debut = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);

        Employe employe = new Employe();
        employe.setId(80L);
        employe.setEntreprise(entreprise);

        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setAjouterBonusApresNbMinutePresence(1);
        typeEmploye.setBaseCalculBoni(12);

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setTypeEmploye(typeEmploye);

        Devise devise = new Devise();
        devise.setId(3L);
        RegimePaie regimePaie = new RegimePaie();
        regimePaie.setDevise(devise);
        regimePaie.setModeRemuneration(RegimePaie.ModeRemuneration.SALAIRE);
        regimePaie.setPeriodicite(RegimePaie.Periodicite.QUINZAINE);

        EmployeSalaire salaire = new EmployeSalaire();
        salaire.setEmploye(employe);
        salaire.setEmploi(emploi);
        salaire.setRegimePaie(regimePaie);
        salaire.setMontant(new BigDecimal("1000.00"));

        PresenceEmploye presence = new PresenceEmploye();
        presence.setEmploye(employe);
        presence.setEntreprise(entreprise);
        presence.setDateJour(LocalDate.of(2026, 11, 20));
        presence.setDateDepart(LocalDate.of(2026, 11, 20));
        presence.setHeureArrivee("08:00");
        presence.setHeureDepart("12:00");
        presence.setTypeEmploye(typeEmploye);
        presence.setDetails("{}");

        Formule primePresenceFormule = new Formule();
        primePresenceFormule.setActif("Y");
        primePresenceFormule.setDateEffectif(LocalDate.of(2025, 1, 1));
        primePresenceFormule.setExpression("amt.sal.y");

        TypeRevenu typeRevenu = new TypeRevenu();
        typeRevenu.setId(81L);
        typeRevenu.setCodeRevenu("PRIME_PRESENCE");

        when(typeRevenuRepository.findByCodeRevenuAndEntrepriseId("PRIME_PRESENCE", 1L)).thenReturn(Optional.of(typeRevenu));
        when(presenceEmployeRepository.findValidesForPrimePresence(debut, fin, 1L)).thenReturn(List.of(presence));
        when(employeSalaireRepository.findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(80L, "Y")).thenReturn(Optional.of(salaire));
        when(emploiEmployeRepository.findActiveForDate(80L, fin)).thenReturn(List.of(emploi));
        when(autreRevenuEmployeRepository.existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(80L, 81L, debut, fin)).thenReturn(false);
        when(formuleRepository.findByCodeVariableIgnoreCase("prime_presence")).thenReturn(Optional.of(primePresenceFormule));
        when(supplementaireEmployeRepository.findValidesForPayroll(80L, debut, fin)).thenReturn(List.of());
        when(payrollEmployeRepository.sumMontantSalaireBaseByEmployeAndDateFinBetween(
                eq(80L), anyList(), eq(LocalDate.of(2026, 1, 1)), eq(fin)
        )).thenReturn(new BigDecimal("22000.00"));
        when(autreRevenuEmployeRepository.save(any(AutreRevenuEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.generatePrimePresence(debut, fin, 1L, "tester");

        assertEquals(1, result.get("createdRows"));
        ArgumentCaptor<AutreRevenuEmploye> captor = ArgumentCaptor.forClass(AutreRevenuEmploye.class);
        verify(autreRevenuEmployeRepository).save(captor.capture());
        AutreRevenuEmploye saved = captor.getValue();
        assertEquals(new BigDecimal("24000.00"), saved.getMontant());
    }

    @Test
    void generatePrimePresenceCanIgnoreMissingPayrollsWhenRequested() {
        LocalDate debut = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);

        Employe employe = new Employe();
        employe.setId(90L);
        employe.setEntreprise(entreprise);

        TypeEmploye typeEmploye = new TypeEmploye();
        typeEmploye.setAjouterBonusApresNbMinutePresence(1);
        typeEmploye.setBaseCalculBoni(12);

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setEmploye(employe);
        emploi.setTypeEmploye(typeEmploye);

        Devise devise = new Devise();
        devise.setId(3L);
        RegimePaie regimePaie = new RegimePaie();
        regimePaie.setDevise(devise);
        regimePaie.setModeRemuneration(RegimePaie.ModeRemuneration.SALAIRE);
        regimePaie.setPeriodicite(RegimePaie.Periodicite.MENSUEL);

        EmployeSalaire salaire = new EmployeSalaire();
        salaire.setEmploye(employe);
        salaire.setEmploi(emploi);
        salaire.setRegimePaie(regimePaie);
        salaire.setMontant(new BigDecimal("15000.00"));

        PresenceEmploye presence = new PresenceEmploye();
        presence.setEmploye(employe);
        presence.setEntreprise(entreprise);
        presence.setDateJour(LocalDate.of(2026, 11, 20));
        presence.setDateDepart(LocalDate.of(2026, 11, 20));
        presence.setHeureArrivee("08:00");
        presence.setHeureDepart("12:00");
        presence.setTypeEmploye(typeEmploye);
        presence.setDetails("{}");

        Formule primePresenceFormule = new Formule();
        primePresenceFormule.setActif("Y");
        primePresenceFormule.setDateEffectif(LocalDate.of(2025, 1, 1));
        primePresenceFormule.setExpression("amt.sal.r");

        TypeRevenu typeRevenu = new TypeRevenu();
        typeRevenu.setId(91L);
        typeRevenu.setCodeRevenu("PRIME_PRESENCE");

        when(typeRevenuRepository.findByCodeRevenuAndEntrepriseId("PRIME_PRESENCE", 1L)).thenReturn(Optional.of(typeRevenu));
        when(presenceEmployeRepository.findValidesForPrimePresence(debut, fin, 1L)).thenReturn(List.of(presence));
        when(employeSalaireRepository.findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(90L, "Y")).thenReturn(Optional.of(salaire));
        when(emploiEmployeRepository.findActiveForDate(90L, fin)).thenReturn(List.of(emploi));
        when(autreRevenuEmployeRepository.existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(90L, 91L, debut, fin)).thenReturn(false);
        when(formuleRepository.findByCodeVariableIgnoreCase("prime_presence")).thenReturn(Optional.of(primePresenceFormule));
        when(supplementaireEmployeRepository.findValidesForPayroll(90L, debut, fin)).thenReturn(List.of());
        when(payrollEmployeRepository.sumMontantSalaireBaseByEmployeAndDateFinBetween(
                eq(90L), anyList(), eq(LocalDate.of(2026, 1, 1)), eq(fin)
        )).thenReturn(new BigDecimal("150000.00"));
        when(payrollEmployeRepository.countPayrollsByEmployeAndDateFinBetween(
                eq(90L), anyList(), eq(LocalDate.of(2026, 1, 1)), eq(fin)
        )).thenReturn(10L);
        when(autreRevenuEmployeRepository.save(any(AutreRevenuEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = service.generatePrimePresence(debut, fin, 1L, "tester", false);

        assertEquals(0, result.get("createdRows"));
    }
}
