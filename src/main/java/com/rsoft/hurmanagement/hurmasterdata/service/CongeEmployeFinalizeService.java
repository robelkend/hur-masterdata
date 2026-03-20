package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.BalanceConge;
import com.rsoft.hurmanagement.hurmasterdata.entity.BalanceCongeAnnee;
import com.rsoft.hurmanagement.hurmasterdata.entity.CongeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.JourConge;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeConge;
import com.rsoft.hurmanagement.hurmasterdata.repository.BalanceCongeAnneeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.BalanceCongeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.CongeEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.JourCongeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PresenceEmployeRepository;
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

@Service
@RequiredArgsConstructor
public class CongeEmployeFinalizeService {

    private final CongeEmployeRepository congeEmployeRepository;
    private final BalanceCongeRepository balanceCongeRepository;
    private final BalanceCongeAnneeRepository balanceCongeAnneeRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final PresenceEmployeRepository presenceEmployeRepository;
    private final JourCongeRepository jourCongeRepository;

    @Transactional
    public CongeEmploye finalizeConge(Long congeId, String username, boolean autoFinalize) {
                
        CongeEmploye conge = congeEmployeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("CongeEmploye not found with id: " + congeId));
        if (conge.getStatut() != CongeEmploye.StatutConge.EN_COURS) {
            throw new RuntimeException("CongeEmploye must be EN_COURS to finalize");
        }

        if (conge.getDateDebutReel() == null) {
            conge.setDateDebutReel(conge.getDateDebutPlan());
        }
        if (conge.getDateFinReel() == null) {
            conge.setDateFinReel(conge.getDateFinPlan());
        }

        if (conge.getDateDebutReel() != null && conge.getDateFinReel() != null) {
            conge.setNbJoursReel(calculateNbJoursExcludingOffAndHolidays(
                    conge.getEmploiEmploye(),
                    conge.getDateDebutReel(),
                    conge.getDateFinReel()
            ));
        }

        updateBalances(conge);
        releaseEmploiConge(conge);

        conge.setStatut(CongeEmploye.StatutConge.TERMINE);
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
        List<CongeEmploye> conges = congeEmployeRepository.findByStatut(CongeEmploye.StatutConge.EN_COURS);
        int total = 0;
        int finalized = 0;
        int skippedNoPresence = 0;
        for (CongeEmploye conge : conges) {
            if (entrepriseId != null) {
                Long congeEntrepriseId = conge.getEntreprise() != null ? conge.getEntreprise().getId() : null;
                if (!entrepriseId.equals(congeEntrepriseId)) {
                    continue;
                }
            }
            total++;
            if (conge.getDateDebutPlan() == null) {
                continue;
            }
            if (!hasPresenceFromStartDate(conge)) {
                skippedNoPresence++;
                continue;
            }
            finalizeConge(conge.getId(), username, true);
            finalized++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalRows", total);
        result.put("finalizedRows", finalized);
        result.put("skippedNoPresence", skippedNoPresence);
        result.put("message", "Finalized " + finalized + " leaves");
        return result;
    }

    private boolean hasPresenceFromStartDate(CongeEmploye conge) {
        if (conge.getEmploye() == null || conge.getEmploye().getId() == null) {
            return false;
        }
        if (conge.getDateDebutPlan() == null) {
            return false;
        }
        return presenceEmployeRepository.existsByEmployeIdAndDateJourGreaterThanEqualAndStatutPresence(
                conge.getEmploye().getId(),
                conge.getDateDebutPlan(),
                com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye.StatutPresence.VALIDE
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

    private BigDecimal calculateNbJoursExcludingOffAndHolidays(EmploiEmploye emploi, LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null || dateFin == null) {
            return BigDecimal.ZERO;
        }
        LocalDate start = dateDebut;
        LocalDate end = dateFin;
        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        long effectiveDays = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (isOffDay(emploi, date)) {
                continue;
            }
            if (jourCongeRepository.existsByDateCongeAndActif(date, JourConge.Actif.Y)) {
                continue;
            }
            effectiveDays++;
        }
        return BigDecimal.valueOf(effectiveDays).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isOffDay(EmploiEmploye emploi, LocalDate date) {
        if (emploi == null || date == null) {
            return false;
        }
        int day = date.getDayOfWeek().getValue();
        return java.util.Objects.equals(emploi.getJourOff1(), day)
                || java.util.Objects.equals(emploi.getJourOff2(), day)
                || java.util.Objects.equals(emploi.getJourOff3(), day);
    }
}
