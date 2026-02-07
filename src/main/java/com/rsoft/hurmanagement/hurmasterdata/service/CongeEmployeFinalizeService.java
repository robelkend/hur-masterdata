package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.BalanceConge;
import com.rsoft.hurmanagement.hurmasterdata.entity.BalanceCongeAnnee;
import com.rsoft.hurmanagement.hurmasterdata.entity.CongeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeConge;
import com.rsoft.hurmanagement.hurmasterdata.repository.BalanceCongeAnneeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.BalanceCongeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.CongeEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PointageBrutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CongeEmployeFinalizeService {

    private final CongeEmployeRepository congeEmployeRepository;
    private final BalanceCongeRepository balanceCongeRepository;
    private final BalanceCongeAnneeRepository balanceCongeAnneeRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final PointageBrutRepository pointageBrutRepository;

    @Transactional
    public CongeEmploye finalizeConge(Long congeId, String username, boolean autoFinalize) {
        CongeEmploye conge = congeEmployeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("CongeEmploye not found with id: " + congeId));
        if (conge.getStatut() != CongeEmploye.StatutConge.EN_COURS) {
            throw new RuntimeException("CongeEmploye must be EN_COURS to finalize");
        }

        if (autoFinalize && conge.getDateDebutReel() == null) {
            conge.setDateDebutReel(conge.getDateDebutPlan());
        }
        if (autoFinalize && conge.getDateFinReel() == null) {
            conge.setDateFinReel(conge.getDateFinPlan());
        }

        if (conge.getDateDebutReel() != null && conge.getDateFinReel() != null) {
            conge.setNbJoursReel(calculateNbJours(conge.getDateDebutReel(), conge.getDateFinReel()));
        }

        updateBalances(conge);
        releaseEmploiConge(conge);

        conge.setStatut(CongeEmploye.StatutConge.FINALISE);
        conge.setUpdatedBy(username);
        conge.setUpdatedOn(OffsetDateTime.now());
        conge.setRowscn(conge.getRowscn() + 1);
        return congeEmployeRepository.save(conge);
    }

    @Transactional
    public Map<String, Object> autoFinalizeForDate(LocalDate targetDate, String username) {
        return autoFinalizeForDate(targetDate, null, username);
    }

    @Transactional
    public Map<String, Object> autoFinalizeForDate(LocalDate targetDate, Long entrepriseId, String username) {
        List<CongeEmploye> conges = congeEmployeRepository.findByStatutAndDateFinPlanBefore(
                CongeEmploye.StatutConge.EN_COURS,
                targetDate
        );

        int total = 0;
        int finalized = 0;
        int skippedNoPointage = 0;
        for (CongeEmploye conge : conges) {
            if (entrepriseId != null) {
                Long congeEntrepriseId = conge.getEntreprise() != null ? conge.getEntreprise().getId() : null;
                if (!entrepriseId.equals(congeEntrepriseId)) {
                    continue;
                }
            }
            total++;
            if (!hasPointageForDay(conge, targetDate)) {
                skippedNoPointage++;
                continue;
            }
            finalizeConge(conge.getId(), username, true);
            finalized++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalRows", total);
        result.put("finalizedRows", finalized);
        result.put("skippedNoPointage", skippedNoPointage);
        result.put("message", "Finalized " + finalized + " leaves");
        return result;
    }

    private boolean hasPointageForDay(CongeEmploye conge, LocalDate date) {
        if (conge.getEmploye() == null || conge.getEmploye().getId() == null) {
            return false;
        }
        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime start = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime end = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
        return pointageBrutRepository.existsByEmployeIdAndDateHeurePointageBetween(
                conge.getEmploye().getId(),
                start,
                end
        );
    }

    private void updateBalances(CongeEmploye conge) {
        if (conge.getEmploye() == null || conge.getTypeConge() == null) {
            return;
        }
        Long employeId = conge.getEmploye().getId();
        Long emploiEmployeId = conge.getEmploiEmploye() != null ? conge.getEmploiEmploye().getId() : null;
        Long typeCongeId = conge.getTypeConge().getId();
        if (emploiEmployeId == null) {
            return;
        }

        BigDecimal nbJours = conge.getNbJoursReel() != null ? conge.getNbJoursReel() : BigDecimal.ZERO;
        BigDecimal baseEntitlement = resolveBaseEntitlement(conge.getTypeConge());

        BalanceConge balance = balanceCongeRepository
                .findFirstByEmploiEmployeIdAndEmployeIdAndTypeCongeId(emploiEmployeId, employeId, typeCongeId)
                .orElseGet(() -> createBalanceConge(conge, baseEntitlement));

        balance.setSoldeActuel(balance.getSoldeActuel().subtract(nbJours));
        balance.setSoldeDisponible(balance.getSoldeDisponible().subtract(nbJours));
        balance.setDerniereMiseAJour(LocalDate.now());
        balanceCongeRepository.save(balance);

        Integer year = conge.getDateFinReel() != null ? conge.getDateFinReel().getYear() : conge.getDateFinPlan().getYear();
        BalanceCongeAnnee annee = balanceCongeAnneeRepository
                .findByBalanceCongeIdAndAnnee(balance.getId(), year)
                .orElseGet(() -> createBalanceCongeAnnee(balance, conge, baseEntitlement, year));

        annee.setJoursPris(annee.getJoursPris().add(nbJours));
        BigDecimal soldeFin = annee.getJoursAcquis()
                .add(annee.getJoursReportes())
                .subtract(annee.getJoursExpires())
                .subtract(annee.getJoursPris());
        annee.setSoldeFinAnnee(soldeFin);
        balanceCongeAnneeRepository.save(annee);
    }

    private BalanceConge createBalanceConge(CongeEmploye conge, BigDecimal baseEntitlement) {
        BalanceConge balance = new BalanceConge();
        balance.setEntreprise(conge.getEntreprise());
        balance.setEmploiEmploye(conge.getEmploiEmploye());
        balance.setEmploye(conge.getEmploye());
        balance.setTypeConge(conge.getTypeConge());
        balance.setSoldeActuel(baseEntitlement);
        balance.setSoldeDisponible(baseEntitlement);
        balance.setDerniereMiseAJour(LocalDate.now());
        balance.setActif("Y");
        balance.setCreatedBy("system");
        balance.setCreatedOn(OffsetDateTime.now());
        balance.setRowscn(1);
        return balanceCongeRepository.save(balance);
    }

    private BalanceCongeAnnee createBalanceCongeAnnee(BalanceConge balance,
                                                      CongeEmploye conge,
                                                      BigDecimal baseEntitlement,
                                                      Integer year) {
        BalanceCongeAnnee annee = new BalanceCongeAnnee();
        annee.setBalanceConge(balance);
        annee.setEmploiEmploye(conge.getEmploiEmploye());
        annee.setTypeConge(conge.getTypeConge());
        annee.setAnnee(year);
        annee.setJoursAcquis(baseEntitlement);
        annee.setJoursPris(BigDecimal.ZERO);
        annee.setCumulAutorise("N");
        annee.setJoursReportes(BigDecimal.ZERO);
        annee.setJoursExpires(BigDecimal.ZERO);
        annee.setSoldeFinAnnee(baseEntitlement);
        annee.setCreatedBy("system");
        annee.setCreatedOn(OffsetDateTime.now());
        annee.setRowscn(1);
        return balanceCongeAnneeRepository.save(annee);
    }

    private void releaseEmploiConge(CongeEmploye conge) {
        if (conge.getEmploiEmploye() == null) {
            return;
        }
        EmploiEmploye emploi = conge.getEmploiEmploye();
        emploi.setEnConge("N");
        emploiEmployeRepository.save(emploi);
    }

    private BigDecimal resolveBaseEntitlement(TypeConge typeConge) {
        if (typeConge == null || typeConge.getNbJours() == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(typeConge.getNbJours()).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateNbJours(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null || dateFin == null) {
            return BigDecimal.ZERO;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
        return BigDecimal.valueOf(days).setScale(2, RoundingMode.HALF_UP);
    }
}
