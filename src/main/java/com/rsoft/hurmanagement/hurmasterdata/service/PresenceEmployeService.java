package com.rsoft.hurmanagement.hurmasterdata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsoft.hurmanagement.hurmasterdata.dto.PresenceEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PresenceEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PresenceEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PresenceEmployeService {

    private final PresenceEmployeRepository repository;
    private final EmployeRepository employeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final EmployeSalaireRepository employeSalaireRepository;
    private final HoraireSpecialRepository horaireSpecialRepository;
    private final HoraireDtRepository horaireDtRepository;
    private final JourCongeRepository jourCongeRepository;
    private final CongeEmployeRepository congeEmployeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public Page<PresenceEmployeDTO> findByFilters(String dateDebut,
                                                  String dateFin,
                                                  Long employeId,
                                                  String statut,
                                                  Long entrepriseId,
                                                  Pageable pageable) {
        LocalDate debut = LocalDate.parse(dateDebut);
        LocalDate fin = LocalDate.parse(dateFin);
        PresenceEmploye.StatutPresence statutEnum = null;
        if (statut != null && !statut.trim().isEmpty()) {
            try {
                statutEnum = PresenceEmploye.StatutPresence.valueOf(statut);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return repository.findByFilters(debut, fin, employeId, statutEnum, entrepriseId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public PresenceEmployeDTO findById(Long id) {
        PresenceEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PresenceEmploye not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional
    public PresenceEmployeDTO create(PresenceEmployeCreateDTO dto, String username) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));

        PresenceEmploye entity = new PresenceEmploye();
        entity.setEmploye(employe);
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        } else {
            entity.setEntreprise(employe.getEntreprise());
        }

        entity.setDateJour(dto.getDateJour());
        entity.setDateDepart(dto.getDateDepart());
        entity.setHeureArrivee(dto.getHeureArrivee());
        entity.setHeureDepart(cleanTime(dto.getHeureDepart()));
        entity.setCumulPauseMin(dto.getCumulPauseMin() != null ? dto.getCumulPauseMin() : 0);
        entity.setCommentaire(dto.getCommentaire());
        entity.setSourceSaisie(resolveSource(dto.getSourceSaisie()));
        entity.setAutomatique("N");
        entity.setFermetureManuelle("Y");
        entity.setCumulPauseMin(0);

        applyDerivedFields(entity);

        entity.setCreatedBy(username);
        entity.setCreatedOn(java.time.OffsetDateTime.now());
        entity.setRowscn(1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public PresenceEmployeDTO update(Long id, PresenceEmployeUpdateDTO dto, String username) {
        PresenceEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PresenceEmploye not found with id: " + id));
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }

        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        entity.setEmploye(employe);
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        } else {
            entity.setEntreprise(employe.getEntreprise());
        }

        entity.setDateJour(dto.getDateJour());
        entity.setDateDepart(dto.getDateDepart());
        entity.setHeureArrivee(dto.getHeureArrivee());
        entity.setHeureDepart(cleanTime(dto.getHeureDepart()));
        if (dto.getCumulPauseMin() != null) {
            entity.setCumulPauseMin(dto.getCumulPauseMin());
        }
        entity.setCommentaire(dto.getCommentaire());
        entity.setSourceSaisie(resolveSource(dto.getSourceSaisie()));

        applyDerivedFields(entity);

        entity.setUpdatedBy(username);
        entity.setUpdatedOn(java.time.OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, Integer rowscn) {
        PresenceEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PresenceEmploye not found with id: " + id));
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        repository.delete(entity);
    }

    void applyDerivedFields(PresenceEmploye entity) {
        EmploiEmploye emploi = resolveEmploiEmploye(entity.getEmploye().getId(), entity.getDateJour());
        entity.setTypeEmploye(emploi != null ? emploi.getTypeEmploye() : null);

        EmployeSalaire salaire = employeSalaireRepository
                .findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(entity.getEmploye().getId(), "Y")
                .orElse(null);
        entity.setRegimePaie(salaire != null ? salaire.getRegimePaie() : null);

        Horaire horaire = emploi != null ? emploi.getHoraire() : null;
        HorairePlan plan = resolveHorairePlan(entity, emploi, horaire);
        entity.setHoraireSpecial(plan.horaireSpecial ? "Y" : "N");
        entity.setIdHoraire(plan.horaireId);
        entity.setHeureDebutPrevue(plan.heureDebutPrevue);
        entity.setHeureFinPrevue(plan.heureFinPrevue);
        entity.setNuitPlanifiee(plan.nuitPlanifiee ? "Y" : "N");

        entity.setGenererSupplementaire(horaire != null && "Y".equalsIgnoreCase(horaire.getPayerSupplementaire()) ? "Y" : "N");
        entity.setGenererBoni("Y");
        entity.setSupplementaireGenere("N");
        entity.setBoniGenere(shouldBoni(entity.getTypeEmploye()) ? "Y" : "N");

        boolean hasDepart = entity.getDateDepart() != null && isNotBlank(entity.getHeureDepart());
        entity.setStatutPresence(hasDepart ? PresenceEmploye.StatutPresence.VALIDE : PresenceEmploye.StatutPresence.BROUILLON);

        entity.setTypeErreur(determineTypeErreur(entity, plan));

        entity.setDetails(buildDetails(entity, plan));
    }

    private HorairePlan resolveHorairePlan(PresenceEmploye entity, EmploiEmploye emploi, Horaire horaire) {
        HorairePlan plan = new HorairePlan();
        LocalDate date = entity.getDateJour();
        List<HoraireSpecial> specials = horaireSpecialRepository.findActiveByEmployeIdAndDate(entity.getEmploye().getId(), date);
        if (!specials.isEmpty()) {
            HoraireSpecial hs = specials.get(0);
            plan.horaireSpecial = true;
            plan.horaireId = hs.getId();
            plan.heureDebutPrevue = hs.getHeureDebut();
            plan.heureFinPrevue = hs.getHeureFin();
            plan.nuitPlanifiee = hs.getDateFin() != null && hs.getDateFin().isAfter(hs.getDateDebut());
            if (entity.getDateDepart() != null && entity.getDateDepart().isAfter(entity.getDateJour())) {
                plan.nuitPlanifiee = true;
            }
            if (horaire != null && horaire.getNbHeuresRef() != null) {
                plan.nbHeuresJour = horaire.getNbHeuresRef();
            }
            return plan;
        }

        Horaire horaireToUse = horaire;
        if (horaireToUse == null && emploi != null) {
            horaireToUse = emploi.getHoraire();
        }
        if (horaireToUse == null) {
            return plan;
        }
        if (horaireToUse.getNbHeuresRef() != null) {
            plan.nbHeuresJour = horaireToUse.getNbHeuresRef();
        }

        int dayIndex = entity.getDateJour().getDayOfWeek().getValue();
        HoraireDt horaireDt = horaireDtRepository.findByHoraireIdAndJour(horaireToUse.getId(), dayIndex);
        if (horaireDt == null) {
            return plan;
        }
        boolean hasDay = isNotBlank(horaireDt.getHeureDebutJour()) && isNotBlank(horaireDt.getHeureFinJour());
        boolean hasNight = isNotBlank(horaireDt.getHeureDebutNuit()) && isNotBlank(horaireDt.getHeureFinNuit());
        boolean nightPlan = false;

        if (hasDay && hasNight && isOverlapping(horaireDt)) {
            hasNight = false;
        }
        if (hasNight) {
            nightPlan = isArrivalInNight(entity.getHeureArrivee(), horaireDt);
            if (!nightPlan && !hasDay) {
                nightPlan = true;
            }
        }

        if (nightPlan) {
            plan.nuitPlanifiee = true;
            plan.heureDebutPrevue = horaireDt.getHeureDebutNuit();
            plan.heureFinPrevue = horaireDt.getHeureFinNuit();
        } else if (hasDay) {
            plan.heureDebutPrevue = horaireDt.getHeureDebutJour();
            plan.heureFinPrevue = horaireDt.getHeureFinJour();
        }
        plan.horaireSpecial = false;
        plan.horaireId = horaireToUse.getId();
        if (entity.getDateDepart() != null && entity.getDateDepart().isAfter(entity.getDateJour())) {
            plan.nuitPlanifiee = true;
        }
        return plan;
    }

    private PresenceEmploye.TypeErreur determineTypeErreur(PresenceEmploye entity, HorairePlan plan) {
        if (entity.getDateDepart() == null || !isNotBlank(entity.getHeureDepart())) {
            return PresenceEmploye.TypeErreur.INVALIDE;
        }

        if (plan.heureDebutPrevue != null) {
            BigDecimal diff = minutesDiff(entity.getHeureArrivee(), plan.heureDebutPrevue, plan.nuitPlanifiee);
            if (diff.compareTo(BigDecimal.valueOf(60)) >= 0) {
                return PresenceEmploye.TypeErreur.HEURE;
            }
        }

        if (plan.nuitPlanifiee && entity.getDateDepart() != null && entity.getDateDepart().equals(entity.getDateJour())) {
            return PresenceEmploye.TypeErreur.NUIT;
        }

        if (!plan.nuitPlanifiee && entity.getDateDepart() != null && !entity.getDateDepart().equals(entity.getDateJour())) {
            return PresenceEmploye.TypeErreur.JOUT;
        }

        return PresenceEmploye.TypeErreur.VALIDE;
    }

    private String buildDetails(PresenceEmploye entity, HorairePlan plan) {
        if (entity.getStatutPresence() != PresenceEmploye.StatutPresence.VALIDE) {
            return "{}";
        }
        boolean conge = congeEmployeRepository.existsActiveCongeForDate(entity.getEmploye().getId(), entity.getDateJour());
        boolean off = isOffDay(entity.getEmploye().getId(), entity.getDateJour());
        boolean ferie = jourCongeRepository.existsByDateCongeAndActif(entity.getDateJour(), JourConge.Actif.Y);

        Map<String, Object> details = new HashMap<>();
        details.put("conge", conge ? "Y" : "N");
        details.put("off", off ? "Y" : "N");
        details.put("ferie", ferie ? "Y" : "N");
        details.put("unite_retard", computeRetardUnite(entity, plan));
        details.put("nb_retard", computeRetardValeur(entity, plan));
        details.put("nb_heures_jour", computeNbHeuresJour(entity, plan));

        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error generating details JSON", e);
        }
    }

    private String computeRetardUnite(PresenceEmploye entity, HorairePlan plan) {
        if (!isNotBlank(entity.getHeureArrivee()) || !isNotBlank(plan.heureDebutPrevue)) {
            return null;
        }
        BigDecimal minutes = minutesDiff(entity.getHeureArrivee(), plan.heureDebutPrevue, plan.nuitPlanifiee);
        if (minutes.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return minutes.compareTo(BigDecimal.valueOf(60)) >= 0 ? "HEURE" : "MINUTE";
    }

    private BigDecimal computeNbHeuresJour(PresenceEmploye entity, HorairePlan plan) {
        if (!isNotBlank(entity.getHeureArrivee()) || !isNotBlank(entity.getHeureDepart())) {
            return plan.nbHeuresJour != null ? plan.nbHeuresJour : BigDecimal.ZERO;
        }
        try {
            LocalTime arrivee = LocalTime.parse(entity.getHeureArrivee());
            LocalTime depart = LocalTime.parse(entity.getHeureDepart());
            long minutes = Duration.between(arrivee, depart).toMinutes();
            boolean overnight = entity.getDateDepart() != null && entity.getDateDepart().isAfter(entity.getDateJour());
            if (minutes < 0 && overnight) {
                minutes += 24 * 60;
            }
            if (minutes < 0) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(minutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return plan.nbHeuresJour != null ? plan.nbHeuresJour : BigDecimal.ZERO;
        }
    }

    private BigDecimal computeRetardValeur(PresenceEmploye entity, HorairePlan plan) {
        if (!isNotBlank(entity.getHeureArrivee()) || !isNotBlank(plan.heureDebutPrevue)) {
            return BigDecimal.ZERO;
        }
        BigDecimal minutes = minutesDiff(entity.getHeureArrivee(), plan.heureDebutPrevue, plan.nuitPlanifiee);
        if (minutes.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (minutes.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return minutes.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        }
        return minutes;
    }

    private BigDecimal minutesDiff(String actual, String planned, boolean allowOvernight) {
        try {
            LocalTime a = LocalTime.parse(actual);
            LocalTime p = LocalTime.parse(planned);
            long minutes = Duration.between(p, a).toMinutes();
            if (minutes < 0 && allowOvernight) {
                minutes += 24 * 60;
            }
            if (minutes < 0) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(minutes);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean isOffDay(Long employeId, LocalDate date) {
        EmploiEmploye emploi = resolveEmploiEmploye(employeId, date);
        if (emploi == null) {
            return false;
        }
        int dayIndex = date.getDayOfWeek().getValue();
        return (emploi.getJourOff1() != null && emploi.getJourOff1() == dayIndex)
                || (emploi.getJourOff2() != null && emploi.getJourOff2() == dayIndex)
                || (emploi.getJourOff3() != null && emploi.getJourOff3() == dayIndex);
    }

    private EmploiEmploye resolveEmploiEmploye(Long employeId, LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        List<EmploiEmploye> emplois = emploiEmployeRepository.findActiveForDate(employeId, target);
        if (emplois != null && !emplois.isEmpty()) {
            return emplois.get(0);
        }
        emplois = emploiEmployeRepository.findByEmployeIdAndStatutEmploiNot(
                employeId, EmploiEmploye.StatutEmploi.TERMINE);
        return emplois.isEmpty() ? null : emplois.get(0);
    }

    private boolean shouldBoni(TypeEmploye typeEmploye) {
        if (typeEmploye == null || typeEmploye.getPourcentageJourBonus() == null) {
            return false;
        }
        return typeEmploye.getPourcentageJourBonus().compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isArrivalInNight(String heureArrivee, HoraireDt horaireDt) {
        if (!isNotBlank(heureArrivee) || !isNotBlank(horaireDt.getHeureDebutNuit()) || !isNotBlank(horaireDt.getHeureFinNuit())) {
            return false;
        }
        try {
            LocalTime arrivee = LocalTime.parse(heureArrivee);
            LocalTime start = LocalTime.parse(horaireDt.getHeureDebutNuit());
            LocalTime end = LocalTime.parse(horaireDt.getHeureFinNuit());
            if (end.isBefore(start)) {
                return !arrivee.isBefore(start) || !arrivee.isAfter(end);
            }
            return !arrivee.isBefore(start) && !arrivee.isAfter(end);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isOverlapping(HoraireDt horaireDt) {
        try {
            LocalTime dayStart = LocalTime.parse(horaireDt.getHeureDebutJour());
            LocalTime dayEnd = LocalTime.parse(horaireDt.getHeureFinJour());
            LocalTime nightStart = LocalTime.parse(horaireDt.getHeureDebutNuit());
            LocalTime nightEnd = LocalTime.parse(horaireDt.getHeureFinNuit());
            return !(dayEnd.isBefore(nightStart) || nightEnd.isBefore(dayStart));
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String cleanTime(String value) {
        return isNotBlank(value) ? value : null;
    }

    private PresenceEmploye.SourceSaisie resolveSource(String source) {
        if (source == null || source.trim().isEmpty()) {
            return PresenceEmploye.SourceSaisie.MANUEL;
        }
        try {
            return PresenceEmploye.SourceSaisie.valueOf(source.trim());
        } catch (IllegalArgumentException e) {
            return PresenceEmploye.SourceSaisie.MANUEL;
        }
    }

    private PresenceEmployeDTO toDTO(PresenceEmploye entity) {
        PresenceEmployeDTO dto = new PresenceEmployeDTO();
        dto.setId(entity.getId());
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        if (entity.getEmploye() != null) {
            dto.setEmployeId(entity.getEmploye().getId());
            dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
            dto.setEmployeNom(entity.getEmploye().getNom());
            dto.setEmployePrenom(entity.getEmploye().getPrenom());
        }
        dto.setDateJour(entity.getDateJour());
        dto.setDateDepart(entity.getDateDepart());
        dto.setHeureArrivee(entity.getHeureArrivee());
        dto.setHeureDepart(entity.getHeureDepart());
        dto.setNuitPlanifiee(entity.getNuitPlanifiee());
        dto.setHeureDebutPrevue(entity.getHeureDebutPrevue());
        dto.setHeureFinPrevue(entity.getHeureFinPrevue());
        dto.setTypeEmployeId(entity.getTypeEmploye() != null ? entity.getTypeEmploye().getId() : null);
        dto.setRegimePaieId(entity.getRegimePaie() != null ? entity.getRegimePaie().getId() : null);
        dto.setIdHoraire(entity.getIdHoraire());
        dto.setHoraireSpecial(entity.getHoraireSpecial());
        dto.setAutomatique(entity.getAutomatique());
        dto.setGenererSupplementaire(entity.getGenererSupplementaire());
        dto.setSupplementaireGenere(entity.getSupplementaireGenere());
        dto.setGenererBoni(entity.getGenererBoni());
        dto.setBoniGenere(entity.getBoniGenere());
        dto.setSourceSaisie(entity.getSourceSaisie() != null ? entity.getSourceSaisie().name() : null);
        dto.setStatutPresence(entity.getStatutPresence() != null ? entity.getStatutPresence().name() : null);
        dto.setNbHeuresSup(entity.getNbHeuresSup());
        dto.setCumulPauseMin(entity.getCumulPauseMin());
        dto.setNoSupplementaire(entity.getNoSupplementaire());
        dto.setFermetureManuelle(entity.getFermetureManuelle());
        dto.setCommentaire(entity.getCommentaire());
        dto.setTypeErreur(entity.getTypeErreur() != null ? entity.getTypeErreur().name() : null);
        dto.setDetails(entity.getDetails());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private static class HorairePlan {
        private boolean horaireSpecial;
        private boolean nuitPlanifiee;
        private Long horaireId;
        private String heureDebutPrevue;
        private String heureFinPrevue;
        private BigDecimal nbHeuresJour = BigDecimal.ZERO;
    }
}
