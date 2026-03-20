package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollEmployeBoniDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollBoniDeductionDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BoniGenerationService {
    private final PayrollEmployeAgregatRepository payrollEmployeAgregatRepository;
    private final PayrollEmployeBoniRepository payrollEmployeBoniRepository;
    private final PayrollBoniDeductionRepository payrollBoniDeductionRepository;
    private final RubriquePaieRepository rubriquePaieRepository;
    private final TypeRevenuRepository typeRevenuRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final RubriquePaieDeductionRepository rubriquePaieDeductionRepository;
    private final TrancheBaremeDeductionRepository trancheBaremeDeductionRepository;
    private final FormulaExpressionEvaluator formulaEvaluator = new FormulaExpressionEvaluator();

    @Transactional
    public Map<String, Object> generate(Long periodeBoniId,
                                        Long rubriquePaieId,
                                        Long entrepriseId,
                                        Long regimePaieId,
                                        String username) {
        return generate(periodeBoniId, rubriquePaieId, entrepriseId, regimePaieId, null, username);
    }

    @Transactional
    public Map<String, Object> generate(Long periodeBoniId,
                                        Long rubriquePaieId,
                                        Long entrepriseId,
                                        Long regimePaieId,
                                        Long employeId,
                                        String username) {
        clearCalculatedForFilters(periodeBoniId, rubriquePaieId, entrepriseId, regimePaieId, employeId);
        RubriquePaie rubrique = rubriquePaieRepository.findById(rubriquePaieId)
                .orElseThrow(() -> new RuntimeException("RubriquePaie not found with id: " + rubriquePaieId));
        TypeRevenu typeRevenu = resolveTypeRevenu(entrepriseId, rubriquePaieId);
        String formule = resolveFormule(typeRevenu);
        List<PayrollEmployeAgregat> agregats = payrollEmployeAgregatRepository
                .findForBoniGeneration(periodeBoniId, regimePaieId, entrepriseId);

        int totalEmployes = agregats.size();
        int eligibleEmployes = 0;
        int createdRows = 0;
        int skippedMissingConfig = 0;
        int skippedValidated = 0;

        for (PayrollEmployeAgregat agregat : agregats) {
            if (agregat == null || agregat.getEmploye() == null || agregat.getRegimePaie() == null || agregat.getPeriodeBoni() == null) {
                skippedMissingConfig++;
                continue;
            }
            if (employeId != null && !Objects.equals(agregat.getEmploye().getId(), employeId)) {
                continue;
            }
            int baseCalculBoni = resolveBaseCalculBoni(agregat.getEmploye().getId(), agregat.getPeriodeBoni().getDateFin());
            if (baseCalculBoni <= 0) {
                skippedMissingConfig++;
                continue;
            }
            eligibleEmployes++;
            PayrollEmployeBoni boni = payrollEmployeBoniRepository
                    .findByRegimePaieIdAndPeriodeBoniIdAndEmployeIdAndRubriquePaieId(
                            agregat.getRegimePaie().getId(),
                            agregat.getPeriodeBoni().getId(),
                            agregat.getEmploye().getId(),
                            rubriquePaieId
                    )
                    .orElseGet(PayrollEmployeBoni::new);

            if (boni.getId() != null && boni.getStatut() == PayrollEmployeBoni.StatutBoni.VALIDE) {
                skippedValidated++;
                continue;
            }

            BigDecimal montantReference = computeMontantReference(agregat);
            BigDecimal diviseur = BigDecimal.valueOf(baseCalculBoni).setScale(4, RoundingMode.HALF_UP);
            BigDecimal montantBoniBrut = computeMontantBoniBrut(formule, agregat, montantReference, diviseur, baseCalculBoni);
            BigDecimal montantDeductions = BigDecimal.ZERO;

            boni.setStatut(PayrollEmployeBoni.StatutBoni.CALCULE);
            boni.setRubriquePaie(rubrique);
            boni.setRegimePaie(agregat.getRegimePaie());
            boni.setPeriodeBoni(agregat.getPeriodeBoni());
            boni.setEmploye(agregat.getEmploye());
            boni.setMontantReference(montantReference);
            boni.setDiviseur(diviseur);
            boni.setMontantBoniBrut(montantBoniBrut);
            boni.setFormule(formule);
            boni.setMontantDeductions(BigDecimal.ZERO);
            boni.setMontantBoniNet(montantBoniBrut);
            boni.setUpdatedBy(username);
            boni.setUpdatedOn(OffsetDateTime.now());
            if (boni.getId() == null) {
                boni.setCreatedBy(username);
                boni.setCreatedOn(OffsetDateTime.now());
                boni.setRowscn(1);
            }
            boni = payrollEmployeBoniRepository.save(boni);
            payrollBoniDeductionRepository.deleteByPayrollBoniId(boni.getId());

            List<RubriquePaieDeduction> rubriqueDeductions = rubriquePaieDeductionRepository.findByRubriquePaieId(rubriquePaieId);
            for (RubriquePaieDeduction link : rubriqueDeductions) {
                if (link == null || link.getDefinitionDeduction() == null) {
                    continue;
                }
                DefinitionDeduction dd = link.getDefinitionDeduction();
                if (!"Y".equalsIgnoreCase(dd.getSpecialise())) {
                    continue;
                }
                BigDecimal baseMontant = applyAbattement(montantBoniBrut, dd.getPctHorsCalcul());
                baseMontant = applyMinMax(baseMontant, dd.getMinPrelevement(), dd.getMaxPrelevement());
                DeductionAmounts amounts = computeDeductionAmounts(dd, baseMontant);
                amounts = applyArrondir(amounts, dd.getArrondir());
                if (amounts.montant().compareTo(BigDecimal.ZERO) <= 0 && amounts.montantCouvert().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                PayrollBoniDeduction pbd = new PayrollBoniDeduction();
                pbd.setPayrollBoni(boni);
                pbd.setEmploye(agregat.getEmploye());
                pbd.setCodeDeduction(dd.getCodeDeduction());
                pbd.setLibelle(dd.getLibelle());
                pbd.setBaseMontant(baseMontant.setScale(2, RoundingMode.HALF_UP));
                pbd.setTaux(dd.getTypeDeduction() == DefinitionDeduction.TypeDeduction.POURCENTAGE
                        ? safe(dd.getValeur()).setScale(4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
                pbd.setMontant(amounts.montant().setScale(2, RoundingMode.HALF_UP));
                pbd.setMontantCouvert(amounts.montantCouvert().setScale(2, RoundingMode.HALF_UP));
                pbd.setCreatedBy(username);
                pbd.setCreatedOn(OffsetDateTime.now());
                pbd.setUpdatedBy(username);
                pbd.setUpdatedOn(OffsetDateTime.now());
                pbd.setRowscn(1);
                payrollBoniDeductionRepository.save(pbd);
                montantDeductions = montantDeductions.add(amounts.montant());
            }

            BigDecimal montantNet = montantBoniBrut.subtract(montantDeductions);
            if (montantNet.compareTo(BigDecimal.ZERO) < 0) {
                montantNet = BigDecimal.ZERO;
            }
            boni.setMontantDeductions(montantDeductions.setScale(2, RoundingMode.HALF_UP));
            boni.setMontantBoniNet(montantNet.setScale(2, RoundingMode.HALF_UP));
            boni.setUpdatedBy(username);
            boni.setUpdatedOn(OffsetDateTime.now());
            payrollEmployeBoniRepository.save(boni);
            createdRows++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalEmployes", totalEmployes);
        result.put("eligibleEmployes", eligibleEmployes);
        result.put("createdRows", createdRows);
        result.put("skippedExisting", 0);
        result.put("skippedMissingConfig", skippedMissingConfig + skippedValidated);
        result.put("message", "Boni generated for " + createdRows + " employes");
        return result;
    }

    private void clearCalculatedForFilters(Long periodeBoniId,
                                           Long rubriquePaieId,
                                           Long entrepriseId,
                                           Long regimePaieId,
                                           Long employeId) {
        List<Long> idsToDelete = payrollEmployeBoniRepository.findByFilters(
                periodeBoniId,
                rubriquePaieId,
                regimePaieId,
                entrepriseId,
                employeId
        ).stream()
                .filter(b -> b.getStatut() == PayrollEmployeBoni.StatutBoni.CALCULE)
                .map(PayrollEmployeBoni::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!idsToDelete.isEmpty()) {
            payrollBoniDeductionRepository.deleteByPayrollBoniIds(idsToDelete);
            payrollEmployeBoniRepository.deleteAllByIdInBatch(idsToDelete);
        }
    }

    @Transactional
    public Map<String, Object> validateCalculated(Long periodeBoniId,
                                                  Long rubriquePaieId,
                                                  Long entrepriseId,
                                                  Long regimePaieId,
                                                  String username) {
        int updatedRows = payrollEmployeBoniRepository.validateCalculated(
                periodeBoniId,
                rubriquePaieId,
                regimePaieId,
                entrepriseId,
                username,
                OffsetDateTime.now()
        );
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("updatedRows", updatedRows);
        result.put("message", "Boni validated for " + updatedRows + " employes");
        return result;
    }

    @Transactional(readOnly = true)
    public List<PayrollEmployeBoniDTO> findBonis(Long periodeBoniId,
                                                 Long rubriquePaieId,
                                                 Long entrepriseId,
                                                 Long regimePaieId,
                                                 Long employeId) {
        if (periodeBoniId == null || rubriquePaieId == null) {
            throw new RuntimeException("periodeBoniId and rubriquePaieId are required");
        }
        return payrollEmployeBoniRepository.findByFilters(
                periodeBoniId,
                rubriquePaieId,
                regimePaieId,
                entrepriseId,
                employeId
        ).stream().map(this::toDTO).toList();
    }

    @Transactional
    public Map<String, Object> deleteCalculated(Long periodeBoniId,
                                                Long rubriquePaieId,
                                                Long entrepriseId,
                                                Long regimePaieId,
                                                Long employeId) {
        List<Long> idsToDelete = payrollEmployeBoniRepository.findByFilters(
                periodeBoniId,
                rubriquePaieId,
                regimePaieId,
                entrepriseId,
                employeId
        ).stream()
                .filter(b -> b.getStatut() == PayrollEmployeBoni.StatutBoni.CALCULE)
                .map(PayrollEmployeBoni::getId)
                .filter(Objects::nonNull)
                .toList();

        if (!idsToDelete.isEmpty()) {
            payrollBoniDeductionRepository.deleteByPayrollBoniIds(idsToDelete);
            payrollEmployeBoniRepository.deleteAllByIdInBatch(idsToDelete);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("deletedRows", idsToDelete.size());
        result.put("message", "Boni deleted: " + idsToDelete.size());
        return result;
    }

    @Transactional(readOnly = true)
    public List<PayrollBoniDeductionDTO> findBoniDeductions(Long payrollBoniId) {
        if (payrollBoniId == null) {
            throw new RuntimeException("payrollBoniId is required");
        }
        return payrollBoniDeductionRepository.findByPayrollBoniIdOrderByCodeDeductionAsc(payrollBoniId)
                .stream()
                .map(this::toDeductionDTO)
                .toList();
    }

    private int resolveBaseCalculBoni(Long employeId, LocalDate dateFin) {
        if (employeId == null || dateFin == null) {
            return 12;
        }
        List<EmploiEmploye> emplois = emploiEmployeRepository.findActiveForDate(employeId, dateFin);
        if (emplois != null && !emplois.isEmpty()) {
            TypeEmploye typeEmploye = emplois.get(0).getTypeEmploye();
            if (typeEmploye != null && typeEmploye.getBaseCalculBoni() != null && typeEmploye.getBaseCalculBoni() > 0) {
                return typeEmploye.getBaseCalculBoni();
            }
        }
        return 12;
    }

    private BigDecimal computeMontantReference(PayrollEmployeAgregat agregat) {
        BigDecimal ref = safe(agregat.getMontantSalaireBase())
                .add(safe(agregat.getMontantSupplementaire()))
                .add(safe(agregat.getMontantAutreRevenu()))
                .subtract(safe(agregat.getMontantSanctions()));
        return ref.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeMontantBoniBrut(String formule,
                                              PayrollEmployeAgregat agregat,
                                              BigDecimal montantReference,
                                              BigDecimal diviseur,
                                              int baseCalculBoni) {
        Map<String, Object> vars = new HashMap<>();
        BigDecimal montantParPeriode = computeMontantParPeriode(montantReference, agregat != null ? agregat.getNbPaie() : null);
        BigDecimal montantForfaitaireRestant = computeMontantForfaitaireRestant(agregat, montantParPeriode);
        BigDecimal baseBoni = BigDecimal.valueOf(Math.max(baseCalculBoni, 1)).setScale(4, RoundingMode.HALF_UP);

        vars.put("amt.ref", montantReference);
        vars.put("bon.div", diviseur);
        vars.put("amt.sal.y", montantReference);
        vars.put("amt.sal.m", montantParPeriode);
        vars.put("amt.sal.r", montantForfaitaireRestant);
        vars.put("n.bon.base", baseBoni);
        vars.put("montant_reference", montantReference);
        vars.put("diviseur", diviseur);
        vars.put("montant_salaire_annuel", montantReference);
        vars.put("montant_salaire_mensuel", montantParPeriode);
        vars.put("montant_salaire_restant", montantForfaitaireRestant);
        vars.put("base_calcul_boni", baseBoni);

        String expression = normalizeExpression(formule);
        BigDecimal eval = formulaEvaluator.evaluate(expression, vars, false);
        if (eval == null) {
            eval = montantReference.divide(diviseur.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : diviseur, 6, RoundingMode.HALF_UP);
        }
        if (eval.compareTo(BigDecimal.ZERO) < 0) {
            eval = BigDecimal.ZERO;
        }
        return eval.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeMontantParPeriode(BigDecimal montantReference, Integer nbPaie) {
        if (nbPaie == null || nbPaie <= 0) {
            return BigDecimal.ZERO;
        }
        return safe(montantReference).divide(BigDecimal.valueOf(nbPaie), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal computeMontantForfaitaireRestant(PayrollEmployeAgregat agregat, BigDecimal montantParPeriode) {
        if (agregat == null || agregat.getRegimePaie() == null || montantParPeriode == null || montantParPeriode.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        int nbPeriodesTotal = agregat.getRegimePaie().getNbPeriodePaie() != null ? agregat.getRegimePaie().getNbPeriodePaie() : 0;
        int periodePaieCourante = agregat.getRegimePaie().getPeriodePaieCourante() != null ? agregat.getRegimePaie().getPeriodePaieCourante() : 0;
        int periodesRestantes = Math.max(nbPeriodesTotal - periodePaieCourante, 0);
        if (periodesRestantes == 0) {
            return BigDecimal.ZERO;
        }
        return montantParPeriode.multiply(BigDecimal.valueOf(periodesRestantes)).setScale(6, RoundingMode.HALF_UP);
    }

    private DeductionAmounts computeDeductionAmounts(DefinitionDeduction dd, BigDecimal baseMontant) {
        List<TrancheBaremeDeduction> tranches = trancheBaremeDeductionRepository
                .findByDefinitionDeductionIdOrderByBorneInfAsc(dd.getId());
        if (!tranches.isEmpty()) {
            BigDecimal total = computeDeductionFromTranches(baseMontant, tranches);
            return new DeductionAmounts(total.setScale(2, RoundingMode.HALF_UP), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        BigDecimal total;
        BigDecimal couvert;
        if (dd.getTypeDeduction() == DefinitionDeduction.TypeDeduction.POURCENTAGE) {
            total = baseMontant.multiply(safe(dd.getValeur())).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            couvert = baseMontant.multiply(safe(dd.getValeurCouvert())).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        } else {
            total = safe(dd.getValeur());
            couvert = safe(dd.getValeurCouvert());
        }
        BigDecimal montantEmploye = total.subtract(couvert);
        if (montantEmploye.compareTo(BigDecimal.ZERO) < 0) {
            montantEmploye = BigDecimal.ZERO;
        }
        if (dd.getMinPrelevement() != null && dd.getMinPrelevement().compareTo(BigDecimal.ZERO) > 0
                && montantEmploye.compareTo(dd.getMinPrelevement()) < 0) {
            montantEmploye = dd.getMinPrelevement();
        }
        if (dd.getMaxPrelevement() != null && dd.getMaxPrelevement().compareTo(BigDecimal.ZERO) > 0
                && montantEmploye.compareTo(dd.getMaxPrelevement()) > 0) {
            montantEmploye = dd.getMaxPrelevement();
        }
        return new DeductionAmounts(montantEmploye, couvert);
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

    private BigDecimal computeFromType(DefinitionDeduction.TypeDeduction type, BigDecimal valeur, BigDecimal baseMontant) {
        if (type == DefinitionDeduction.TypeDeduction.POURCENTAGE) {
            return baseMontant.multiply(safe(valeur)).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        }
        return safe(valeur);
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

    private DeductionAmounts applyArrondir(DeductionAmounts split, DefinitionDeduction.Arrondir arrondir) {
        if (arrondir == null) {
            return split;
        }
        int scale = switch (arrondir) {
            case UNITE -> 0;
            case DIXIEME -> 1;
            case CENTIEME -> 2;
            case MILLIEME -> 3;
        };
        return new DeductionAmounts(
                split.montant().setScale(scale, RoundingMode.HALF_UP),
                split.montantCouvert().setScale(scale, RoundingMode.HALF_UP)
        );
    }

    private TypeRevenu resolveTypeRevenu(Long entrepriseId, Long rubriquePaieId) {
        List<TypeRevenu> rows = typeRevenuRepository.findActifsByRubriquePaieAndEntreprisePreferEntreprise(rubriquePaieId, entrepriseId);
        if (rows.isEmpty()) {
            return null;
        }
        if (entrepriseId != null) {
            for (TypeRevenu row : rows) {
                if (row.getEntreprise() != null && Objects.equals(row.getEntreprise().getId(), entrepriseId)) {
                    return row;
                }
            }
        }
        return rows.get(0);
    }

    private String resolveFormule(TypeRevenu typeRevenu) {
        if (typeRevenu != null && typeRevenu.getFormule() != null && typeRevenu.getFormule().getExpression() != null
                && !typeRevenu.getFormule().getExpression().isBlank()) {
            return typeRevenu.getFormule().getExpression();
        }
        return "amt.ref / bon.div";
    }

    private String normalizeExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return "amt.ref / bon.div";
        }
        return expression.replaceAll("\\$\\{([^}]+)\\}", "$1");
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private PayrollEmployeBoniDTO toDTO(PayrollEmployeBoni entity) {
        PayrollEmployeBoniDTO dto = new PayrollEmployeBoniDTO();
        dto.setId(entity.getId());
        dto.setStatut(entity.getStatut() != null ? entity.getStatut().name() : null);
        dto.setRubriquePaieId(entity.getRubriquePaie() != null ? entity.getRubriquePaie().getId() : null);
        dto.setRubriquePaieCode(entity.getRubriquePaie() != null ? entity.getRubriquePaie().getCodeRubrique() : null);
        dto.setRegimePaieId(entity.getRegimePaie() != null ? entity.getRegimePaie().getId() : null);
        dto.setRegimePaieCode(entity.getRegimePaie() != null ? entity.getRegimePaie().getCodeRegimePaie() : null);
        dto.setPeriodeBoniId(entity.getPeriodeBoni() != null ? entity.getPeriodeBoni().getId() : null);
        dto.setPeriodeBoniCode(entity.getPeriodeBoni() != null ? entity.getPeriodeBoni().getCode() : null);
        dto.setEmployeId(entity.getEmploye() != null ? entity.getEmploye().getId() : null);
        dto.setEmployeCode(entity.getEmploye() != null ? entity.getEmploye().getCodeEmploye() : null);
        dto.setEmployeNom(entity.getEmploye() != null ? entity.getEmploye().getNom() : null);
        dto.setEmployePrenom(entity.getEmploye() != null ? entity.getEmploye().getPrenom() : null);
        dto.setMontantReference(entity.getMontantReference());
        dto.setDiviseur(entity.getDiviseur());
        dto.setMontantBoniBrut(entity.getMontantBoniBrut());
        dto.setMontantDeductions(entity.getMontantDeductions());
        dto.setMontantBoniNet(entity.getMontantBoniNet());
        return dto;
    }

    private PayrollBoniDeductionDTO toDeductionDTO(PayrollBoniDeduction entity) {
        PayrollBoniDeductionDTO dto = new PayrollBoniDeductionDTO();
        dto.setId(entity.getId());
        dto.setPayrollBoniId(entity.getPayrollBoni() != null ? entity.getPayrollBoni().getId() : null);
        dto.setEmployeId(entity.getEmploye() != null ? entity.getEmploye().getId() : null);
        dto.setCodeDeduction(entity.getCodeDeduction());
        dto.setLibelle(entity.getLibelle());
        dto.setBaseMontant(entity.getBaseMontant());
        dto.setTaux(entity.getTaux());
        dto.setMontant(entity.getMontant());
        dto.setMontantCouvert(entity.getMontantCouvert());
        return dto;
    }

    private record DeductionAmounts(BigDecimal montantEmploye, BigDecimal montantCouvert) {
        private BigDecimal montant() {
            return montantEmploye != null ? montantEmploye : BigDecimal.ZERO;
        }
    }
}
