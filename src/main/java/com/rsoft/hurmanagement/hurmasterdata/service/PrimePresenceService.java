package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PrimePresenceService {

    private final PresenceEmployeRepository presenceEmployeRepository;
    private final AutreRevenuEmployeRepository autreRevenuEmployeRepository;
    private final TypeRevenuRepository typeRevenuRepository;
    private final EmployeSalaireRepository employeSalaireRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final JourCongeRepository jourCongeRepository;
    private final CongeEmployeRepository congeEmployeRepository;
    private final SupplementaireEmployeRepository supplementaireEmployeRepository;
    private final HoraireDtRepository horaireDtRepository;
    private final FormuleRepository formuleRepository;
    private final PayrollEmployeRepository payrollEmployeRepository;
    private final PayrollEmployeAgregatRepository payrollEmployeAgregatRepository;
    private final RegimePaieDeductionRepository regimePaieDeductionRepository;

    private final FormulaExpressionEvaluator formulaEvaluator = new FormulaExpressionEvaluator();
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Transactional
    public Map<String, Object> generatePrimePresence(LocalDate dateDebut,
                                                     LocalDate dateFin,
                                                     Long entrepriseId,
                                                     String username) {
        return generatePrimePresence(dateDebut, dateFin, entrepriseId, username, true);
    }

    @Transactional
    public Map<String, Object> generatePrimePresence(LocalDate dateDebut,
                                                     LocalDate dateFin,
                                                     Long entrepriseId,
                                                     String username,
                                                     boolean inclurePaiesManquantes) {
        return generatePrimePresence(dateDebut, dateFin, entrepriseId, null, null, null, username, inclurePaiesManquantes);
    }

    @Transactional
    public Map<String, Object> generatePrimePresence(LocalDate dateDebut,
                                                     LocalDate dateFin,
                                                     Long entrepriseId,
                                                     Long regimePaieId,
                                                     Long rubriquePaieId,
                                                     Long periodeBoniId,
                                                     String username,
                                                     boolean inclurePaiesManquantes) {
        return generatePrimePresenceInternal(
                dateDebut,
                dateFin,
                entrepriseId,
                regimePaieId,
                rubriquePaieId,
                periodeBoniId,
                null,
                username,
                inclurePaiesManquantes
        );
    }

    @Transactional
    public Map<String, Object> generatePrimePresenceForEmploye(LocalDate dateDebut,
                                                               LocalDate dateFin,
                                                               Long entrepriseId,
                                                               Long regimePaieId,
                                                               Long rubriquePaieId,
                                                               Long periodeBoniId,
                                                               Long employeId,
                                                               String username,
                                                               boolean inclurePaiesManquantes) {
        return generatePrimePresenceInternal(
                dateDebut,
                dateFin,
                entrepriseId,
                regimePaieId,
                rubriquePaieId,
                periodeBoniId,
                employeId,
                username,
                inclurePaiesManquantes
        );
    }

    private Map<String, Object> generatePrimePresenceInternal(LocalDate dateDebut,
                                                              LocalDate dateFin,
                                                              Long entrepriseId,
                                                              Long regimePaieId,
                                                              Long rubriquePaieId,
                                                              Long periodeBoniId,
                                                              Long employeIdFilter,
                                                              String username,
                                                              boolean inclurePaiesManquantes) {
        TypeRevenu typeRevenu = resolveTypeRevenu(entrepriseId, rubriquePaieId);
        if (typeRevenu == null) {
            throw new RuntimeException("TypeRevenu PRIME_PRESENCE not found");
        }

        List<PresenceEmploye> presences = presenceEmployeRepository
                .findValidesForPrimePresence(dateDebut, dateFin, entrepriseId);


        Map<Long, List<PresenceEmploye>> presencesByEmploye = new LinkedHashMap<>();
        for (PresenceEmploye presence : presences) {
            if (presence.getEmploye() == null || presence.getEmploye().getId() == null) {
                continue;
            }
            presencesByEmploye
                    .computeIfAbsent(presence.getEmploye().getId(), id -> new ArrayList<>())
                    .add(presence);
        }

        int totalEmployes = presencesByEmploye.size();
        int eligibleEmployes = 0;
        int createdRows = 0;
        int skippedMissingConfig = 0;
        int skippedExisting = 0;

        for (Map.Entry<Long, List<PresenceEmploye>> entry : presencesByEmploye.entrySet()) {
            Long employeId = entry.getKey();
            if (employeIdFilter != null && !Objects.equals(employeId, employeIdFilter)) {
                continue;
            }
            List<PresenceEmploye> employePresences = entry.getValue();
            if (employePresences.isEmpty()) {
                continue;
            }

            EmployeSalaire salaire = employeSalaireRepository
                    .findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(employeId, "Y")
                    .orElse(null);
            if (salaire == null || salaire.getRegimePaie() == null || salaire.getRegimePaie().getDevise() == null) {
                skippedMissingConfig++;
                continue;
            }
            if (regimePaieId != null && !Objects.equals(salaire.getRegimePaie().getId(), regimePaieId)) {
                continue;
            }
            if (entrepriseId != null && (salaire.getEmploye() == null || salaire.getEmploye().getEntreprise() == null
                    || !Objects.equals(salaire.getEmploye().getEntreprise().getId(), entrepriseId))) {
                continue;
            }

            EmploiEmploye emploi = resolveEmploiEmploye(employeId, dateFin);
            TypeEmploye typeEmploye = emploi != null ? emploi.getTypeEmploye() : employePresences.get(0).getTypeEmploye();
            if (typeEmploye == null || typeEmploye.getAjouterBonusApresNbMinutePresence() == null) {
                skippedMissingConfig++;
                continue;
            }
            int totalMinutes = sumPresenceMinutes(employePresences);

            eligibleEmployes++;

            if (autreRevenuEmployeRepository.existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(
                    employeId, typeRevenu.getId(), dateDebut, dateFin)) {
                skippedExisting++;
                continue;
            }
            Map<String, Object> variables = buildVariables(
                    salaire,
                    emploi,
                    typeEmploye,
                    employePresences,
                    dateDebut,
                    dateFin,
                    totalMinutes,
                    inclurePaiesManquantes,
                    periodeBoniId,
                    typeRevenu.getRubriquePaie()
            );
            BigDecimal montant = computeMontant(typeRevenu, variables, dateFin, resolvePrimePresenceFormule(dateFin));
            montant = applySpecialDeductionsIfNeeded(montant, salaire.getRegimePaie(), typeRevenu.getRubriquePaie());
            if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            AutreRevenuEmploye revenu = new AutreRevenuEmploye();
            revenu.setEntreprise(salaire.getEmploye().getEntreprise());
            revenu.setEmploye(salaire.getEmploye());
            revenu.setTypeRevenu(typeRevenu);
            revenu.setDateRevenu(dateFin);
            revenu.setDateEffet(dateFin);
            revenu.setDevise(salaire.getRegimePaie().getDevise());
            revenu.setMontant(montant.setScale(2, RoundingMode.HALF_UP));
            revenu.setCommentaire("AUTO PRIME_PRESENCE");
            revenu.setModeInclusion(AutreRevenuEmploye.ModeInclusion.PROCHAINE_PAIE);
            revenu.setRegimePaie(salaire.getRegimePaie());
            revenu.setDateInclusion(dateFin);
            revenu.setReference("PRIME_PRESENCE");
            revenu.setStatut(AutreRevenuEmploye.StatutAutreRevenu.BROUILLON);
            revenu.setPayrollNo(0);
            revenu.setCreatedBy(username);
            revenu.setCreatedOn(OffsetDateTime.now());
            revenu.setRowscn(1);
            autreRevenuEmployeRepository.save(revenu);
            createdRows++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalEmployes", totalEmployes);
        result.put("eligibleEmployes", eligibleEmployes);
        result.put("createdRows", createdRows);
        result.put("skippedExisting", skippedExisting);
        result.put("skippedMissingConfig", skippedMissingConfig);
        result.put("message", "Prime presence generated for " + createdRows + " employes");
        return result;
    }

    private TypeRevenu resolveTypeRevenu(Long entrepriseId, Long rubriquePaieId) {
        if (rubriquePaieId != null) {
            List<TypeRevenu> candidats = typeRevenuRepository
                    .findActifsByRubriquePaieAndEntreprisePreferEntreprise(rubriquePaieId, entrepriseId);
            if (!candidats.isEmpty()) {
                if (entrepriseId != null) {
                    for (TypeRevenu candidat : candidats) {
                        if (candidat.getEntreprise() != null && Objects.equals(candidat.getEntreprise().getId(), entrepriseId)) {
                            return candidat;
                        }
                    }
                }
                return candidats.get(0);
            }
        }
        TypeRevenu type = typeRevenuRepository.findByCodeRevenuAndEntrepriseId("PRIME_PRESENCE", entrepriseId)
                .orElse(null);
        if (type == null && entrepriseId != null) {
            type = typeRevenuRepository.findByCodeRevenuAndEntrepriseId("PRIME_PRESENCE", null).orElse(null);
        }
        return type;
    }

    private EmploiEmploye resolveEmploiEmploye(Long employeId, LocalDate date) {
        List<EmploiEmploye> emplois = emploiEmployeRepository.findActiveForDate(employeId, date);
        if (emplois != null && !emplois.isEmpty()) {
            return emplois.get(0);
        }
        emplois = emploiEmployeRepository.findByEmployeIdAndStatutEmploiNot(employeId, EmploiEmploye.StatutEmploi.TERMINE);
        return emplois.isEmpty() ? null : emplois.get(0);
    }

    private int sumPresenceMinutes(List<PresenceEmploye> presences) {
        int total = 0;
        for (PresenceEmploye presence : presences) {
            int minutes = computePresenceMinutes(presence);
            total += minutes;
        }
        return total;
    }

    private int computePresenceMinutes(PresenceEmploye presence) {
        if (presence == null || presence.getHeureArrivee() == null || presence.getHeureDepart() == null) {
            return 0;
        }
        try {
            LocalDate startDate = presence.getDateJour();
            LocalDate endDate = presence.getDateDepart() != null ? presence.getDateDepart() : startDate;
            if (startDate == null || endDate == null) {
                return 0;
            }

            LocalTime arrivee = LocalTime.parse(presence.getHeureArrivee());
            LocalTime depart = LocalTime.parse(presence.getHeureDepart());
            java.time.LocalDateTime start = java.time.LocalDateTime.of(startDate, arrivee);
            java.time.LocalDateTime end = java.time.LocalDateTime.of(endDate, depart);
            if (end.isBefore(start)) {
                end = end.plusDays(1);
            }
            long minutes = java.time.Duration.between(start, end).toMinutes();
            return minutes < 0 ? 0 : (int) minutes;
        } catch (Exception e) {
            return 0;
        }
    }

    private Map<String, Object> buildVariables(EmployeSalaire salaire,
                                               EmploiEmploye emploi,
                                               TypeEmploye typeEmploye,
                                               List<PresenceEmploye> presences,
                                               LocalDate debut,
                                               LocalDate fin,
                                               int totalMinutes,
                                               boolean inclurePaiesManquantes,
                                               Long periodeBoniId,
                                               RubriquePaie rubriquePaie) {
        Map<String, Object> vars = new HashMap<>();
        long nPaid = ChronoUnit.DAYS.between(debut, fin) + 1;
        long nPres = presences.stream().map(PresenceEmploye::getDateJour).distinct().count();
        long nPresDay = presences.stream()
                .filter(p -> p.getDateDepart() == null || p.getDateDepart().isEqual(p.getDateJour()))
                .map(PresenceEmploye::getDateJour)
                .distinct()
                .count();
        long nPresNight = presences.stream()
                .filter(p -> p.getDateDepart() != null && p.getDateDepart().isAfter(p.getDateJour()))
                .map(PresenceEmploye::getDateJour)
                .distinct()
                .count();
        long nOff = countOffDays(emploi, debut, fin);
        long nFerie = countFerieDays(debut, fin);
        BigDecimal hOt = sumSupplementaireHours(salaire.getEmploye().getId(), debut, fin);
        BigDecimal montantSupp = sumSupplementaireMontant(salaire.getEmploye().getId(), debut, fin);
        BigDecimal coefBonW = resolveBoniCoefficient(salaire);
        BigDecimal salaireBrut = salaire.getMontant() != null ? salaire.getMontant() : BigDecimal.ZERO;
        BigDecimal hPlanDay = computePlannedDailyHours(emploi, debut);
        BigDecimal salaireJournalier = computeDailySalaryAmount(salaire, salaire.getRegimePaie(), hPlanDay);
        Set<LocalDate> presenceDays = collectPresenceDays(presences);
        BigDecimal hFerieConge = computeFerieCongeHours(salaire.getEmploye().getId(), emploi, debut, fin, presenceDays);
        BigDecimal hWorkW = BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        int baseCalculBoni = resolveBaseCalculBoni(typeEmploye);
        BigDecimal montantSalaireAnnuel = computeMontantSalaireAnnuelCible(salaire, baseCalculBoni, fin, inclurePaiesManquantes, periodeBoniId, rubriquePaie);
        BigDecimal montantForfaitaireRestant = computeMontantForfaitaireRestant(salaire, montantSalaireAnnuel, fin, inclurePaiesManquantes, periodeBoniId, rubriquePaie);


        vars.put("n.paid.pp", nPaid);
        vars.put("n.pres.pp", nPres);
        vars.put("n.pres.day", nPresDay);
        vars.put("n.pres.night", nPresNight);
        vars.put("n.off.pp", nOff + nFerie);
        vars.put("h.ot.pp", hOt);
        vars.put("amt.supp.pp", montantSupp);
        vars.put("sal.brut", salaireBrut);
        vars.put("sal.base", salaireBrut);
        vars.put("amt.sal.d", salaireJournalier);
        vars.put("n.bon.base", baseCalculBoni);
        vars.put("amt.sal.y", montantSalaireAnnuel.setScale(2, RoundingMode.HALF_UP));
        vars.put("amt.sal.r", montantForfaitaireRestant.setScale(2, RoundingMode.HALF_UP));
        vars.put("h.plan.day", hPlanDay);
        vars.put("h.ferie.conge.pp", hFerieConge);
        vars.put("h.work.w", hWorkW);
        vars.put("coef.bon.w", coefBonW);
        vars.put("h.work.pp", BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
        BigDecimal bonusMinPresence = typeEmploye != null && typeEmploye.getAjouterBonusApresNbMinutePresence() != null
                ? BigDecimal.valueOf(typeEmploye.getAjouterBonusApresNbMinutePresence())
                : BigDecimal.ZERO;
        BigDecimal bonusPctJour = typeEmploye != null && typeEmploye.getPourcentageJourBonus() != null
                ? typeEmploye.getPourcentageJourBonus()
                : BigDecimal.ZERO;
        BigDecimal suppApres = typeEmploye != null && typeEmploye.getCalculerSupplementaireApres() != null
                ? BigDecimal.valueOf(typeEmploye.getCalculerSupplementaireApres())
                : BigDecimal.ZERO;
        // New compact aliases
        vars.put("t.bon.min", bonusMinPresence);
        vars.put("t.bon.pct", bonusPctJour);
        vars.put("t.sup.apr", suppApres);
        vars.put("amt.value", BigDecimal.ZERO);        
        return vars;
    }

    private int resolveBaseCalculBoni(TypeEmploye typeEmploye) {
        if (typeEmploye == null || typeEmploye.getBaseCalculBoni() == null || typeEmploye.getBaseCalculBoni() <= 0) {
            return 12;
        }
        return Math.min(typeEmploye.getBaseCalculBoni(), 12);
    }

    private BigDecimal computeMontantSalaireAnnuelCible(EmployeSalaire salaire,
                                                         int baseCalculBoni,
                                                         LocalDate dateRef,
                                                         boolean inclurePaiesManquantes,
                                                         Long periodeBoniId,
                                                         RubriquePaie rubriquePaie) {
        if (salaire == null || salaire.getEmploye() == null || salaire.getEmploye().getId() == null) {
            return BigDecimal.ZERO;
        }
        PayrollEmployeAgregat agregat = resolveAgregat(salaire, periodeBoniId);
        if (agregat != null) {
            return computeSalaireAnnuelFromAgregat(agregat, rubriquePaie);
        }
        BigDecimal cumulSalaireBaseAnnee = sumSalaireBaseAnnee(salaire.getEmploye().getId(), dateRef);
        int periodesMoyenne = resolveNombrePeriodesPourMoyenne(salaire, dateRef, inclurePaiesManquantes);
        BigDecimal moyenneMensuelle;
        if (cumulSalaireBaseAnnee.compareTo(BigDecimal.ZERO) > 0) {
            int periodesParMois = resolvePeriodesParMois(salaire);
            BigDecimal moyenneParPeriode = cumulSalaireBaseAnnee.divide(BigDecimal.valueOf(periodesMoyenne), 6, RoundingMode.HALF_UP);
            moyenneMensuelle = moyenneParPeriode.multiply(BigDecimal.valueOf(periodesParMois));
        } else {
            moyenneMensuelle = resolveSalaireMensuelFallback(salaire);
        }
        return moyenneMensuelle.multiply(BigDecimal.valueOf(baseCalculBoni));
    }

    private BigDecimal computeMontantForfaitaireRestant(EmployeSalaire salaire,
                                                        BigDecimal montantSalaireAnnuelCible,
                                                        LocalDate dateRef,
                                                        boolean inclurePaiesManquantes,
                                                        Long periodeBoniId,
                                                        RubriquePaie rubriquePaie) {
        if (salaire == null || salaire.getEmploye() == null || salaire.getEmploye().getId() == null) {
            return BigDecimal.ZERO;
        }
        if (!inclurePaiesManquantes) {
            return BigDecimal.ZERO;
        }
        PayrollEmployeAgregat agregat = resolveAgregat(salaire, periodeBoniId);
        if (agregat != null) {
            int nbPaie = Math.max(safeInt(agregat.getNbPaie()), 1);
            int nbPeriodeTotale = salaire.getRegimePaie() != null && salaire.getRegimePaie().getNbPeriodePaie() != null
                    ? Math.max(salaire.getRegimePaie().getNbPeriodePaie(), 1)
                    : 1;
            int noPeriodeCourante = Math.max(safeInt(agregat.getNoPeriode()), 1);
            int nbPeriodeRestante = Math.max(nbPeriodeTotale - noPeriodeCourante, 0);
            BigDecimal montantAnnuel = computeSalaireAnnuelFromAgregat(agregat, rubriquePaie);
            BigDecimal montantMoyenParPeriode = montantAnnuel.divide(BigDecimal.valueOf(nbPaie), 6, RoundingMode.HALF_UP);
            return montantMoyenParPeriode.multiply(BigDecimal.valueOf(nbPeriodeRestante));
        }
        BigDecimal cumulSalaireBaseAnnee = sumSalaireBaseAnnee(salaire.getEmploye().getId(), dateRef);
        BigDecimal restant = montantSalaireAnnuelCible.subtract(cumulSalaireBaseAnnee);
        return restant.compareTo(BigDecimal.ZERO) > 0 ? restant : BigDecimal.ZERO;
    }

    private int resolveNombrePeriodesPourMoyenne(EmployeSalaire salaire, LocalDate dateRef, boolean inclurePaiesManquantes) {
        if (salaire == null || salaire.getEmploye() == null || salaire.getEmploye().getId() == null || dateRef == null) {
            return 1;
        }
        if (inclurePaiesManquantes) {
            return resolvePeriodesAttenduesJusquaDate(salaire, dateRef);
        }
        Long nbPayrolls = countPayrollsAnnee(salaire.getEmploye().getId(), dateRef);
        if (nbPayrolls == null || nbPayrolls <= 0) {
            return 1;
        }
        return Math.max(nbPayrolls.intValue(), 1);
    }

    private int resolvePeriodesAttenduesJusquaDate(EmployeSalaire salaire, LocalDate dateRef) {
        RegimePaie regimePaie = salaire != null ? salaire.getRegimePaie() : null;
        RegimePaie.Periodicite periodicite = regimePaie != null ? regimePaie.getPeriodicite() : null;
        if (dateRef == null) {
            return 1;
        }
        int moisComplets = dateRef.getMonthValue();
        if (dateRef.getDayOfMonth() < dateRef.lengthOfMonth()) {
            moisComplets = moisComplets - 1;
        }
        moisComplets = Math.max(moisComplets, 1);
        return switch (periodicite != null ? periodicite : RegimePaie.Periodicite.MENSUEL) {
            case JOURNALIER -> Math.max(moisComplets * 30, 1);
            case HEBDO -> Math.max(moisComplets * 4, 1);
            case QUINZAINE, QUINZOMADAIRE -> Math.max(moisComplets * 2, 1);
            case MENSUEL -> moisComplets;
            case TRIMESTRIEL -> Math.max((int) Math.ceil(moisComplets / 3.0), 1);
            case SEMESTRIEL -> Math.max((int) Math.ceil(moisComplets / 6.0), 1);
            case ANNUEL -> 1;
        };
    }

    private int resolvePeriodesParMois(EmployeSalaire salaire) {
        RegimePaie regimePaie = salaire != null ? salaire.getRegimePaie() : null;
        RegimePaie.Periodicite periodicite = regimePaie != null ? regimePaie.getPeriodicite() : null;
        return switch (periodicite != null ? periodicite : RegimePaie.Periodicite.MENSUEL) {
            case JOURNALIER -> 30;
            case HEBDO -> 4;
            case QUINZAINE, QUINZOMADAIRE -> 2;
            case MENSUEL -> 1;
            case TRIMESTRIEL -> 1;
            case SEMESTRIEL -> 1;
            case ANNUEL -> 1;
        };
    }

    private BigDecimal sumSalaireBaseAnnee(Long employeId, LocalDate dateRef) {
        if (employeId == null || dateRef == null) {
            return BigDecimal.ZERO;
        }
        LocalDate debutAnnee = LocalDate.of(dateRef.getYear(), 1, 1);
        BigDecimal sum = payrollEmployeRepository.sumMontantSalaireBaseByEmployeAndDateFinBetween(
                employeId,
                List.of(Payroll.StatutPayroll.VALIDE, Payroll.StatutPayroll.FINALISE),
                debutAnnee,
                dateRef
        );
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private Long countPayrollsAnnee(Long employeId, LocalDate dateRef) {
        if (employeId == null || dateRef == null) {
            return 0L;
        }
        LocalDate debutAnnee = LocalDate.of(dateRef.getYear(), 1, 1);
        Long count = payrollEmployeRepository.countPayrollsByEmployeAndDateFinBetween(
                employeId,
                List.of(Payroll.StatutPayroll.VALIDE, Payroll.StatutPayroll.FINALISE),
                debutAnnee,
                dateRef
        );
        return count != null ? count : 0L;
    }

    private PayrollEmployeAgregat resolveAgregat(EmployeSalaire salaire, Long periodeBoniId) {
        if (salaire == null || salaire.getRegimePaie() == null || salaire.getEmploye() == null || periodeBoniId == null) {
            return null;
        }
        return payrollEmployeAgregatRepository.findByRegimePaieIdAndEmployeIdAndPeriodeBoniId(
                salaire.getRegimePaie().getId(),
                salaire.getEmploye().getId(),
                periodeBoniId
        ).orElse(null);
    }

    private BigDecimal computeSalaireAnnuelFromAgregat(PayrollEmployeAgregat agregat, RubriquePaie rubriquePaie) {
        if (agregat == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal base = safe(agregat.getMontantSalaireBase()).subtract(safe(agregat.getMontantSanctions()));
        // salaire base - sanctions is always included; remaining parts are configurable by payroll rubric.
        if (rubriquePaie != null && "Y".equalsIgnoreCase(rubriquePaie.getSoumisCotisations())) {
            base = base.add(safe(agregat.getMontantSupplementaire()));
        }
        if (rubriquePaie != null && "Y".equalsIgnoreCase(rubriquePaie.getImposable())) {
            base = base.add(safe(agregat.getMontantAutreRevenu()));
        }
        return base.max(BigDecimal.ZERO);
    }

    private BigDecimal applySpecialDeductionsIfNeeded(BigDecimal montant, RegimePaie regimePaie, RubriquePaie rubriquePaie) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (regimePaie == null || regimePaie.getId() == null || rubriquePaie == null || !"Y".equalsIgnoreCase(rubriquePaie.getImposable())) {
            return montant;
        }
        List<RegimePaieDeduction> deductions = regimePaieDeductionRepository.findByRegimePaieId(regimePaie.getId());
        if (deductions == null || deductions.isEmpty()) {
            return montant;
        }
        BigDecimal totalDeductions = BigDecimal.ZERO;
        for (RegimePaieDeduction rd : deductions) {
            if (rd == null || rd.getDeductionCode() == null || !"Y".equalsIgnoreCase(rd.getDeductionCode().getSpecialise())) {
                continue;
            }
            DefinitionDeduction definition = rd.getDeductionCode();
            BigDecimal valeur = safe(definition.getValeur());
            BigDecimal current;
            if (definition.getTypeDeduction() == DefinitionDeduction.TypeDeduction.POURCENTAGE) {
                current = montant.multiply(valeur).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            } else {
                current = valeur;
            }
            BigDecimal min = safe(definition.getMinPrelevement());
            BigDecimal max = safe(definition.getMaxPrelevement());
            if (min.compareTo(BigDecimal.ZERO) > 0 && current.compareTo(min) < 0) {
                current = min;
            }
            if (max.compareTo(BigDecimal.ZERO) > 0 && current.compareTo(max) > 0) {
                current = max;
            }
            totalDeductions = totalDeductions.add(current);
        }
        BigDecimal net = montant.subtract(totalDeductions);
        return net.compareTo(BigDecimal.ZERO) > 0 ? net : BigDecimal.ZERO;
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal resolveSalaireMensuelFallback(EmployeSalaire salaire) {
        if (salaire == null || salaire.getMontant() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal montant = salaire.getMontant();
        RegimePaie regimePaie = salaire.getRegimePaie();
        RegimePaie.Periodicite periodicite = regimePaie != null ? regimePaie.getPeriodicite() : null;
        int factor = switch (periodicite != null ? periodicite : RegimePaie.Periodicite.MENSUEL) {
            case JOURNALIER -> 30;
            case HEBDO -> 4;
            case QUINZAINE -> 2;
            case QUINZOMADAIRE -> 2;
            case MENSUEL -> 1;
            case TRIMESTRIEL -> 1;
            case SEMESTRIEL -> 1;
            case ANNUEL -> 1;
        };
        if (periodicite == RegimePaie.Periodicite.TRIMESTRIEL) {
            return montant.divide(BigDecimal.valueOf(3), 6, RoundingMode.HALF_UP);
        }
        if (periodicite == RegimePaie.Periodicite.SEMESTRIEL) {
            return montant.divide(BigDecimal.valueOf(6), 6, RoundingMode.HALF_UP);
        }
        if (periodicite == RegimePaie.Periodicite.ANNUEL) {
            return montant.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        }
        return montant.multiply(BigDecimal.valueOf(factor));
    }

    private long countOffDays(EmploiEmploye emploi, LocalDate debut, LocalDate fin) {
        if (emploi == null) {
            return 0;
        }
        Set<Integer> offDays = new HashSet<>();
        if (emploi.getJourOff1() != null) offDays.add(emploi.getJourOff1());
        if (emploi.getJourOff2() != null) offDays.add(emploi.getJourOff2());
        if (emploi.getJourOff3() != null) offDays.add(emploi.getJourOff3());
        long count = 0;
        for (LocalDate date = debut; !date.isAfter(fin); date = date.plusDays(1)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (offDays.contains(dow.getValue())) {
                count++;
            }
        }
        return count;
    }

    private long countFerieDays(LocalDate debut, LocalDate fin) {
        long count = 0;
        for (LocalDate date = debut; !date.isAfter(fin); date = date.plusDays(1)) {
            if (jourCongeRepository.existsByDateCongeAndActif(date, JourConge.Actif.Y)) {
                count++;
            }
        }
        return count;
    }

    private BigDecimal sumSupplementaireHours(Long employeId, LocalDate debut, LocalDate fin) {
        BigDecimal total = BigDecimal.ZERO;
        List<SupplementaireEmploye> supplementaires = supplementaireEmployeRepository.findValidesForPayroll(employeId, debut, fin);
        for (SupplementaireEmploye supplementaire : supplementaires) {
            if (supplementaire.getDetails() == null || supplementaire.getDetails().isBlank()) {
                continue;
            }
            try {
                Map<String, Object> details = objectMapper
                        .readValue(supplementaire.getDetails(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                Object nbHeures = details.get("nb_heures");
                if (nbHeures instanceof Number) {
                    total = total.add(BigDecimal.valueOf(((Number) nbHeures).doubleValue()));
                } else if (nbHeures instanceof String) {
                    try {
                        total = total.add(new BigDecimal((String) nbHeures));
                    } catch (NumberFormatException ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return total;
    }

    private BigDecimal sumSupplementaireMontant(Long employeId, LocalDate debut, LocalDate fin) {
        BigDecimal total = BigDecimal.ZERO;
        List<SupplementaireEmploye> supplementaires = supplementaireEmployeRepository.findValidesForPayroll(employeId, debut, fin);
        for (SupplementaireEmploye supplementaire : supplementaires) {
            if (supplementaire.getMontantCalcule() != null) {
                total = total.add(supplementaire.getMontantCalcule());
            }
        }
        return total;
    }

    private BigDecimal sumPresenceWorkHoursFromDetails(List<PresenceEmploye> presences) {
        BigDecimal total = BigDecimal.ZERO;
        for (PresenceEmploye presence : presences) {
            BigDecimal fromDetails = extractNbHeuresJourFromDetails(presence.getDetails());
            if (fromDetails != null) {
                total = total.add(fromDetails);
            } else {
                total = total.add(BigDecimal.valueOf(computePresenceMinutes(presence))
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal extractNbHeuresJourFromDetails(String rawDetails) {
        if (rawDetails == null || rawDetails.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> details = objectMapper
                    .readValue(rawDetails, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            Object value = details.get("nb_heures_jour");
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            if (value instanceof String && !((String) value).isBlank()) {
                return new BigDecimal((String) value);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isWeeklyRange(LocalDate debut, LocalDate fin) {
        long days = ChronoUnit.DAYS.between(debut, fin) + 1;
        return days == 7 || days == 8;
    }

    private BigDecimal computePlannedDailyHours(EmploiEmploye emploi, LocalDate dateReference) {
        if (emploi == null || emploi.getHoraire() == null) {
            return BigDecimal.ZERO;
        }
        Horaire horaire = emploi.getHoraire();
        String start = horaire.getHeureDebut();
        String end = isNotBlank(horaire.getHeureFermetureAutoJour())
                ? horaire.getHeureFermetureAutoJour()
                : horaire.getHeureFin();
        if (!isNotBlank(start) || !isNotBlank(end)) {
            int day = dateReference.getDayOfWeek().getValue();
            HoraireDt dt = horaireDtRepository.findByHoraireIdAndJour(horaire.getId(), day);
            if (dt != null && isNotBlank(dt.getHeureDebutJour()) && isNotBlank(dt.getHeureFinJour())) {
                start = dt.getHeureDebutJour();
                end = dt.getHeureFinJour();
            }
        }
        return computeHoursBetween(start, end);
    }

    private BigDecimal computeFerieCongeHours(Long employeId, EmploiEmploye emploi, LocalDate debut, LocalDate fin, Set<LocalDate> presenceDays) {
        BigDecimal plannedDaily = computePlannedDailyHours(emploi, debut);
        if (plannedDaily.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate date = debut; !date.isAfter(fin); date = date.plusDays(1)) {
            if (isOffDay(emploi, date)) {
                continue;
            }
            if (presenceDays != null && presenceDays.contains(date)) {
                continue;
            }
            boolean ferie = jourCongeRepository.existsByDateCongeAndActif(date, JourConge.Actif.Y);
            boolean conge = congeEmployeRepository.existsCongeForDate(employeId, date);
            if (ferie || conge) {
                total = total.add(plannedDaily);
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isOffDay(EmploiEmploye emploi, LocalDate date) {
        if (emploi == null || date == null) {
            return false;
        }
        int day = date.getDayOfWeek().getValue();
      
        return Objects.equals(emploi.getJourOff1(), day)
                || Objects.equals(emploi.getJourOff2(), day)
                || Objects.equals(emploi.getJourOff3(), day);
    }

    private Set<LocalDate> collectPresenceDays(List<PresenceEmploye> presences) {
        Set<LocalDate> days = new HashSet<>();
        if (presences == null) {
            return days;
        }
        for (PresenceEmploye presence : presences) {
            if (presence == null || presence.getDateJour() == null) {
                continue;
            }
            LocalDate start = presence.getDateJour();
            LocalDate end = presence.getDateDepart() != null ? presence.getDateDepart() : start;
            if (end.isBefore(start)) {
                end = start;
            }
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                days.add(date);
            }
        }
        return days;
    }

    private BigDecimal computeHoursBetween(String start, String end) {
        if (!isNotBlank(start) || !isNotBlank(end)) {
            return BigDecimal.ZERO;
        }
        try {
            LocalTime startTime = LocalTime.parse(start);
            LocalTime endTime = LocalTime.parse(end);
            long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
            if (minutes < 0) {
                minutes += 24 * 60;
            }
            if (minutes < 0) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private BigDecimal resolveBoniCoefficient(EmployeSalaire salaire) {
        if (salaire == null || salaire.getEmploi() == null || salaire.getEmploi().getTypeEmploye() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal coef = salaire.getEmploi().getTypeEmploye().getPourcentageJourBonus();
        return coef != null ? coef : BigDecimal.ZERO;
    }

    private BigDecimal computeDailySalaryAmount(EmployeSalaire salaire, RegimePaie regimePaie, BigDecimal hoursPerDay) {
        if (salaire == null || salaire.getMontant() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal montant = salaire.getMontant();
        RegimePaie.ModeRemuneration mode = regimePaie != null ? regimePaie.getModeRemuneration() : null;
        RegimePaie.Periodicite periodicite = regimePaie != null ? regimePaie.getPeriodicite() : null;
        BigDecimal hours = hoursPerDay != null && hoursPerDay.compareTo(BigDecimal.ZERO) > 0 ? hoursPerDay : BigDecimal.valueOf(8);

        if (mode == RegimePaie.ModeRemuneration.JOURNALIER
                || mode == RegimePaie.ModeRemuneration.PIECE
                || mode == RegimePaie.ModeRemuneration.PIECE_FIXE) {
            return montant;
        }
        if (mode == RegimePaie.ModeRemuneration.HORAIRE) {
            return montant.multiply(hours).setScale(6, RoundingMode.HALF_UP);
        }
        BigDecimal periodDays = periodDaysFromPeriodicite(periodicite);
        if (periodDays.compareTo(BigDecimal.ZERO) > 0) {
            return montant.divide(periodDays, 6, RoundingMode.HALF_UP);
        }
        return montant;
    }

    private BigDecimal periodDaysFromPeriodicite(RegimePaie.Periodicite periodicite) {
        if (periodicite == null) {
            return BigDecimal.ZERO;
        }
        return switch (periodicite) {
            case JOURNALIER -> BigDecimal.valueOf(1);
            case HEBDO -> BigDecimal.valueOf(7);
            case QUINZAINE -> BigDecimal.valueOf(14);
            case QUINZOMADAIRE -> BigDecimal.valueOf(15);
            case MENSUEL -> BigDecimal.valueOf(30);
            case TRIMESTRIEL -> BigDecimal.valueOf(90);
            case SEMESTRIEL -> BigDecimal.valueOf(180);
            case ANNUEL -> BigDecimal.valueOf(365);
        };
    }

    private BigDecimal computeMontant(TypeRevenu typeRevenu, Map<String, Object> variables, LocalDate dateRef, Formule primePresenceFormule) {
        if (typeRevenu == null) {
            return BigDecimal.ZERO;
        }
        Formule formuleToUse = primePresenceFormule;
        if (formuleToUse == null && typeRevenu.getFormule() != null && isFormuleEffective(typeRevenu.getFormule(), dateRef)) {
            formuleToUse = typeRevenu.getFormule();
        }
        if (formuleToUse == null || !isFormuleEffective(formuleToUse, dateRef)) {
            return BigDecimal.ZERO;
        }
        String expression = normalizeExpression(formuleToUse.getExpression());

        if (expression == null || expression.isBlank()) {
            return BigDecimal.ZERO;
        }
        BigDecimal eval = formulaEvaluator.evaluate(expression, variables, false);
        return eval != null ? eval : BigDecimal.ZERO;
    }

    private Formule resolvePrimePresenceFormule(LocalDate dateRef) {
        Formule formule = formuleRepository.findByCodeVariableIgnoreCase("prime_presence").orElse(null);
        if (formule != null && isFormuleEffective(formule, dateRef)) {
            return formule;
        }
        return null;
    }

    private boolean isFormuleEffective(Formule formule, LocalDate dateRef) {
        if (formule == null || !"Y".equalsIgnoreCase(formule.getActif())) {
            return false;
        }
        if (formule.getDateEffectif() != null && dateRef.isBefore(formule.getDateEffectif())) {
            return false;
        }
        if (formule.getDateFin() != null && !formule.getDateFin().isAfter(dateRef)) {
            return false;
        }
        return true;
    }

    private String normalizeExpression(String expression) {
        if (expression == null) {
            return null;
        }
        return expression.replaceAll("\\$\\{([^}]+)\\}", "$1");
    }
}
