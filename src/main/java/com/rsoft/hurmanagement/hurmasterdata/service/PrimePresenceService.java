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
    private final SupplementaireEmployeRepository supplementaireEmployeRepository;

    private final FormulaExpressionEvaluator formulaEvaluator = new FormulaExpressionEvaluator();

    @Transactional
    public Map<String, Object> generatePrimePresence(LocalDate dateDebut,
                                                     LocalDate dateFin,
                                                     Long entrepriseId,
                                                     String username) {
        TypeRevenu typeRevenu = resolveTypeRevenu(entrepriseId);
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

            EmploiEmploye emploi = resolveEmploiEmploye(employeId, dateFin);
            TypeEmploye typeEmploye = emploi != null ? emploi.getTypeEmploye() : employePresences.get(0).getTypeEmploye();
            if (typeEmploye == null || typeEmploye.getAjouterBonusApresNbMinutePresence() == null) {
                skippedMissingConfig++;
                continue;
            }

            int totalMinutes = sumPresenceMinutes(employePresences);
            if (totalMinutes < typeEmploye.getAjouterBonusApresNbMinutePresence()) {
                continue;
            }
            eligibleEmployes++;

            if (autreRevenuEmployeRepository.existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(
                    employeId, typeRevenu.getId(), dateDebut, dateFin)) {
                skippedExisting++;
                continue;
            }

            Map<String, Object> variables = buildVariables(salaire, emploi, employePresences, dateDebut, dateFin, totalMinutes);
            BigDecimal montant = computeMontant(typeRevenu, variables, dateFin);
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
            revenu.setStatut(AutreRevenuEmploye.StatutAutreRevenu.VALIDE);
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

    private TypeRevenu resolveTypeRevenu(Long entrepriseId) {
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
            total += computePresenceMinutes(presence);
        }
        return total;
    }

    private int computePresenceMinutes(PresenceEmploye presence) {
        if (presence == null || presence.getHeureArrivee() == null || presence.getHeureDepart() == null) {
            return 0;
        }
        try {
            LocalTime arrivee = LocalTime.parse(presence.getHeureArrivee());
            LocalTime depart = LocalTime.parse(presence.getHeureDepart());
            long minutes = java.time.Duration.between(arrivee, depart).toMinutes();
            boolean overnight = presence.getDateDepart() != null && presence.getDateDepart().isAfter(presence.getDateJour());
            if (minutes < 0 && overnight) {
                minutes += 24 * 60;
            }
            return minutes < 0 ? 0 : (int) minutes;
        } catch (Exception e) {
            return 0;
        }
    }

    private Map<String, Object> buildVariables(EmployeSalaire salaire,
                                               EmploiEmploye emploi,
                                               List<PresenceEmploye> presences,
                                               LocalDate debut,
                                               LocalDate fin,
                                               int totalMinutes) {
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
        BigDecimal coefBonW = resolveBoniCoefficient(salaire);

        vars.put("n.paid.pp", nPaid);
        vars.put("n.pres.pp", nPres);
        vars.put("n.pres.day", nPresDay);
        vars.put("n.pres.night", nPresNight);
        vars.put("n.off.pp", nOff + nFerie);
        vars.put("h.ot.pp", hOt);
        vars.put("coef.bon.w", coefBonW);
        vars.put("h.work.pp", BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
        vars.put("amt.value", BigDecimal.ZERO);
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
                Map<String, Object> details = new com.fasterxml.jackson.databind.ObjectMapper()
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

    private BigDecimal resolveBoniCoefficient(EmployeSalaire salaire) {
        if (salaire == null || salaire.getEmploi() == null || salaire.getEmploi().getTypeEmploye() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal coef = salaire.getEmploi().getTypeEmploye().getPourcentageJourBonus();
        return coef != null ? coef : BigDecimal.ZERO;
    }

    private BigDecimal computeMontant(TypeRevenu typeRevenu, Map<String, Object> variables, LocalDate dateRef) {
        if (typeRevenu == null || typeRevenu.getFormule() == null || !isFormuleEffective(typeRevenu.getFormule(), dateRef)) {
            return BigDecimal.ZERO;
        }
        String expression = normalizeExpression(typeRevenu.getFormule().getExpression());
        if (expression == null || expression.isBlank()) {
            return BigDecimal.ZERO;
        }
        BigDecimal eval = formulaEvaluator.evaluate(expression, variables, false);
        return eval != null ? eval : BigDecimal.ZERO;
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
