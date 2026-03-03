package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import com.rsoft.hurmanagement.hurmasterdata.util.PayrollTaxePeriod;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollEmployeRepository payrollEmployeRepository;
    private final PayrollGainRepository payrollGainRepository;
    private final PayrollDeductionRepository payrollDeductionRepository;
    private final PayrollSanctionRepository payrollSanctionRepository;
    private final PayrollRecouvrementRepository payrollRecouvrementRepository;
    private final PayrollEmployeStatsRepository payrollEmployeStatsRepository;
    private final RegimePaieRepository regimePaieRepository;
    private final EmployeSalaireRepository employeSalaireRepository;
    private final SupplementaireEmployeRepository supplementaireEmployeRepository;
    private final AutreRevenuEmployeRepository autreRevenuEmployeRepository;
    private final TypeRevenuRepository typeRevenuRepository;
    private final PresenceEmployeRepository presenceEmployeRepository;
    private final AbsenceEmployeRepository absenceEmployeRepository;
    private final JourCongeRepository jourCongeRepository;
    private final SanctionEmployeRepository sanctionEmployeRepository;
    private final CoordonneeBancaireEmployeRepository coordonneeBancaireEmployeRepository;
    private final DefinitionDeductionRepository definitionDeductionRepository;
    private final RubriquePaieRepository rubriquePaieRepository;
    private final RubriquePaieDeductionRepository rubriquePaieDeductionRepository;
    private final RegimePaieDeductionRepository regimePaieDeductionRepository;
    private final TrancheBaremeDeductionRepository trancheBaremeDeductionRepository;
    private final ExclusionDeductionRepository exclusionDeductionRepository;
    private final PlanAssuranceRepository planAssuranceRepository;
    private final AssuranceEmployeRepository assuranceEmployeRepository;
    private final PayrollTaxeCycleRepository payrollTaxeCycleRepository;
    private final PretEmployeRepository pretEmployeRepository;
    private final PretRemboursementRepository pretRemboursementRepository;
    private final ProductionPieceRepository productionPieceRepository;
    private final JdbcTemplate jdbcTemplate;

    private final FormulaExpressionEvaluator formulaEvaluator = new FormulaExpressionEvaluator();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public Page<PayrollDTO> findByFilters(Long regimePaieId,
                                          String statut,
                                          String dateFinFrom,
                                          String dateFinTo,
                                          Pageable pageable) {
        Payroll.StatutPayroll statutEnum = parseStatut(statut);
        LocalDate finFrom = dateFinFrom != null && !dateFinFrom.isBlank() ? LocalDate.parse(dateFinFrom) : null;
        LocalDate finTo = dateFinTo != null && !dateFinTo.isBlank() ? LocalDate.parse(dateFinTo) : null;
        return payrollRepository.findByFilters(regimePaieId, statutEnum, finFrom, finTo, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public PayrollDTO findById(Long id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));
        return toDTO(payroll);
    }

    @Transactional
    public PayrollDTO create(PayrollCreateDTO dto, String username) {
        RegimePaie regimePaie = regimePaieRepository.findById(dto.getRegimePaieId())
                .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + dto.getRegimePaieId()));

        Payroll payroll = new Payroll();
        payroll.setRegimePaie(regimePaie);
        payroll.setLibelle(dto.getLibelle());
        payroll.setDateDebut(resolveDateDebut(regimePaie));
        payroll.setDateFin(resolveDateFin(regimePaie, payroll.getDateDebut()));
        payroll.setStatut(Payroll.StatutPayroll.BROUILLON);
        payroll.setCreatedBy(username);
        payroll.setCreatedOn(OffsetDateTime.now());
        payroll.setRowscn(1);
        return toDTO(payrollRepository.save(payroll));
    }

    @Transactional
    public PayrollDTO update(Long id, PayrollUpdateDTO dto, String username) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));
        if (!payroll.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        if (payroll.getStatut() != Payroll.StatutPayroll.BROUILLON) {
            throw new RuntimeException("payroll.error.cannotEdit");
        }

        RegimePaie regimePaie = regimePaieRepository.findById(dto.getRegimePaieId())
                .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + dto.getRegimePaieId()));

        payroll.setRegimePaie(regimePaie);
        payroll.setLibelle(dto.getLibelle());
        payroll.setDateDebut(resolveDateDebut(regimePaie));
        payroll.setDateFin(resolveDateFin(regimePaie, payroll.getDateDebut()));
        payroll.setUpdatedBy(username);
        payroll.setUpdatedOn(OffsetDateTime.now());
        return toDTO(payrollRepository.save(payroll));
    }

    @Transactional
    public void delete(Long id, Integer rowscn, String username) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));
        if (!payroll.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        if (payroll.getStatut() != Payroll.StatutPayroll.BROUILLON
                && payroll.getStatut() != Payroll.StatutPayroll.CALCULE) {
            throw new RuntimeException("payroll.error.cannotDelete");
        }
        clearExternalReferences(payroll.getId(), username);
        payrollEmployeStatsRepository.deleteAll(payrollEmployeStatsRepository.findByPayrollId(payroll.getId()));
        payrollSanctionRepository.deleteAll(payrollSanctionRepository.findByPayrollId(payroll.getId()));
        payrollDeductionRepository.deleteAll(payrollDeductionRepository.findByPayrollId(payroll.getId()));
        payrollGainRepository.deleteAll(payrollGainRepository.findByPayrollId(payroll.getId()));
        payrollRecouvrementRepository.deleteAll(payrollRecouvrementRepository.findByPayrollId(payroll.getId()));
        payrollEmployeRepository.deleteAll(payrollEmployeRepository.findByPayrollId(payroll.getId()));
        payrollRepository.delete(payroll);
    }

    @Transactional
    public PayrollDTO calculate(Long id, String username) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));
        if (payroll.getStatut() != Payroll.StatutPayroll.BROUILLON
                && payroll.getStatut() != Payroll.StatutPayroll.CALCULE) {
            throw new RuntimeException("payroll.error.cannotCalculate");
        }

        Long regimePaieId = payroll.getRegimePaie().getId();
        RegimePaie regimePaie = regimePaieRepository.findById(regimePaieId)
                .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + regimePaieId));

        LocalDate recalculatedDateDebut = resolveDateDebut(regimePaie);
        LocalDate recalculatedDateFin = resolveDateFin(regimePaie, recalculatedDateDebut);
        if (!recalculatedDateDebut.equals(payroll.getDateDebut())
                || !recalculatedDateFin.equals(payroll.getDateFin())) {
            payroll.setDateDebut(recalculatedDateDebut);
            payroll.setDateFin(recalculatedDateFin);
            payroll.setUpdatedBy(username);
            payroll.setUpdatedOn(OffsetDateTime.now());
            payroll = payrollRepository.save(payroll);
        }

        clearExternalReferences(payroll.getId(), username);
        payrollEmployeStatsRepository.deleteAll(payrollEmployeStatsRepository.findByPayrollId(payroll.getId()));
        payrollSanctionRepository.deleteAll(payrollSanctionRepository.findByPayrollId(payroll.getId()));
        payrollDeductionRepository.deleteAll(payrollDeductionRepository.findByPayrollId(payroll.getId()));
        payrollGainRepository.deleteAll(payrollGainRepository.findByPayrollId(payroll.getId()));
        payrollRecouvrementRepository.deleteAll(payrollRecouvrementRepository.findByPayrollId(payroll.getId()));
        payrollEmployeRepository.deleteAll(payrollEmployeRepository.findByPayrollId(payroll.getId()));

        RubriquePaie rubriqueSalaireBase = getRubriquePaieByCode("SAL_BASE");
        RubriquePaie rubriqueSupplementaire = getRubriquePaieByCode("SUPPLEMENTAIRE");
        RubriquePaie rubriqueAutreRevenu = getRubriquePaieByCode("AUTRE_REVENU");

        List<EmployeSalaire> salaires = employeSalaireRepository
                .findActifsByRegimePaie(payroll.getRegimePaie().getId());
        boolean applyTaxesCycle = shouldApplyTaxes(regimePaie, payroll);
        boolean applySupplementsCycle = shouldApplySupplements(regimePaie, payroll);

        for (EmployeSalaire salaire : salaires) {
            PayrollEmploye payrollEmploye = buildPayrollEmploye(payroll, salaire, username);
            payrollEmploye = payrollEmployeRepository.save(payrollEmploye);

            BigDecimal salaireBase = computeSalaireBase(salaire, regimePaie, payroll, username);
            if (salaireBase.compareTo(BigDecimal.ZERO) > 0) {
                payrollEmploye.setMontantSalaireBase(salaireBase);
                payrollGainRepository.save(buildGain(payroll, payrollEmploye, rubriqueSalaireBase,
                        salaireBase, "SYSTEME", null, username));
            }

            BigDecimal supplementaire = computeSupplementaire(salaire, regimePaie, payroll, payrollEmploye, rubriqueSupplementaire, username, applySupplementsCycle);
            payrollEmploye.setMontantSupplementaire(supplementaire);

            BigDecimal autreRevenu = computeAutreRevenu(salaire, payroll, payrollEmploye, rubriqueAutreRevenu, username);
            payrollEmploye.setMontantAutreRevenu(autreRevenu);

            BigDecimal sanctions = computeSanctions(salaire.getEmploye(), payroll, payrollEmploye, username);
            payrollEmploye.setMontantSanctions(sanctions);

            BigDecimal recouvrements = computeRecouvrements(salaire.getEmploye(), regimePaie, payroll, payrollEmploye, username);
            payrollEmploye.setMontantRecouvrements(recouvrements);

            computeStats(salaire, regimePaie, payroll, payrollEmploye, username);

            BigDecimal brut = payrollEmploye.getMontantSalaireBase()
                    .add(supplementaire)
                    .add(autreRevenu);
            payrollEmploye.setMontantBrut(brut);

            BigDecimal deductions;
            if (applyTaxesCycle) {
                deductions = computeDeductionsForTaxCycle(salaire, payrollEmploye, payroll, regimePaie, username);
            } else {
                deductions = BigDecimal.ZERO;
            }
            payrollEmploye.setMontantDeductions(deductions);

            BigDecimal net = brut.subtract(deductions.add(recouvrements).add(sanctions));
            if (isYes(regimePaie.getBloquerNetNegatif()) && net.compareTo(BigDecimal.ZERO) < 0) {
                Employe employe = salaire.getEmploye();
                String code = employe != null && employe.getCodeEmploye() != null ? employe.getCodeEmploye().trim() : "";
                String nom = employe != null && employe.getNom() != null ? employe.getNom().trim() : "";
                String prenom = employe != null && employe.getPrenom() != null ? employe.getPrenom().trim() : "";
                String label = (code + " - " + nom + " " + prenom).trim();
                if (label.startsWith("-")) {
                    label = label.substring(1).trim();
                }
                throw new RuntimeException("Paie bloquée: net négatif pour employé " + (label.isBlank() ? "(inconnu)" : label));
            }
            payrollEmploye.setMontantNetAPayer(net);

            payrollEmploye.setUpdatedBy(username);
            payrollEmploye.setUpdatedOn(OffsetDateTime.now());
            payrollEmployeRepository.save(payrollEmploye);
        }

        payroll.setStatut(Payroll.StatutPayroll.CALCULE);
        payroll.setUpdatedBy(username);
        payroll.setUpdatedOn(OffsetDateTime.now());
        return toDTO(payrollRepository.save(payroll));
    }

    @Transactional
    public PayrollDTO validate(Long id, String username) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));
        if (payroll.getStatut() != Payroll.StatutPayroll.CALCULE) {
            throw new RuntimeException("payroll.error.cannotValidate");
        }

        List<PretRemboursement> remboursements = pretRemboursementRepository.findByNoPayroll(payroll.getId().intValue());
        Set<Long> remboursementsPretIds = new HashSet<>();
        for (PretRemboursement remboursement : remboursements) {
            remboursement.setStatut(PretRemboursement.StatutRemboursement.PAYE);
            remboursement.setUpdatedBy(username);
            remboursement.setUpdatedOn(OffsetDateTime.now());
            remboursement.setRowscn(remboursement.getRowscn() + 1);
            pretRemboursementRepository.save(remboursement);

            PretEmploye pret = remboursement.getPretEmploye();
            if (pret != null && pret.getId() != null) {
                remboursementsPretIds.add(pret.getId());
            }
            BigDecimal montantVerse = pret.getMontantVerse() != null ? pret.getMontantVerse() : BigDecimal.ZERO;
            BigDecimal nouveauVerse = montantVerse.add(remboursement.getMontantRembourse());
            pret.setMontantVerse(nouveauVerse);
            pret.setDernierPrelevement(remboursement.getDateRemboursement());
            updatePretFrequenceCompteur(pret, true);
            if (nouveauVerse.compareTo(pret.getMontantPret()) >= 0) {
                pret.setStatut(PretEmploye.StatutPret.TERMINE);
            }
            pret.setUpdatedBy(username);
            pret.setUpdatedOn(OffsetDateTime.now());
            pret.setRowscn(pret.getRowscn() + 1);
            pretEmployeRepository.save(pret);
        }

        if (!remboursements.isEmpty() || payroll.getRegimePaie() != null) {
            List<PayrollEmploye> payrollEmployes = payrollEmployeRepository.findByPayrollId(payroll.getId());
            for (PayrollEmploye payrollEmploye : payrollEmployes) {
                Employe employe = payrollEmploye.getEmploye();
                if (employe == null || employe.getId() == null) {
                    continue;
                }
                List<PretEmploye> prets = pretEmployeRepository.findPrelevablesForPayroll(
                        employe.getId(),
                        payroll.getRegimePaie() != null ? payroll.getRegimePaie().getId() : null
                );
                for (PretEmploye pret : prets) {
                    if (pret == null || pret.getId() == null) {
                        continue;
                    }
                    if (remboursementsPretIds.contains(pret.getId())) {
                        continue;
                    }
                    updatePretFrequenceCompteur(pret, false);
                    pret.setUpdatedBy(username);
                    pret.setUpdatedOn(OffsetDateTime.now());
                    pret.setRowscn(pret.getRowscn() + 1);
                    pretEmployeRepository.save(pret);
                }
            }
        }

        RegimePaie regimePaie = payroll.getRegimePaie() != null && payroll.getRegimePaie().getId() != null
                ? regimePaieRepository.findById(payroll.getRegimePaie().getId()).orElse(null)
                : null;
        if (regimePaie != null) {
            LocalDate dateFin = payroll.getDateFin();
            regimePaie.setDernierePaie(dateFin);
            regimePaie.setProchainePaie(computeNextPayrollDate(dateFin, regimePaie.getPeriodicite(), 1));
            boolean taxesApplied = shouldApplyTaxes(regimePaie, payroll);
            if (taxesApplied) {
                regimePaie.setDernierPrelevement(dateFin);
            }
            boolean supplementsApplied = shouldApplySupplements(regimePaie, payroll);
            if (supplementsApplied) {
                regimePaie.setDernierSupplement(dateFin);
            }
            regimePaie.setProchainSupplement(computeNextSupplementDate(regimePaie, payroll, supplementsApplied));
            regimePaie.setUpdatedBy(username);
            regimePaie.setUpdatedOn(OffsetDateTime.now());
            regimePaieRepository.save(regimePaie);
        }

        payroll.setStatut(Payroll.StatutPayroll.VALIDE);
        payroll.setUpdatedBy(username);
        payroll.setUpdatedOn(OffsetDateTime.now());
        return toDTO(payrollRepository.save(payroll));
    }

    @Transactional
    public PayrollDTO finalise(Long id, String username) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));
        if (payroll.getStatut() != Payroll.StatutPayroll.VALIDE) {
            throw new RuntimeException("payroll.error.cannotFinalize");
        }
        payroll.setStatut(Payroll.StatutPayroll.FINALISE);
        payroll.setUpdatedBy(username);
        payroll.setUpdatedOn(OffsetDateTime.now());
        return toDTO(payrollRepository.save(payroll));
    }

    @Transactional(readOnly = true)
    public List<PayrollEmployeDTO> findEmployes(Long payrollId) {
        return payrollEmployeRepository.findByPayrollId(payrollId).stream()
                .map(this::toEmployeDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PayrollGainDTO> findGains(Long payrollId) {
        return payrollGainRepository.findByPayrollId(payrollId).stream()
                .map(this::toGainDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PayrollDeductionDTO> findDeductions(Long payrollId) {
        return payrollDeductionRepository.findByPayrollId(payrollId).stream()
                .map(this::toDeductionDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PayrollRecouvrementDTO> findRecouvrements(Long payrollId) {
        return payrollRecouvrementRepository.findByPayrollId(payrollId).stream()
                .map(this::toRecouvrementDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PayrollSanctionDTO> findSanctions(Long payrollId) {
        return payrollSanctionRepository.findByPayrollId(payrollId).stream()
                .map(this::toSanctionDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PayrollEmployeStatsDTO> findStats(Long payrollId) {
        List<PayrollEmployeStats> stats = payrollEmployeStatsRepository.findByPayrollId(payrollId);
        Map<String, PayrollEmployeStatsDTO> aggregated = new LinkedHashMap<>();
        for (PayrollEmployeStats stat : stats) {
            String key = String.join("|",
                    stat.getPayrollEmploye().getId().toString(),
                    stat.getMetricCode(),
                    stat.getMetricLabel(),
                    stat.getMetricGroup().name(),
                    stat.getUniteMesure().name());
            PayrollEmployeStatsDTO dto = aggregated.get(key);
            if (dto == null) {
                dto = new PayrollEmployeStatsDTO();
                dto.setPayrollId(stat.getPayroll().getId());
                dto.setPayrollEmployeId(stat.getPayrollEmploye().getId());
                dto.setMetricCode(stat.getMetricCode());
                dto.setMetricLabel(stat.getMetricLabel());
                dto.setMetricGroup(stat.getMetricGroup().name());
                dto.setUniteMesure(stat.getUniteMesure().name());
                dto.setQuantite(BigDecimal.ZERO);
                dto.setMontant(BigDecimal.ZERO);
                aggregated.put(key, dto);
            }
            dto.setQuantite(dto.getQuantite().add(stat.getQuantite() != null ? stat.getQuantite() : BigDecimal.ZERO));
            dto.setMontant(dto.getMontant().add(stat.getMontant() != null ? stat.getMontant() : BigDecimal.ZERO));
        }
        return new ArrayList<>(aggregated.values());
    }

    private PayrollEmploye buildPayrollEmploye(Payroll payroll, EmployeSalaire salaire, String username) {
        PayrollEmploye payrollEmploye = new PayrollEmploye();
        payrollEmploye.setPayroll(payroll);
        payrollEmploye.setEmploye(salaire.getEmploye());
        applyPaymentInfo(payrollEmploye, salaire.getEmploye());
        payrollEmploye.setCreatedBy(username);
        payrollEmploye.setCreatedOn(OffsetDateTime.now());
        payrollEmploye.setRowscn(1);
        return payrollEmploye;
    }

    private void applyPaymentInfo(PayrollEmploye payrollEmploye, Employe employe) {
        List<CoordonneeBancaireEmploye> comptes = coordonneeBancaireEmployeRepository
                .findByEmployeIdAndActif(employe.getId(), "Y");
        CoordonneeBancaireEmploye principal = comptes.stream()
                .filter(c -> c.getCategorie() == CoordonneeBancaireEmploye.CategorieCompte.PRINCIPAL)
                .findFirst()
                .orElse(null);
        if (principal != null) {
            payrollEmploye.setModePaiement(PayrollEmploye.ModePaiement.VIREMENT);
            payrollEmploye.setLibelleBanque(principal.getBanque().getNom());
            payrollEmploye.setNoCompte(principal.getNumeroCompte());
            payrollEmploye.setTypeCompte(principal.getBanque().getReference());
        } else {
            payrollEmploye.setModePaiement(PayrollEmploye.ModePaiement.CHEQUE);
        }
    }

    private BigDecimal computeSalaireBase(EmployeSalaire salaire, RegimePaie regimePaie, Payroll payroll, String username) {
        EmploiEmploye emploi = salaire.getEmploi();
        BigDecimal heuresRef = emploi != null && emploi.getHoraire() != null && emploi.getHoraire().getNbHeuresRef() != null
                ? emploi.getHoraire().getNbHeuresRef()
                : BigDecimal.valueOf(8);
        BigDecimal dailyRate = computeDailyRate(salaire.getMontant(), regimePaie, heuresRef);
        RegimePaie.Periodicite periodicite = regimePaie.getPeriodicite();
        LocalDate dateDebut = payroll.getDateDebut();
        LocalDate dateFin = payroll.getDateFin();
        boolean isQuinz = periodicite == RegimePaie.Periodicite.QUINZAINE
                || periodicite == RegimePaie.Periodicite.QUINZOMADAIRE;
        long days = isQuinz
                ? ChronoUnit.DAYS.between(dateDebut, dateFin)
                : ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;

        if (regimePaie.getModeRemuneration() == RegimePaie.ModeRemuneration.PIECE
                || regimePaie.getModeRemuneration() == RegimePaie.ModeRemuneration.PIECE_FIXE) {
            List<ProductionPiece.StatutProduction> statuts = List.of(
                    ProductionPiece.StatutProduction.VALIDE,
                    ProductionPiece.StatutProduction.PAYE
            );
            BigDecimal totalPieces = isQuinz
                    ? productionPieceRepository.sumMontantTotalByEmployeAndPeriodExclusive(
                            salaire.getEmploye().getId(), dateDebut, dateFin, statuts)
                    : productionPieceRepository.sumMontantTotalByEmployeAndPeriod(
                            salaire.getEmploye().getId(), dateDebut, dateFin, statuts);

            long daysWithPieces = isQuinz
                    ? productionPieceRepository.countDistinctDaysByEmployeAndPeriodExclusive(
                            salaire.getEmploye().getId(), dateDebut, dateFin, statuts)
                    : productionPieceRepository.countDistinctDaysByEmployeAndPeriod(
                            salaire.getEmploye().getId(), dateDebut, dateFin, statuts);

            if (regimePaie.getModeRemuneration() == RegimePaie.ModeRemuneration.PIECE_FIXE) {
                long missingDays = Math.max(days - daysWithPieces, 0);
                BigDecimal fallback = dailyRate.multiply(BigDecimal.valueOf(missingDays));
                totalPieces = totalPieces.add(fallback);
            }

            if (isQuinz) {
                productionPieceRepository.updatePayrollIdByEmployeAndPeriodExclusive(
                        payroll.getId(),
                        username,
                        OffsetDateTime.now(),
                        salaire.getEmploye().getId(),
                        dateDebut,
                        dateFin,
                        statuts
                );
            } else {
                productionPieceRepository.updatePayrollIdByEmployeAndPeriod(
                        payroll.getId(),
                        username,
                        OffsetDateTime.now(),
                        salaire.getEmploye().getId(),
                        dateDebut,
                        dateFin,
                        statuts
                );
            }

            return totalPieces.setScale(2, RoundingMode.HALF_UP);
        }

        return dailyRate.multiply(BigDecimal.valueOf(Math.max(days, 0))).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeSupplementaire(EmployeSalaire salaire,
                                             RegimePaie regimePaie,
                                             Payroll payroll,
                                             PayrollEmploye payrollEmploye,
                                             RubriquePaie rubriqueSupplementaire,
                                             String username,
                                             boolean applySupplements) {
        if (salaire == null || !isYes(salaire.getPrincipal())) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (!applySupplements) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        Employe employe = salaire.getEmploye();
        BigDecimal total = BigDecimal.ZERO;
        List<SupplementaireEmploye> supplementaires = supplementaireEmployeRepository
                .findValidesForPayroll(
                        employe.getId(),
                        payroll.getDateDebut(),
                        payroll.getDateFin());
        for (SupplementaireEmploye supplementaire : supplementaires) {
            BigDecimal montant = supplementaire.getMontantCalcule() != null ? supplementaire.getMontantCalcule() : BigDecimal.ZERO;
            total = total.add(montant);
            supplementaire.setNoPayroll(payroll.getId().intValue());
            supplementaireEmployeRepository.save(supplementaire);
        }
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            payrollGainRepository.save(buildGain(payroll, payrollEmploye, rubriqueSupplementaire,
                    total, "SYSTEME", null, username));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeAutreRevenu(EmployeSalaire salaire, Payroll payroll, PayrollEmploye payrollEmploye,
                                          RubriquePaie rubriqueAutreRevenu, String username) {
        BigDecimal total = BigDecimal.ZERO;
        Map<Long, BigDecimal> montantByRubrique = new HashMap<>();
        Map<Long, RubriquePaie> rubriqueById = new HashMap<>();
        List<AutreRevenuEmploye> revenus = autreRevenuEmployeRepository
                .findValidesForPayroll(salaire.getEmploye().getId(), payroll.getDateDebut(), payroll.getDateFin());
        Map<String, Object> variables = buildAutreRevenuVariables(salaire, payroll);

        for (AutreRevenuEmploye revenu : revenus) {
            TypeRevenu typeRevenu = revenu.getTypeRevenu();
            BigDecimal montant = revenu.getMontant() != null ? revenu.getMontant() : BigDecimal.ZERO;
            variables.put("amt.value", montant);
            if (typeRevenu != null && typeRevenu.getFormule() != null && isFormuleEffective(typeRevenu.getFormule(), payroll.getDateFin())) {
                String expression = normalizeExpression(typeRevenu.getFormule().getExpression());
                if (expression != null && !expression.isBlank()) {
                    BigDecimal eval = formulaEvaluator.evaluate(expression, variables, false);
                    if (eval != null) {
                        montant = eval;
                    }
                }
            }

            RubriquePaie rubriqueGain = typeRevenu != null && typeRevenu.getRubriquePaie() != null
                    ? typeRevenu.getRubriquePaie()
                    : rubriqueAutreRevenu;
            if (rubriqueGain != null && rubriqueGain.getId() != null) {
                rubriqueById.putIfAbsent(rubriqueGain.getId(), rubriqueGain);
                montantByRubrique.merge(rubriqueGain.getId(), montant, BigDecimal::add);
            }

            if (typeRevenu != null && "Y".equalsIgnoreCase(typeRevenu.getAjouterSalBase())) {
                payrollEmploye.setMontantSalaireBase(payrollEmploye.getMontantSalaireBase().add(montant));
            } else {
                total = total.add(montant);
            }

            revenu.setPayrollNo(payroll.getId().intValue());
            autreRevenuEmployeRepository.save(revenu);
        }

        for (Map.Entry<Long, BigDecimal> entry : montantByRubrique.entrySet()) {
            BigDecimal montant = entry.getValue() != null ? entry.getValue() : BigDecimal.ZERO;
            if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            RubriquePaie rubriqueGain = rubriqueById.get(entry.getKey());
            payrollGainRepository.save(buildGain(payroll, payrollEmploye, rubriqueGain,
                    montant, "SYSTEME", null, username));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeSanctions(Employe employe, Payroll payroll, PayrollEmploye payrollEmploye, String username) {
        BigDecimal total = BigDecimal.ZERO;
        Map<PayrollSanction.TypeSanction, BigDecimal> montantByType = new HashMap<>();
        Map<PayrollSanction.TypeSanction, BigDecimal> quantiteMinuteByType = new HashMap<>();
        if (tableExists("sanction_employe")) {
            List<SanctionEmploye> sanctions = sanctionEmployeRepository
                    .findValidesForPayroll(employe.getId(), payroll.getDateDebut(), payroll.getDateFin());
            for (SanctionEmploye sanction : sanctions) {
                BigDecimal montant = sanction.getMontantCalcule() != null ? sanction.getMontantCalcule() : BigDecimal.ZERO;
                total = total.add(montant);
                PayrollSanction.TypeSanction type = mapSanctionType(sanction.getTypeEvenement());
                BigDecimal quantiteMinute = toMinutes(sanction.getValeurMesuree(), sanction.getUniteMesure());
                montantByType.merge(type, montant, BigDecimal::add);
                quantiteMinuteByType.merge(type, quantiteMinute, BigDecimal::add);
            }
        }

        List<AbsenceEmploye> absences = absenceEmployeRepository
                .findValidesForPayroll(employe.getId(), payroll.getDateDebut(), payroll.getDateFin());
        for (AbsenceEmploye absence : absences) {
            BigDecimal montant = absence.getMontantEquivalent() != null ? absence.getMontantEquivalent() : BigDecimal.ZERO;
            total = total.add(montant);
            PayrollSanction.TypeSanction type = mapAbsenceType(absence.getTypeEvenement());
            BigDecimal quantiteMinute = toMinutes(absence.getQuantite(), absence.getUniteMesure());
            montantByType.merge(type, montant, BigDecimal::add);
            quantiteMinuteByType.merge(type, quantiteMinute, BigDecimal::add);
            absence.setPayrollId(payroll.getId());
            absenceEmployeRepository.save(absence);
        }
        for (PayrollSanction.TypeSanction type : montantByType.keySet()) {
            PayrollSanction payrollSanction = new PayrollSanction();
            payrollSanction.setPayroll(payroll);
            payrollSanction.setPayrollEmploye(payrollEmploye);
            payrollSanction.setTypeSanction(type);
            payrollSanction.setDateJour(null);
            payrollSanction.setQuantiteMinute(quantiteMinuteByType.getOrDefault(type, BigDecimal.ZERO));
            payrollSanction.setMontant(montantByType.getOrDefault(type, BigDecimal.ZERO));
            payrollSanction.setReferenceExterne(null);
            payrollSanction.setCreatedBy(username);
            payrollSanction.setCreatedOn(OffsetDateTime.now());
            payrollSanction.setRowscn(1);
            payrollSanctionRepository.save(payrollSanction);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private PayrollSanction.TypeSanction mapSanctionType(SanctionEmploye.TypeEvenement typeEvenement) {
        if (typeEvenement == null) {
            return PayrollSanction.TypeSanction.AUTRE;
        }
        return switch (typeEvenement) {
            case RETARD -> PayrollSanction.TypeSanction.RETARD;
            case ABSENCE -> PayrollSanction.TypeSanction.ABSENCE;
            case AUTRE -> PayrollSanction.TypeSanction.AUTRE;
        };
    }

    private PayrollSanction.TypeSanction mapAbsenceType(AbsenceEmploye.TypeEvenement typeEvenement) {
        if (typeEvenement == null) {
            return PayrollSanction.TypeSanction.ABSENCE;
        }
        return switch (typeEvenement) {
            case RETARD -> PayrollSanction.TypeSanction.RETARD;
            case ABSENCE -> PayrollSanction.TypeSanction.ABSENCE;
        };
    }

    private BigDecimal toMinutes(BigDecimal quantite, SanctionEmploye.UniteMesure unite) {
        if (quantite == null) {
            return BigDecimal.ZERO;
        }
        if (unite == null) {
            return quantite;
        }
        return switch (unite) {
            case MINUTE -> quantite;
            case HEURE -> quantite.multiply(BigDecimal.valueOf(60));
            case JOUR -> quantite.multiply(BigDecimal.valueOf(8 * 60L));
        };
    }

    private BigDecimal toMinutes(BigDecimal quantite, AbsenceEmploye.UniteMesure unite) {
        if (quantite == null) {
            return BigDecimal.ZERO;
        }
        if (unite == null) {
            return quantite;
        }
        return switch (unite) {
            case MINUTE -> quantite;
            case HEURE -> quantite.multiply(BigDecimal.valueOf(60));
            case JOUR -> quantite.multiply(BigDecimal.valueOf(8 * 60L));
        };
    }

    private boolean tableExists(String tableName) {
        try {
            String reg = jdbcTemplate.queryForObject(
                    "SELECT to_regclass(?)",
                    String.class,
                    "public." + tableName);
            return reg != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean shouldApplyTaxes(RegimePaie regimePaie, Payroll payroll) {
        if (regimePaie == null) {
            return true;
        }
        if (!isYes(regimePaie.getTaxable())) {
            return false;
        }
        Integer taxeEvery = regimePaie.getTaxeChaqueNPaies();
        if (taxeEvery == null || taxeEvery <= 1) {
            return true;
        }
        LocalDate lastTaxedDate = regimePaie.getDernierPrelevement();
        List<Payroll.StatutPayroll> taxStatuts = List.of(Payroll.StatutPayroll.VALIDE, Payroll.StatutPayroll.FINALISE);
        long completed;
        if (lastTaxedDate != null) {
            completed = payrollRepository.countByRegimePaieIdAndStatutInAndDateFinRange(
                    regimePaie.getId(),
                    taxStatuts,
                    lastTaxedDate,
                    payroll.getDateFin()
            );
        } else {
            completed = payrollRepository.countByRegimePaieIdAndStatutInAndDateFinBeforeOrEqual(
                    regimePaie.getId(),
                    taxStatuts,
                    payroll.getDateFin()
            );
        }
        long sequence = completed + 1;
        return sequence % taxeEvery == 0;
    }

    private boolean shouldApplySupplements(RegimePaie regimePaie, Payroll payroll) {
        if (regimePaie == null || payroll == null) {
            return true;
        }
        Integer suppEvery = regimePaie.getSuppChaqueNPaies();
        if (suppEvery == null || suppEvery <= 1) {
            return true;
        }
        int decalage = regimePaie.getSuppDecalageNbPaies() != null ? regimePaie.getSuppDecalageNbPaies() : 0;
        LocalDate lastSuppDate = regimePaie.getDernierSupplement();
        long completed = countCompletedPayrolls(regimePaie.getId(), lastSuppDate, payroll.getDateFin());
        long sequence = completed + 1 + Math.max(decalage, 0);
        return sequence % suppEvery == 0;
    }

    private long countCompletedPayrolls(Long regimePaieId, LocalDate lastDate, LocalDate currentDateFin) {
        if (regimePaieId == null || currentDateFin == null) {
            return 0;
        }
        List<Payroll.StatutPayroll> statuts = List.of(Payroll.StatutPayroll.VALIDE, Payroll.StatutPayroll.FINALISE);
        if (lastDate != null) {
            return payrollRepository.countByRegimePaieIdAndStatutInAndDateFinRange(
                    regimePaieId,
                    statuts,
                    lastDate,
                    currentDateFin
            );
        }
        return payrollRepository.countByRegimePaieIdAndStatutInAndDateFinBeforeOrEqual(
                regimePaieId,
                statuts,
                currentDateFin
        );
    }

    private LocalDate computeNextPayrollDate(LocalDate dateFin, RegimePaie.Periodicite periodicite, int periodsToAdd) {
        if (dateFin == null || periodicite == null) {
            return dateFin;
        }
        int periods = Math.max(periodsToAdd, 1);
        return switch (periodicite) {
            case JOURNALIER -> dateFin.plusDays(periods);
            case HEBDO -> dateFin.plusWeeks(periods);
            case QUINZAINE -> dateFin.plusDays(14L * periods);
            case QUINZOMADAIRE -> dateFin.plusDays(15L * periods);
            case MENSUEL -> dateFin.plusMonths(periods);
            case TRIMESTRIEL -> dateFin.plusMonths(3L * periods);
            case SEMESTRIEL -> dateFin.plusMonths(6L * periods);
            case ANNUEL -> dateFin.plusYears(periods);
        };
    }

    private LocalDate computeNextSupplementDate(RegimePaie regimePaie, Payroll payroll, boolean appliedThisPayroll) {
        if (regimePaie == null || payroll == null || payroll.getDateFin() == null) {
            return null;
        }
        Integer suppEvery = regimePaie.getSuppChaqueNPaies();
        if (suppEvery == null || suppEvery <= 1) {
            return computeNextPayrollDate(payroll.getDateFin(), regimePaie.getPeriodicite(), 1);
        }
        int decalage = regimePaie.getSuppDecalageNbPaies() != null ? regimePaie.getSuppDecalageNbPaies() : 0;
        if (appliedThisPayroll) {
            return computeNextPayrollDate(payroll.getDateFin(), regimePaie.getPeriodicite(), suppEvery);
        }
        LocalDate lastSuppDate = regimePaie.getDernierSupplement();
        long completed = countCompletedPayrolls(regimePaie.getId(), lastSuppDate, payroll.getDateFin());
        long sequence = completed + 1 + Math.max(decalage, 0);
        int remainder = (int) (suppEvery - (sequence % suppEvery));
        if (remainder == 0) {
            remainder = suppEvery;
        }
        return computeNextPayrollDate(payroll.getDateFin(), regimePaie.getPeriodicite(), remainder);
    }

    private BigDecimal computeDeductions(EmployeSalaire salaire, PayrollEmploye payrollEmploye, String username) {
        return computeDeductionsAggregated(
                salaire,
                payrollEmploye,
                List.of(payrollEmploye),
                1,
                username
        );
    }

    private BigDecimal computeDeductionsForTaxCycle(EmployeSalaire salaire,
                                                    PayrollEmploye payrollEmploye,
                                                    Payroll payroll,
                                                    RegimePaie regimePaie,
                                                    String username) {
        Integer taxeEvery = regimePaie.getTaxeChaqueNPaies();
        if (taxeEvery == null || taxeEvery <= 1) {
            BigDecimal total = computeDeductionsAggregated(
                    salaire,
                    payrollEmploye,
                    List.of(payrollEmploye),
                    1,
                    username
            );
            upsertTaxeCycle(salaire, payroll, username);
            return total;
        }

        Long employeId = salaire.getEmploye() != null ? salaire.getEmploye().getId() : null;
        if (employeId == null) {
            return BigDecimal.ZERO;
        }

        PayrollTaxeCycle cycle = payrollTaxeCycleRepository
                .findByEmployeIdAndRegimePaieId(employeId, regimePaie.getId())
                .orElse(null);
        LocalDate lastTaxedDate = cycle != null ? cycle.getDernierTaxeDateFin() : regimePaie.getDernierPrelevement();
        List<Payroll.StatutPayroll> taxStatuts = List.of(Payroll.StatutPayroll.VALIDE, Payroll.StatutPayroll.FINALISE);
        List<PayrollEmploye> previousPayrolls;
        if (lastTaxedDate != null) {
            previousPayrolls = payrollEmployeRepository.findForTaxeCycle(
                    employeId,
                    regimePaie.getId(),
                    taxStatuts,
                    lastTaxedDate,
                    payroll.getDateFin()
            );
        } else {
            previousPayrolls = payrollEmployeRepository.findForTaxeCycleFromStart(
                    employeId,
                    regimePaie.getId(),
                    taxStatuts,
                    payroll.getDateFin()
            );
        }

        List<PayrollEmploye> allPayrolls = new ArrayList<>(previousPayrolls);
        allPayrolls.add(payrollEmploye);

        int periodMultiplier = resolveTaxPeriodMultiplier(regimePaie.getId(), lastTaxedDate, payroll.getDateFin());
        BigDecimal total = computeDeductionsAggregated(
                salaire,
                payrollEmploye,
                allPayrolls,
                periodMultiplier,
                username
        );
        upsertTaxeCycle(salaire, payroll, username);
        return total;
    }

    private BigDecimal computeDeductionsAggregated(EmployeSalaire salaire,
                                                   PayrollEmploye payrollEmploye,
                                                   List<PayrollEmploye> payrolls,
                                                   int periodMultiplier,
                                                   String username) {
        BigDecimal total = BigDecimal.ZERO;
        List<PayrollGain> gains = collectGains(payrolls);
        Map<Long, BigDecimal> gainByRubrique = new HashMap<>();
        for (PayrollGain gain : gains) {
            if (gain.getRubriquePaie() == null || gain.getRubriquePaie().getId() == null) {
                continue;
            }
            BigDecimal montant = gain.getMontant() != null ? gain.getMontant() : BigDecimal.ZERO;
            gainByRubrique.merge(gain.getRubriquePaie().getId(), montant, BigDecimal::add);
        }

        BigDecimal baseMontantNonSpecial = computeBaseMontantNonSpecial(gains);
        BigDecimal baseMontantCotisations = computeBaseMontantCotisations(gains);

        Set<String> excludedCodes = new HashSet<>();
        if (salaire.getEmploi() != null && salaire.getEmploi().getTypeEmploye() != null) {
            List<ExclusionDeduction> exclusions = exclusionDeductionRepository
                    .findByTypeEmployeId(salaire.getEmploi().getTypeEmploye().getId());
            for (ExclusionDeduction exclusion : exclusions) {
                excludedCodes.add(exclusion.getDefinitionDeduction().getCodeDeduction());
            }
        }

        RegimePaie regimePaie = payrollEmploye != null && payrollEmploye.getPayroll() != null
                ? payrollEmploye.getPayroll().getRegimePaie()
                : null;
        List<DefinitionDeduction> deductions = new ArrayList<>();
        if (regimePaie != null && regimePaie.getId() != null) {
            for (RegimePaieDeduction link : regimePaieDeductionRepository.findByRegimePaieId(regimePaie.getId())) {
                if (link.getDeductionCode() != null) {
                    deductions.add(link.getDeductionCode());
                }
            }
        }
        for (DefinitionDeduction deduction : deductions) {
            if (excludedCodes.contains(deduction.getCodeDeduction())) {
                continue;
            }
            if (shouldSkipForProbation(deduction, salaire.getEmploi())) {
                continue;
            }
            BigDecimal baseMontant = isYes(deduction.getSpecialise())
                    ? computeBaseMontantSpecial(deduction, gainByRubrique)
                    : baseMontantNonSpecial;
            baseMontant = applyAbattement(baseMontant, deduction.getPctHorsCalcul());
            boolean annuel = deduction.getBaseLimite() == DefinitionDeduction.BaseLimite.ANNUEL;
            if (annuel) {
                baseMontant = PayrollTaxePeriod.toAnnualAmount(
                        baseMontant,
                        payrollEmploye.getPayroll().getRegimePaie().getPeriodicite(),
                        periodMultiplier
                );
            }
            baseMontant = applyMinMax(baseMontant, deduction.getMinPrelevement(), deduction.getMaxPrelevement());

            DeductionSplit split = computeDeductionSplit(deduction, baseMontant);
            if (annuel) {
                split = new DeductionSplit(
                        PayrollTaxePeriod.fromAnnualAmount(
                                split.montantEmploye,
                                payrollEmploye.getPayroll().getRegimePaie().getPeriodicite(),
                                periodMultiplier
                        ),
                        PayrollTaxePeriod.fromAnnualAmount(
                                split.montantCouvert,
                                payrollEmploye.getPayroll().getRegimePaie().getPeriodicite(),
                                periodMultiplier
                        )
                );
                baseMontant = PayrollTaxePeriod.fromAnnualAmount(
                        baseMontant,
                        payrollEmploye.getPayroll().getRegimePaie().getPeriodicite(),
                        periodMultiplier
                );
            }
            split = applyArrondir(split, deduction.getArrondir());
            BigDecimal montant = split.montantEmploye;
            if (isZero(montant) && isZero(split.montantCouvert)) {
                continue;
            }
            PayrollDeduction payrollDeduction = new PayrollDeduction();
            payrollDeduction.setPayroll(payrollEmploye.getPayroll());
            payrollDeduction.setPayrollEmploye(payrollEmploye);
            payrollDeduction.setCodeDeduction(deduction.getCodeDeduction());
            payrollDeduction.setLibelle(deduction.getLibelle());
            payrollDeduction.setCategorie(PayrollDeduction.CategorieDeduction.TAXE);
            payrollDeduction.setBaseMontant(baseMontant);
            payrollDeduction.setTaux(deduction.getTypeDeduction() == DefinitionDeduction.TypeDeduction.POURCENTAGE
                    ? deduction.getValeur() : BigDecimal.ZERO);
            payrollDeduction.setMontant(montant);
            payrollDeduction.setMontantCouvert(split.montantCouvert);
            payrollDeduction.setCreatedBy(username);
            payrollDeduction.setCreatedOn(OffsetDateTime.now());
            payrollDeduction.setRowscn(1);
            payrollDeductionRepository.save(payrollDeduction);
            total = total.add(montant);
        }

        Long employeId = salaire.getEmploye() != null ? salaire.getEmploye().getId() : null;
        if (!isYes(salaire.getPrincipal())) {
            return total.setScale(2, RoundingMode.HALF_UP);
        }
        Set<Long> employePlanAssuranceIds = new HashSet<>();
        if (employeId != null) {
            assuranceEmployeRepository.findByEmployeIdAndActif(employeId, "Y").forEach(assurance ->
                    employePlanAssuranceIds.add(assurance.getPlanAssurance().getId()));
        }

        for (PlanAssurance plan : planAssuranceRepository.findAll()) {
            if (!employePlanAssuranceIds.contains(plan.getId())) {
                continue;
            }
            DeductionSplit split = computePlanAssuranceSplit(plan, baseMontantCotisations);
            BigDecimal montant = split.montantEmploye;
            if (isZero(montant) && isZero(split.montantCouvert)) {
                continue;
            }
            PayrollDeduction payrollDeduction = new PayrollDeduction();
            payrollDeduction.setPayroll(payrollEmploye.getPayroll());
            payrollDeduction.setPayrollEmploye(payrollEmploye);
            payrollDeduction.setCodeDeduction(plan.getCodePlan());
            payrollDeduction.setLibelle(plan.getLibelle());
            payrollDeduction.setCategorie(PayrollDeduction.CategorieDeduction.ASSURANCE);
            payrollDeduction.setBaseMontant(baseMontantCotisations);
            payrollDeduction.setTaux(plan.getTypePrelevement() == PlanAssurance.TypePrelevement.POURCENTAGE
                    ? plan.getValeur() : BigDecimal.ZERO);
            payrollDeduction.setMontant(montant);
            payrollDeduction.setMontantCouvert(split.montantCouvert);
            payrollDeduction.setCreatedBy(username);
            payrollDeduction.setCreatedOn(OffsetDateTime.now());
            payrollDeduction.setRowscn(1);
            payrollDeductionRepository.save(payrollDeduction);
            total = total.add(montant);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private List<PayrollGain> collectGains(List<PayrollEmploye> payrolls) {
        List<PayrollGain> gains = new ArrayList<>();
        if (payrolls == null) {
            return gains;
        }
        for (PayrollEmploye pe : payrolls) {
            if (pe == null || pe.getId() == null) {
                continue;
            }
            gains.addAll(payrollGainRepository.findByPayrollEmployeId(pe.getId()));
        }
        return gains;
    }

    private int resolveTaxPeriodMultiplier(Long regimePaieId, LocalDate lastTaxedDate, LocalDate currentDateFin) {
        List<Payroll.StatutPayroll> taxStatuts = List.of(Payroll.StatutPayroll.VALIDE, Payroll.StatutPayroll.FINALISE);
        long completed;
        if (lastTaxedDate != null) {
            completed = payrollRepository.countByRegimePaieIdAndStatutInAndDateFinRange(
                    regimePaieId,
                    taxStatuts,
                    lastTaxedDate,
                    currentDateFin
            );
        } else {
            completed = payrollRepository.countByRegimePaieIdAndStatutInAndDateFinBeforeOrEqual(
                    regimePaieId,
                    taxStatuts,
                    currentDateFin
            );
        }
        int multiplier = (int) completed + 1;
        return Math.max(multiplier, 1);
    }

    private void upsertTaxeCycle(EmployeSalaire salaire, Payroll payroll, String username) {
        if (salaire == null || salaire.getEmploye() == null || payroll == null || payroll.getRegimePaie() == null) {
            return;
        }
        Long employeId = salaire.getEmploye().getId();
        Long regimeId = payroll.getRegimePaie().getId();
        PayrollTaxeCycle cycle = payrollTaxeCycleRepository
                .findByEmployeIdAndRegimePaieId(employeId, regimeId)
                .orElse(null);
        if (cycle == null) {
            cycle = new PayrollTaxeCycle();
            cycle.setEmploye(salaire.getEmploye());
            cycle.setRegimePaie(payroll.getRegimePaie());
            cycle.setCreatedBy(username);
            cycle.setCreatedOn(OffsetDateTime.now());
            cycle.setRowscn(1);
        }
        cycle.setDernierPayrollTaxe(payroll);
        cycle.setDernierTaxeDateFin(payroll.getDateFin());
        cycle.setUpdatedBy(username);
        cycle.setUpdatedOn(OffsetDateTime.now());
        payrollTaxeCycleRepository.save(cycle);
    }

    private BigDecimal computeBaseMontantNonSpecial(List<PayrollGain> gains) {
        BigDecimal total = BigDecimal.ZERO;
        for (PayrollGain gain : gains) {
            if (gain.getRubriquePaie() == null) {
                continue;
            }
            if (!isYes(gain.getImposable())) {
                continue;
            }
            if (isYes(gain.getRubriquePaie().getTaxesSpeciaux())) {
                continue;
            }
            BigDecimal montant = gain.getMontant() != null ? gain.getMontant() : BigDecimal.ZERO;
            total = total.add(montant);
        }
        return total;
    }

    private BigDecimal computeBaseMontantCotisations(List<PayrollGain> gains) {
        BigDecimal total = BigDecimal.ZERO;
        for (PayrollGain gain : gains) {
            if (gain.getRubriquePaie() == null) {
                continue;
            }
            if (!isYes(gain.getSoumisCotisations())) {
                continue;
            }
            if (isYes(gain.getRubriquePaie().getTaxesSpeciaux())) {
                continue;
            }
            BigDecimal montant = gain.getMontant() != null ? gain.getMontant() : BigDecimal.ZERO;
            total = total.add(montant);
        }
        return total;
    }

    private BigDecimal computeBaseMontantSpecial(DefinitionDeduction deduction,
                                                 Map<Long, BigDecimal> gainByRubrique) {
        BigDecimal total = BigDecimal.ZERO;
        List<RubriquePaieDeduction> rubriques = rubriquePaieDeductionRepository
                .findByDefinitionDeductionId(deduction.getId());
        for (RubriquePaieDeduction link : rubriques) {
            RubriquePaie rubrique = link.getRubriquePaie();
            if (rubrique == null || rubrique.getId() == null) {
                continue;
            }
            if (!isYes(rubrique.getImposable())) {
                continue;
            }
            BigDecimal montant = gainByRubrique.getOrDefault(rubrique.getId(), BigDecimal.ZERO);
            total = total.add(montant);
        }
        return total;
    }

    private boolean isYes(String value) {
        return value != null && "Y".equalsIgnoreCase(value.trim());
    }

    private BigDecimal computeRecouvrements(Employe employe, RegimePaie regimePaie, Payroll payroll, PayrollEmploye payrollEmploye, String username) {
        BigDecimal total = BigDecimal.ZERO;
        List<PretEmploye> prets = pretEmployeRepository.findPrelevablesForPayroll(employe.getId(), regimePaie.getId());
        for (PretEmploye pret : prets) {
            if (!shouldPreleverPret(pret, payroll.getDateDebut(), payroll.getDateFin())) {
                continue;
            }
            BigDecimal soldeAvant = pret.getMontantPret().subtract(pret.getMontantVerse() != null ? pret.getMontantVerse() : BigDecimal.ZERO);
            if (soldeAvant.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal montantPeriode = pret.getMontantPeriode() != null ? pret.getMontantPeriode() : BigDecimal.ZERO;
            if (montantPeriode.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (montantPeriode.compareTo(soldeAvant) > 0) {
                montantPeriode = soldeAvant;
            }
            BigDecimal montantInteret = computeInteret(pret, soldeAvant);
            BigDecimal montantTotal = montantPeriode.add(montantInteret);
            BigDecimal soldeApres = soldeAvant.subtract(montantPeriode);
            if (soldeApres.compareTo(BigDecimal.ZERO) < 0) {
                soldeApres = BigDecimal.ZERO;
            }

            PretRemboursement remboursement = new PretRemboursement();
            remboursement.setPretEmploye(pret);
            remboursement.setDateRemboursement(payroll.getDateFin());
            remboursement.setMontantRembourse(montantPeriode);
            remboursement.setMontantInteret(montantInteret);
            remboursement.setMontantTotal(montantTotal);
            remboursement.setOrigine(PretRemboursement.OrigineRemboursement.PAIE);
            remboursement.setNoPayroll(payroll.getId().intValue());
            remboursement.setStatut(PretRemboursement.StatutRemboursement.BROUILLON);
            remboursement.setCreatedBy(username);
            remboursement.setCreatedOn(OffsetDateTime.now());
            remboursement.setRowscn(1);
            pretRemboursementRepository.save(remboursement);

            PayrollRecouvrement recouvrement = new PayrollRecouvrement();
            recouvrement.setPayroll(payroll);
            recouvrement.setPayrollEmploye(payrollEmploye);
            recouvrement.setLibelle(pret.getLibelle() != null && !pret.getLibelle().isBlank() ? pret.getLibelle() : "EMPRUNT");
            recouvrement.setTypeRecouvrement(isYes(pret.getAvance())
                    ? PayrollRecouvrement.TypeRecouvrement.AVANCE
                    : PayrollRecouvrement.TypeRecouvrement.PRET);
            recouvrement.setReferenceNo(pret.getId() != null ? pret.getId().toString() : null);
            recouvrement.setMontantPeriode(montantPeriode);
            recouvrement.setMontantInteret(montantInteret);
            recouvrement.setMontantTotal(montantTotal);
            recouvrement.setSoldeAvant(soldeAvant);
            recouvrement.setSoldeApres(soldeApres);
            recouvrement.setCreatedBy(username);
            recouvrement.setCreatedOn(OffsetDateTime.now());
            recouvrement.setRowscn(1);
            payrollRecouvrementRepository.save(recouvrement);

            total = total.add(montantTotal);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean shouldPreleverPret(PretEmploye pret, LocalDate dateDebut, LocalDate dateFin) {
        if (pret == null) {
            return false;
        }
        boolean hasFirstDeduction = pret.getMontantVerse() != null && pret.getMontantVerse().compareTo(BigDecimal.ZERO) > 0;
        LocalDate premierPrelevement = pret.getPremierPrelevement();
        if (!hasFirstDeduction) {
            if (premierPrelevement == null) {
                return true;
            }
            if (dateDebut != null && dateFin != null) {
                boolean inRange = (premierPrelevement.isEqual(dateDebut) || premierPrelevement.isAfter(dateDebut))
                        && (premierPrelevement.isEqual(dateFin) || premierPrelevement.isBefore(dateFin));
                return inRange;
            }
        }
        int frequence = pret.getFrequenceNbPeriodicites() != null ? pret.getFrequenceNbPeriodicites() : 1;
        if (frequence <= 1) {
            return true;
        }
        int compteur = pret.getFrequenceCompteur() != null ? pret.getFrequenceCompteur() : 0;
        return (compteur + 1) % frequence == 0;
    }

    private void updatePretFrequenceCompteur(PretEmploye pret, boolean deducted) {
        if (pret == null) {
            return;
        }
        int frequence = pret.getFrequenceNbPeriodicites() != null ? pret.getFrequenceNbPeriodicites() : 1;
        int compteur = pret.getFrequenceCompteur() != null ? pret.getFrequenceCompteur() : 0;
        if (frequence <= 1) {
            pret.setFrequenceCompteur(0);
            return;
        }
        if (deducted) {
            pret.setFrequenceCompteur(0);
        } else {
            int next = compteur + 1;
            pret.setFrequenceCompteur(Math.min(next, frequence - 1));
        }
    }

    private void computeStats(EmployeSalaire salaire, RegimePaie regimePaie, Payroll payroll,
                              PayrollEmploye payrollEmploye, String username) {
        Employe employe = salaire.getEmploye();
        Map<Long, SupplementaireEmploye> supplementaireById = new LinkedHashMap<>();
        List<SupplementaireEmploye> withPayrollId = supplementaireEmployeRepository
                .findValidesForPayrollWithPayrollId(
                        employe.getId(),
                        payroll.getDateDebut(),
                        payroll.getDateFin(),
                        payroll.getId().intValue());
        for (SupplementaireEmploye sup : withPayrollId) {
            if (sup.getId() != null) {
                supplementaireById.put(sup.getId(), sup);
            }
        }
        List<SupplementaireEmploye> withoutPayrollId = supplementaireEmployeRepository
                .findValidesForPayroll(employe.getId(), payroll.getDateDebut(), payroll.getDateFin());
        for (SupplementaireEmploye sup : withoutPayrollId) {
            if (sup.getId() != null) {
                supplementaireById.putIfAbsent(sup.getId(), sup);
            }
        }
        List<SupplementaireEmploye> supplementaires = new ArrayList<>(supplementaireById.values());
        Map<String, StatAccumulator> stats = new LinkedHashMap<>();

        for (SupplementaireEmploye sup : supplementaires) {
            Map<String, Object> details = parseDetails(sup.getDetails());
            addStat(stats, "FERIE", "Férié", PayrollEmployeStats.UniteMesure.JOUR,
                    asBigDecimal(details.get("nb_feries")), asBigDecimal(details.get("montant_ferie_calcule")));
            addStat(stats, "NUIT", "Nuit", PayrollEmployeStats.UniteMesure.JOUR,
                    asBigDecimal(details.get("nb_nuits")), asBigDecimal(details.get("montant_nuit_calcule")));
            addStat(stats, "OFF", "Off", PayrollEmployeStats.UniteMesure.JOUR,
                    asBigDecimal(details.get("nb_offs")), asBigDecimal(details.get("montant_off_calcule")));
            addStat(stats, "CONGE", "Congé", PayrollEmployeStats.UniteMesure.JOUR,
                    asBigDecimal(details.get("nb_conges")), asBigDecimal(details.get("montant_conge_calcule")));
            boolean hasSpecialCounts = details.containsKey("nb_feries")
                    || details.containsKey("nb_nuits")
                    || details.containsKey("nb_offs")
                    || details.containsKey("nb_conges");
            if (!hasSpecialCounts) {
                addStat(stats, "NB_HEURES", "Nb heures", PayrollEmployeStats.UniteMesure.HEURE,
                        asBigDecimal(details.get("nb_heures")), asBigDecimal(details.get("montant_heure_calcule")));
            }
        }

        for (StatAccumulator acc : stats.values()) {
            if (acc.quantite.compareTo(BigDecimal.ZERO) <= 0 && acc.montant.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            PayrollEmployeStats stat = new PayrollEmployeStats();
            stat.setPayroll(payroll);
            stat.setPayrollEmploye(payrollEmploye);
            stat.setMetricCode(acc.code);
            stat.setMetricLabel(acc.label);
            stat.setMetricGroup(resolveStatGroup(acc.code));
            stat.setUniteMesure(acc.uniteMesure);
            stat.setQuantite(acc.quantite);
            stat.setMontant(acc.montant);
            stat.setCreatedBy(username);
            stat.setCreatedOn(OffsetDateTime.now());
            stat.setRowscn(1);
            payrollEmployeStatsRepository.save(stat);
        }
    }

    private PayrollEmployeStats.MetricGroup resolveStatGroup(String code) {
        return switch (code) {
            case "FERIE" -> PayrollEmployeStats.MetricGroup.FERIE_SUP;
            case "OFF" -> PayrollEmployeStats.MetricGroup.OFF_SUP;
            case "CONGE" -> PayrollEmployeStats.MetricGroup.CONGE_SUP;
            case "NB_HEURES" -> PayrollEmployeStats.MetricGroup.HEURE_SUP;
            default -> PayrollEmployeStats.MetricGroup.AUTRE;
        };
    }

    private void addStat(Map<String, StatAccumulator> stats,
                         String code,
                         String label,
                         PayrollEmployeStats.UniteMesure uniteMesure,
                         BigDecimal quantite,
                         BigDecimal montant) {
        if (quantite.compareTo(BigDecimal.ZERO) <= 0 && montant.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        String key = code + "|" + uniteMesure.name();
        StatAccumulator acc = stats.get(key);
        if (acc == null) {
            acc = new StatAccumulator(code, label, uniteMesure);
            stats.put(key, acc);
        }
        acc.quantite = acc.quantite.add(quantite);
        acc.montant = acc.montant.add(montant);
    }

    private static class StatAccumulator {
        private final String code;
        private final String label;
        private final PayrollEmployeStats.UniteMesure uniteMesure;
        private BigDecimal quantite = BigDecimal.ZERO;
        private BigDecimal montant = BigDecimal.ZERO;

        private StatAccumulator(String code, String label, PayrollEmployeStats.UniteMesure uniteMesure) {
            this.code = code;
            this.label = label;
            this.uniteMesure = uniteMesure;
        }
    }

    private Map<String, Object> parseDetails(String details) {
        if (details == null || details.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(details, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private DeductionSplit computeDeductionSplit(DefinitionDeduction.TypeDeduction typeDeduction,
                                                 BigDecimal valeur,
                                                 BigDecimal valeurCouvert,
                                                 BigDecimal baseMontant) {
        BigDecimal montantTotal = computeFromType(typeDeduction, valeur, baseMontant);
        BigDecimal montantCouvert = computeFromType(typeDeduction, valeurCouvert, baseMontant);
        BigDecimal montantEmploye = montantTotal.subtract(montantCouvert);
        if (montantEmploye.compareTo(BigDecimal.ZERO) < 0) {
            montantEmploye = BigDecimal.ZERO;
        }
        return new DeductionSplit(montantEmploye.setScale(2, RoundingMode.HALF_UP),
                montantCouvert.setScale(2, RoundingMode.HALF_UP));
    }

    private DeductionSplit computeDeductionSplit(DefinitionDeduction deduction, BigDecimal baseMontant) {
        List<TrancheBaremeDeduction> tranches = trancheBaremeDeductionRepository
                .findByDefinitionDeductionIdOrderByBorneInfAsc(deduction.getId());
        if (!tranches.isEmpty()) {
            BigDecimal total = computeDeductionFromTranches(baseMontant, tranches);
            return new DeductionSplit(total.setScale(2, RoundingMode.HALF_UP), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        return computeDeductionSplit(deduction.getTypeDeduction(), deduction.getValeur(),
                deduction.getValeurCouvert(), baseMontant);
    }

    private DeductionSplit computePlanAssuranceSplit(PlanAssurance plan, BigDecimal baseMontant) {
        DefinitionDeduction.TypeDeduction type = plan.getTypePrelevement() == PlanAssurance.TypePrelevement.POURCENTAGE
                ? DefinitionDeduction.TypeDeduction.POURCENTAGE
                : DefinitionDeduction.TypeDeduction.PLAT;
        return computeDeductionSplit(type, plan.getValeur(), plan.getValeurCouverte(), baseMontant);
    }

    private BigDecimal computeFromType(DefinitionDeduction.TypeDeduction type, BigDecimal valeur, BigDecimal baseMontant) {
        if (type == DefinitionDeduction.TypeDeduction.POURCENTAGE) {
            return baseMontant.multiply(valeur).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        }
        return valeur != null ? valeur : BigDecimal.ZERO;
    }

    private BigDecimal computeDeductionFromTranches(BigDecimal baseMontant, List<TrancheBaremeDeduction> tranches) {
        if (baseMontant == null || baseMontant.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (TrancheBaremeDeduction tranche : tranches) {
            BigDecimal borneInf = tranche.getBorneInf() != null ? tranche.getBorneInf() : BigDecimal.ZERO;
            if (baseMontant.compareTo(borneInf) <= 0) {
                continue;
            }
            BigDecimal upper = tranche.getBorneSup() != null ? tranche.getBorneSup() : baseMontant;
            BigDecimal trancheMax = baseMontant.min(upper);
            BigDecimal portion = trancheMax.subtract(borneInf);
            if (portion.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            total = total.add(computeFromType(tranche.getTypeDeduction(), tranche.getValeur(), portion));
        }
        return total;
    }

    private BigDecimal applyAbattement(BigDecimal baseMontant, BigDecimal pctHorsCalcul) {
        if (baseMontant == null) {
            return BigDecimal.ZERO;
        }
        if (pctHorsCalcul == null || pctHorsCalcul.compareTo(BigDecimal.ZERO) <= 0) {
            return baseMontant;
        }
        BigDecimal abattement = baseMontant.multiply(pctHorsCalcul)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return baseMontant.subtract(abattement);
    }

    private BigDecimal applyMinMax(BigDecimal baseMontant, BigDecimal min, BigDecimal max) {
        if (baseMontant == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = baseMontant;
        if (min != null && min.compareTo(BigDecimal.ZERO) > 0 && value.compareTo(min) < 0) {
            value = min;
        }
        if (max != null && max.compareTo(BigDecimal.ZERO) > 0 && value.compareTo(max) > 0) {
            value = max;
        }
        return value;
    }

    private DeductionSplit applyArrondir(DeductionSplit split, DefinitionDeduction.Arrondir arrondir) {
        if (arrondir == null) {
            return split;
        }
        int scale = switch (arrondir) {
            case UNITE -> 0;
            case DIXIEME -> 1;
            case CENTIEME -> 2;
            case MILLIEME -> 3;
        };
        return new DeductionSplit(
                split.montantEmploye.setScale(scale, RoundingMode.HALF_UP),
                split.montantCouvert.setScale(scale, RoundingMode.HALF_UP)
        );
    }

    private boolean shouldSkipForProbation(DefinitionDeduction deduction, EmploiEmploye emploi) {
        if (deduction == null || emploi == null) {
            return false;
        }
        return "N".equalsIgnoreCase(deduction.getProbatoire()) && "Y".equalsIgnoreCase(emploi.getEnProbation());
    }

    private boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    private BigDecimal toAnnualAmount(BigDecimal montant, RegimePaie.Periodicite periodicite) {
        if (montant == null) {
            return BigDecimal.ZERO;
        }
        int factor = annualFactor(periodicite);
        return montant.multiply(BigDecimal.valueOf(factor));
    }

    private BigDecimal fromAnnualAmount(BigDecimal montantAnnuel, RegimePaie.Periodicite periodicite) {
        if (montantAnnuel == null) {
            return BigDecimal.ZERO;
        }
        int factor = annualFactor(periodicite);
        if (factor <= 0) {
            return montantAnnuel;
        }
        return montantAnnuel.divide(BigDecimal.valueOf(factor), 6, RoundingMode.HALF_UP);
    }

    private int annualFactor(RegimePaie.Periodicite periodicite) {
        if (periodicite == null) {
            return 1;
        }
        return switch (periodicite) {
            case JOURNALIER -> 365;
            case HEBDO -> 52;
            case QUINZAINE -> 26;
            case QUINZOMADAIRE -> 24;
            case MENSUEL -> 12;
            case TRIMESTRIEL -> 4;
            case SEMESTRIEL -> 2;
            case ANNUEL -> 1;
        };
    }

    private BigDecimal computeInteret(PretEmploye pret, BigDecimal soldeAvant) {
        if (pret == null || pret.getTypeInteret() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal taux = pret.getTauxInteret() != null ? pret.getTauxInteret() : BigDecimal.ZERO;
        if (pret.getTypeInteret() == PretEmploye.TypeInteret.POURCENTAGE) {
            return soldeAvant.multiply(taux).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        }
        return taux;
    }

    private BigDecimal computeDailyRate(BigDecimal montant, RegimePaie regimePaie, BigDecimal heuresRef) {
        if (montant == null) {
            return BigDecimal.ZERO;
        }
        if (regimePaie.getModeRemuneration() == RegimePaie.ModeRemuneration.HORAIRE) {
            return montant.multiply(heuresRef);
        }
        if (regimePaie.getModeRemuneration() == RegimePaie.ModeRemuneration.JOURNALIER
                || regimePaie.getModeRemuneration() == RegimePaie.ModeRemuneration.PIECE
                || regimePaie.getModeRemuneration() == RegimePaie.ModeRemuneration.PIECE_FIXE) {
            return montant;
        }
        if (regimePaie.getModeRemuneration() == RegimePaie.ModeRemuneration.SALAIRE) {
            return montant.divide(BigDecimal.valueOf(resolvePeriodDays(regimePaie.getPeriodicite())), 6, RoundingMode.HALF_UP);
        }
        return montant;
    }

    private int resolvePeriodDays(RegimePaie.Periodicite periodicite) {
        return switch (periodicite) {
            case JOURNALIER -> 1;
            case HEBDO -> 7;
            case QUINZAINE -> 14;
            case QUINZOMADAIRE -> 15;
            case MENSUEL -> 30;
            case TRIMESTRIEL -> 90;
            case SEMESTRIEL -> 180;
            case ANNUEL -> 365;
        };
    }

    private Map<String, Object> buildAutreRevenuVariables(EmployeSalaire salaire, Payroll payroll) {
        Map<String, Object> vars = new HashMap<>();
        if (salaire == null || salaire.getEmploye() == null) {
            return vars;
        }
        LocalDate debut = payroll.getDateDebut();
        LocalDate fin = payroll.getDateFin();
        List<PresenceEmploye> presences = presenceEmployeRepository.findValidesForPayroll(
                salaire.getEmploye().getId(), debut, fin);

        long nPaid = ChronoUnit.DAYS.between(debut, fin) + 1;
        long nPres = presences.stream().map(PresenceEmploye::getDateJour).distinct().count();
        long nPresDay = presences.stream().filter(p -> p.getDateDepart() == null || p.getDateDepart().isEqual(p.getDateJour())).count();
        long nPresNight = presences.stream().filter(p -> p.getDateDepart() != null && p.getDateDepart().isAfter(p.getDateJour())).count();
        long nOff = countOffDays(salaire.getEmploi(), debut, fin);
        long nFerie = countFerieDays(debut, fin);
        BigDecimal hOt = sumSupplementaireHours(salaire.getEmploye().getId(), debut, fin);
        BigDecimal coefBonW = resolveBoniCoefficient(salaire);

        vars.put("h.work.pp", nOff);
        vars.put("n.off.pp", nOff + nFerie);
        vars.put("n.paid.pp", nPaid);
        vars.put("n.pres.pp", nPres);
        vars.put("n.pres.day", nPresDay);
        vars.put("n.pres.night", nPresNight);
        vars.put("h.ot.pp", hOt);
        vars.put("coef.bon.w", coefBonW);
        return vars;
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
            int dayValue = dow.getValue();
            if (offDays.contains(dayValue)) {
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
                Map<String, Object> details = objectMapper.readValue(supplementaire.getDetails(), new TypeReference<Map<String, Object>>() {});
                Object nbHeures = details.get("nb_heures");
                if (nbHeures instanceof Number) {
                    total = total.add(BigDecimal.valueOf(((Number) nbHeures).doubleValue()));
                } else if (nbHeures instanceof String) {
                    try {
                        total = total.add(new BigDecimal((String) nbHeures));
                    } catch (NumberFormatException ignored) {
                        // ignore invalid value
                    }
                }
            } catch (Exception ignored) {
                // ignore invalid JSON
            }
        }
        return total;
    }

    private BigDecimal resolveBoniCoefficient(EmployeSalaire salaire) {
        if (salaire == null || salaire.getEmploi() == null || salaire.getEmploi().getTypeEmploye() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal coef = salaire.getEmploi().getTypeEmploye().getPourcentageJourBonus();
        return coef != null ? coef : BigDecimal.ZERO;
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

    private PayrollGain buildGain(Payroll payroll, PayrollEmploye payrollEmploye, RubriquePaie rubriquePaie,
                                  BigDecimal montant, String source, String reference, String username) {
        PayrollGain gain = new PayrollGain();
        gain.setPayroll(payroll);
        gain.setPayrollEmploye(payrollEmploye);
        gain.setRubriquePaie(rubriquePaie);
        gain.setMontant(montant != null ? montant : BigDecimal.ZERO);
        if (rubriquePaie != null) {
            gain.setImposable(rubriquePaie.getImposable());
            gain.setSoumisCotisations(rubriquePaie.getSoumisCotisations());
        } else {
            gain.setImposable("Y");
            gain.setSoumisCotisations("Y");
        }
        gain.setSource(PayrollGain.SourceGain.valueOf(source));
        gain.setReferenceExterne(reference);
        gain.setCreatedBy(username);
        gain.setCreatedOn(OffsetDateTime.now());
        gain.setRowscn(1);
        return gain;
    }

    private void clearExternalReferences(Long payrollId, String username) {
        supplementaireEmployeRepository.clearPayrollNo(payrollId.intValue());
        autreRevenuEmployeRepository.clearPayrollNo(payrollId.intValue());
        pretRemboursementRepository.deleteByNoPayroll(payrollId.intValue());
        absenceEmployeRepository.clearPayrollId(payrollId);
        productionPieceRepository.clearPayrollId(payrollId, username, OffsetDateTime.now());
    }

    private PayrollDTO toDTO(Payroll payroll) {
        PayrollDTO dto = new PayrollDTO();
        dto.setId(payroll.getId());
        dto.setRegimePaieId(payroll.getRegimePaie().getId());
        dto.setRegimePaieCode(payroll.getRegimePaie().getCodeRegimePaie());
        dto.setRegimePaieLibelle(payroll.getRegimePaie().getDescription());
        dto.setLibelle(payroll.getLibelle());
        dto.setDateDebut(payroll.getDateDebut());
        dto.setDateFin(payroll.getDateFin());
        dto.setStatut(payroll.getStatut().name());
        dto.setCreatedBy(payroll.getCreatedBy());
        dto.setCreatedOn(payroll.getCreatedOn());
        dto.setUpdatedBy(payroll.getUpdatedBy());
        dto.setUpdatedOn(payroll.getUpdatedOn());
        dto.setRowscn(payroll.getRowscn());
        return dto;
    }

    private PayrollEmployeDTO toEmployeDTO(PayrollEmploye entity) {
        PayrollEmployeDTO dto = new PayrollEmployeDTO();
        dto.setId(entity.getId());
        dto.setPayrollId(entity.getPayroll().getId());
        dto.setEmployeId(entity.getEmploye().getId());
        dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
        dto.setEmployeNom(entity.getEmploye().getNom());
        dto.setEmployePrenom(entity.getEmploye().getPrenom());
        dto.setMontantSalaireBase(entity.getMontantSalaireBase());
        dto.setMontantSupplementaire(entity.getMontantSupplementaire());
        dto.setMontantAutreRevenu(entity.getMontantAutreRevenu());
        dto.setMontantBrut(entity.getMontantBrut());
        dto.setMontantDeductions(entity.getMontantDeductions());
        dto.setMontantRecouvrements(entity.getMontantRecouvrements());
        dto.setMontantSanctions(entity.getMontantSanctions());
        dto.setMontantNetAPayer(entity.getMontantNetAPayer());
        dto.setModePaiement(entity.getModePaiement().name());
        dto.setNoCheque(entity.getNoCheque());
        dto.setLibelleBanque(entity.getLibelleBanque());
        dto.setNoCompte(entity.getNoCompte());
        dto.setTypeCompte(entity.getTypeCompte());
        dto.setEmailEnvoye(entity.getEmailEnvoye());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private PayrollGainDTO toGainDTO(PayrollGain entity) {
        PayrollGainDTO dto = new PayrollGainDTO();
        dto.setId(entity.getId());
        dto.setPayrollId(entity.getPayroll().getId());
        dto.setPayrollEmployeId(entity.getPayrollEmploye().getId());
        if (entity.getRubriquePaie() != null) {
            dto.setRubriquePaieId(entity.getRubriquePaie().getId());
            dto.setRubriquePaieCode(entity.getRubriquePaie().getCodeRubrique());
            dto.setRubriquePaieLibelle(entity.getRubriquePaie().getLibelle());
        }
        dto.setMontant(entity.getMontant());
        dto.setImposable(entity.getImposable());
        dto.setSoumisCotisations(entity.getSoumisCotisations());
        dto.setSource(entity.getSource().name());
        dto.setReferenceExterne(entity.getReferenceExterne());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private RubriquePaie getRubriquePaieByCode(String codeRubrique) {
        return rubriquePaieRepository.findByCodeRubrique(codeRubrique)
                .orElseThrow(() -> new RuntimeException("RubriquePaie not found for code_rubrique: " + codeRubrique));
    }

    private PayrollDeductionDTO toDeductionDTO(PayrollDeduction entity) {
        PayrollDeductionDTO dto = new PayrollDeductionDTO();
        dto.setId(entity.getId());
        dto.setPayrollId(entity.getPayroll().getId());
        dto.setPayrollEmployeId(entity.getPayrollEmploye().getId());
        dto.setCodeDeduction(entity.getCodeDeduction());
        dto.setLibelle(entity.getLibelle());
        dto.setCategorie(entity.getCategorie().name());
        dto.setBaseMontant(entity.getBaseMontant());
        dto.setTaux(entity.getTaux());
        dto.setMontant(entity.getMontant());
        dto.setMontantCouvert(entity.getMontantCouvert());
        dto.setReferenceExterne(entity.getReferenceExterne());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private PayrollRecouvrementDTO toRecouvrementDTO(PayrollRecouvrement entity) {
        PayrollRecouvrementDTO dto = new PayrollRecouvrementDTO();
        dto.setId(entity.getId());
        dto.setPayrollId(entity.getPayroll().getId());
        dto.setPayrollEmployeId(entity.getPayrollEmploye().getId());
        dto.setLibelle(entity.getLibelle());
        dto.setTypeRecouvrement(entity.getTypeRecouvrement().name());
        dto.setReferenceNo(entity.getReferenceNo());
        dto.setMontantPeriode(entity.getMontantPeriode());
        dto.setMontantInteret(entity.getMontantInteret());
        dto.setMontantTotal(entity.getMontantTotal());
        dto.setSoldeAvant(entity.getSoldeAvant());
        dto.setSoldeApres(entity.getSoldeApres());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private static class DeductionSplit {
        private final BigDecimal montantEmploye;
        private final BigDecimal montantCouvert;

        private DeductionSplit(BigDecimal montantEmploye, BigDecimal montantCouvert) {
            this.montantEmploye = montantEmploye != null ? montantEmploye : BigDecimal.ZERO;
            this.montantCouvert = montantCouvert != null ? montantCouvert : BigDecimal.ZERO;
        }
    }

    private PayrollSanctionDTO toSanctionDTO(PayrollSanction entity) {
        PayrollSanctionDTO dto = new PayrollSanctionDTO();
        dto.setId(entity.getId());
        dto.setPayrollId(entity.getPayroll().getId());
        dto.setPayrollEmployeId(entity.getPayrollEmploye().getId());
        dto.setTypeSanction(entity.getTypeSanction().name());
        dto.setDateJour(entity.getDateJour());
        dto.setQuantiteMinute(entity.getQuantiteMinute());
        dto.setMontant(entity.getMontant());
        dto.setReferenceExterne(entity.getReferenceExterne());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private PayrollEmployeStatsDTO toStatsDTO(PayrollEmployeStats entity) {
        PayrollEmployeStatsDTO dto = new PayrollEmployeStatsDTO();
        dto.setId(entity.getId());
        dto.setPayrollId(entity.getPayroll().getId());
        dto.setPayrollEmployeId(entity.getPayrollEmploye().getId());
        dto.setMetricCode(entity.getMetricCode());
        dto.setMetricLabel(entity.getMetricLabel());
        dto.setMetricGroup(entity.getMetricGroup().name());
        dto.setUniteMesure(entity.getUniteMesure().name());
        dto.setQuantite(entity.getQuantite());
        dto.setMontant(entity.getMontant());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private Payroll.StatutPayroll parseStatut(String statut) {
        if (statut == null || statut.isBlank()) {
            return null;
        }
        try {
            return Payroll.StatutPayroll.valueOf(statut);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private LocalDate resolveDateDebut(RegimePaie regimePaie) {
        if (regimePaie.getDernierePaie() != null) {
            return regimePaie.getDernierePaie().plusDays(1);
        }
        return LocalDate.now();
    }

    private LocalDate resolveDateFin(RegimePaie regimePaie, LocalDate defaultDate) {
        if (regimePaie.getProchainePaie() != null) {
            return regimePaie.getProchainePaie();
        }
        return defaultDate;
    }
}
