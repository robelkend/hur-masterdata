package com.rsoft.hurmanagement.hurmasterdata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireGenerationRequestDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireGenerationResultDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.SupplementaireValidationResultDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupplementaireEmployeService {
    
    private final SupplementaireEmployeRepository repository;
    private final EmployeRepository employeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final DeviseRepository deviseRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final HoraireSpecialRepository horaireSpecialRepository;
    private final HoraireDtRepository horaireDtRepository;
    private final JourCongeRepository jourCongeRepository;
    private final CongeEmployeRepository congeEmployeRepository;
    private final EmployeSalaireRepository employeSalaireRepository;
    private final FormuleRepository formuleRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FormulaExpressionEvaluator formulaEvaluator = new FormulaExpressionEvaluator();
    
    @Transactional(readOnly = true)
    public Page<SupplementaireEmployeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<SupplementaireEmployeDTO> findByEmployeId(Long employeId, Pageable pageable) {
        return repository.findByEmployeId(employeId, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<SupplementaireEmployeDTO> findByFilters(String dateDebut, String dateFin, Long employeId, String statut, Long entrepriseId, Pageable pageable) {
        LocalDate dateDebutLocal = LocalDate.parse(dateDebut);
        LocalDate dateFinLocal = LocalDate.parse(dateFin);
        
        SupplementaireEmploye.StatutSupplementaire statutEnum = null;
        if (statut != null && !statut.trim().isEmpty()) {
            try {
                statutEnum = SupplementaireEmploye.StatutSupplementaire.valueOf(statut);
            } catch (IllegalArgumentException e) {
                // Invalid statut, will be ignored
            }
        }
        return repository.findByFilters(dateDebutLocal, dateFinLocal, employeId, statutEnum, entrepriseId, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public SupplementaireEmployeDTO findById(Long id) {
        SupplementaireEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SupplementaireEmploye not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public SupplementaireEmployeDTO create(SupplementaireEmployeCreateDTO dto, String username) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        
        SupplementaireEmploye entity = new SupplementaireEmploye();
        entity.setEmploye(employe);
        entity.setEmploiEmploye(resolveEmploiEmploye(dto.getEmployeId(), dto.getEmploiEmployeId()));
        entity.setMemo(dto.getMemo());
        entity.setDateJour(dto.getDateJour());
        entity.setHeureDebut(dto.getHeureDebut());
        entity.setHeureFin(dto.getHeureFin());
        
        if (dto.getTypeSupplementaire() != null) {
            entity.setTypeSupplementaire(SupplementaireEmploye.TypeSupplementaire.valueOf(dto.getTypeSupplementaire()));
        }
        
        // Generate details JSON from form fields
        String detailsJson = generateDetailsJson(
            dto.getNbHeures(),
            dto.getNbJours(),
            dto.getNbNuits(),
            dto.getNbOffs(),
            dto.getNbConges()
        );
        entity.setDetails(detailsJson);
        
        if (dto.getBaseCalcul() != null) {
            entity.setBaseCalcul(SupplementaireEmploye.BaseCalcul.valueOf(dto.getBaseCalcul()));
        }
        entity.setMontantBase(dto.getMontantBase());
        
        if (dto.getDeviseId() != null) {
            Devise devise = deviseRepository.findById(dto.getDeviseId())
                    .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
            entity.setDevise(devise);
        }
        
        entity.setMontantCalcule(dto.getMontantCalcule());
        entity.setAutomatique(dto.getAutomatique() != null ? dto.getAutomatique() : "N");
        entity.setNoPresence(0);
        entity.setNoPayroll(0);
        
        entity.setStatut(SupplementaireEmploye.StatutSupplementaire.BROUILLON);
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        }
        
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public SupplementaireEmployeDTO update(Long id, SupplementaireEmployeUpdateDTO dto, String username) {
        SupplementaireEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SupplementaireEmploye not found with id: " + id));

        if (entity.getStatut() == SupplementaireEmploye.StatutSupplementaire.VALIDE) {
            throw new RuntimeException("supplementaireEmploye.error.cannotEdit");
        }
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        entity.setEmploye(employe);
        entity.setEmploiEmploye(resolveEmploiEmploye(dto.getEmployeId(), dto.getEmploiEmployeId()));
        
        entity.setMemo(dto.getMemo());
        entity.setDateJour(dto.getDateJour());
        entity.setHeureDebut(dto.getHeureDebut());
        entity.setHeureFin(dto.getHeureFin());
        
        if (dto.getTypeSupplementaire() != null) {
            entity.setTypeSupplementaire(SupplementaireEmploye.TypeSupplementaire.valueOf(dto.getTypeSupplementaire()));
        }
        
        // Generate details JSON from form fields
        String detailsJson = generateDetailsJson(
            dto.getNbHeures(),
            dto.getNbJours(),
            dto.getNbNuits(),
            dto.getNbOffs(),
            dto.getNbConges()
        );
        entity.setDetails(detailsJson);
        
        if (dto.getBaseCalcul() != null) {
            entity.setBaseCalcul(SupplementaireEmploye.BaseCalcul.valueOf(dto.getBaseCalcul()));
        } else {
            entity.setBaseCalcul(null);
        }
        entity.setMontantBase(dto.getMontantBase());
        
        if (dto.getDeviseId() != null) {
            Devise devise = deviseRepository.findById(dto.getDeviseId())
                    .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
            entity.setDevise(devise);
        } else {
            entity.setDevise(null);
        }
        
        entity.setMontantCalcule(dto.getMontantCalcule());
        entity.setAutomatique(dto.getAutomatique() != null ? dto.getAutomatique() : "N");
        
        // Status changes are only allowed via validation action
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        } else {
            entity.setEntreprise(null);
        }
        
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        SupplementaireEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SupplementaireEmploye not found with id: " + id));

        if (entity.getStatut() == SupplementaireEmploye.StatutSupplementaire.VALIDE) {
            throw new RuntimeException("supplementaireEmploye.error.cannotDelete");
        }
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }

    @Transactional
    public SupplementaireGenerationResultDTO generateSupplementaires(SupplementaireGenerationRequestDTO request, String username) {
        LocalDate dateDebut = LocalDate.parse(request.getDateDebut());
        LocalDate dateFin = LocalDate.parse(request.getDateFin());
        if (dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("Date fin must be after or equal to date debut");
        }

        int deletedCount = repository.deleteAutoDraftsInRange(
                dateDebut, dateFin, request.getEmployeId(), request.getEntrepriseId());
				

        List<EmploiEmploye> emplois = emploiEmployeRepository.findEligibleForSupplementaire(
                request.getEntrepriseId(), request.getEmployeId());
        int generatedCount = 0;
        for (EmploiEmploye emploi : emplois) {
            Employe employe = emploi.getEmploye();
            Horaire horaire = emploi.getHoraire();
            EmployeSalaire salaire = employeSalaireRepository
                    .findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(employe.getId(), "Y")
                    .orElse(null);
            RegimePaie regimePaie = salaire != null ? salaire.getRegimePaie() : null;
            Devise devise = regimePaie != null ? regimePaie.getDevise() : null;

            for (LocalDate date = dateDebut; !date.isAfter(dateFin); date = date.plusDays(1)) {
                PresenceInfo presence = findLastPresence(employe.getId(), date);
                boolean hasPresence = presence != null;
                boolean isFerie = jourCongeRepository.existsByDateCongeAndActif(date, JourConge.Actif.Y);
                boolean isOff = isOffDay(emploi, date);
                boolean isConge = congeEmployeRepository.existsCongeForDate(employe.getId(), date);

                List<Planification> planifications = resolvePlanifications(emploi, date);
                if (planifications.isEmpty()) {
                    continue;
                }

                for (Planification plan : planifications) {
                    GenerationDecision decision = decideFormula(
                            hasPresence,
                            isConge,
                            isOff,
                            isFerie,
                            plan.isNight,
                            plan.fromHoraireSpecial,
                            horaire != null ? horaire.getMontantFixe() : "N");

                    if (decision == null || decision.formuleCode == null) {
                        continue;
                    }
                    Map<String, Object> variables = buildFormulaVariables(
                            emploi, horaire, salaire, regimePaie, presence, plan.supplementStart);
					
                    BigDecimal montantCalcule = computeFormulaAmount(decision.formuleCode, variables, date);
					
                    if (montantCalcule == null || montantCalcule.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    SupplementaireEmploye supplementaire = new SupplementaireEmploye();
                    supplementaire.setEmploye(employe);
                    supplementaire.setEmploiEmploye(emploi);
                    supplementaire.setEntreprise(employe.getEntreprise());
                    supplementaire.setMemo("AUTO - " + decision.formuleCode);
                    supplementaire.setDateJour(hasPresence ? date : date);
                    supplementaire.setHeureDebut(resolveHeureDebut(presence, plan));
                    supplementaire.setHeureFin(resolveHeureFin(presence, plan));
                    supplementaire.setTypeSupplementaire(decision.typeSupplementaire);
                    supplementaire.setBaseCalcul(decision.baseCalcul);
                    supplementaire.setMontantBase(emploi.getTauxSupplementaire());
                    supplementaire.setDevise(devise);
                    supplementaire.setMontantCalcule(montantCalcule);
                    supplementaire.setNoPresence(hasPresence ? presence.noPresence : 0);
                    supplementaire.setDetails(buildDetailsJson(hasPresence, isConge, isOff, isFerie, plan, presence, montantCalcule));
                    supplementaire.setStatut(SupplementaireEmploye.StatutSupplementaire.BROUILLON);
                    supplementaire.setAutomatique("Y");
                    supplementaire.setCreatedBy(username);
                    supplementaire.setCreatedOn(OffsetDateTime.now());
                    supplementaire.setRowscn(1);

                    repository.save(supplementaire);
                    generatedCount++;
                }
            }
        }

        return new SupplementaireGenerationResultDTO(deletedCount, generatedCount);
    }

    @Transactional
    public SupplementaireValidationResultDTO validateSupplementaires(SupplementaireGenerationRequestDTO request, String username) {
        LocalDate dateDebut = LocalDate.parse(request.getDateDebut());
        LocalDate dateFin = LocalDate.parse(request.getDateFin());
        if (dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("Date fin must be after or equal to date debut");
        }

        int validatedCount = repository.validateDraftsInRange(
                dateDebut,
                dateFin,
                request.getEmployeId(),
                request.getEntrepriseId(),
                username,
                OffsetDateTime.now());
        return new SupplementaireValidationResultDTO(validatedCount);
    }
    
    private String generateDetailsJson(BigDecimal nbHeures, BigDecimal nbJours, BigDecimal nbNuits, 
                                       BigDecimal nbOffs, BigDecimal nbConges) {
        Map<String, Object> details = new HashMap<>();
        details.put("nb_heures", nbHeures != null ? nbHeures : BigDecimal.ZERO);
        details.put("nb_jours", nbJours != null ? nbJours : BigDecimal.ZERO);
        details.put("nb_nuits", nbNuits != null ? nbNuits : BigDecimal.ZERO);
        details.put("nb_offs", nbOffs != null ? nbOffs : BigDecimal.ZERO);
        details.put("nb_conges", nbConges != null ? nbConges : BigDecimal.ZERO);
        details.put("montant_heure_calcule", BigDecimal.ZERO);
        details.put("montant_jour_calcule", BigDecimal.ZERO);
        details.put("montant_nuit_calcule", BigDecimal.ZERO);
        details.put("montant_off_calcule", BigDecimal.ZERO);
        details.put("montant_conge_calcule", BigDecimal.ZERO);
        
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error generating details JSON", e);
        }
    }

    private String buildDetailsJson(boolean hasPresence,
                                    boolean isConge,
                                    boolean isOff,
                                    boolean isFerie,
                                    Planification plan,
                                    PresenceInfo presence,
                                    BigDecimal montantCalcule) {
        Map<String, Object> details = new HashMap<>();
        if (hasPresence && plan.supplementStart != null && presence != null) {
            details.put("nb_heures", computeOvertimeHours(presence, plan.supplementStart));
            details.put("montant_heure_calcule", montantCalcule);
        }
        if (isFerie) {
            details.put("nb_feries", BigDecimal.ONE);
            details.put("montant_ferie_calcule", montantCalcule);
        }
        if (plan.isNight) {
            details.put("nb_nuits", BigDecimal.ONE);
            details.put("montant_nuit_calcule", montantCalcule);
        }
        if (isOff) {
            details.put("nb_offs", BigDecimal.ONE);
            details.put("montant_off_calcule", montantCalcule);
        }
        if (isConge) {
            details.put("nb_conges", BigDecimal.ONE);
            details.put("montant_conge_calcule", montantCalcule);
        }
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error generating details JSON", e);
        }
    }

    private String resolveHeureDebut(PresenceInfo presence, Planification plan) {
        if (presence != null && presence.heureDebut != null) {
            return presence.heureDebut;
        }
        return plan.supplementStart != null ? plan.supplementStart : "00:00";
    }

    private String resolveHeureFin(PresenceInfo presence, Planification plan) {
        if (presence != null && presence.heureFin != null) {
            return presence.heureFin;
        }
        return plan.planEnd != null ? plan.planEnd : (plan.supplementStart != null ? plan.supplementStart : "00:00");
    }

    private List<Planification> resolvePlanifications(EmploiEmploye emploi, LocalDate date) {
        List<Planification> results = new ArrayList<>();
        List<HoraireSpecial> specials = horaireSpecialRepository.findActiveByEmployeIdAndDate(emploi.getEmploye().getId(), date);
        if (!specials.isEmpty()) {
            HoraireSpecial hs = specials.get(0);
            boolean isNight = hs.getDateFin() != null && hs.getDateFin().isAfter(hs.getDateDebut());
            results.add(new Planification(isNight, hs.getHeureFin(), hs.getHeureDebut(), hs.getHeureFin(), true));
            return results;
        }

        if (emploi.getHoraire() == null) {
            return results;
        }

        int dayIndex = getJourIndex(date.getDayOfWeek());
        HoraireDt horaireDt = horaireDtRepository.findByHoraireIdAndJour(emploi.getHoraire().getId(), dayIndex);
        if (horaireDt == null) {
            return results;
        }

        boolean hasDay = isNotBlank(horaireDt.getHeureDebutJour()) && isNotBlank(horaireDt.getHeureFinJour());
        boolean hasNight = isNotBlank(horaireDt.getHeureDebutNuit()) && isNotBlank(horaireDt.getHeureFinNuit());
        if (hasDay && hasNight && isOverlapping(horaireDt)) {
            hasNight = false;
        }

        if (hasDay) {
            results.add(new Planification(false, horaireDt.getHeureFinJour(), horaireDt.getHeureDebutJour(), horaireDt.getHeureFinJour(), false));
        }
        if (hasNight) {
            results.add(new Planification(true, horaireDt.getHeureDebutNuit(), horaireDt.getHeureDebutNuit(), horaireDt.getHeureFinNuit(), false));
        }
        return results;
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

    private boolean isOffDay(EmploiEmploye emploi, LocalDate date) {
        int dayIndex = getJourIndex(date.getDayOfWeek());
        return (emploi.getJourOff1() != null && emploi.getJourOff1() == dayIndex)
                || (emploi.getJourOff2() != null && emploi.getJourOff2() == dayIndex)
                || (emploi.getJourOff3() != null && emploi.getJourOff3() == dayIndex);
    }

    private int getJourIndex(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue(); // Monday=1 ... Sunday=7
    }

    private GenerationDecision decideFormula(boolean hasPresence,
                                             boolean isConge,
                                             boolean isOff,
                                             boolean isFerie,
                                             boolean isNightPlan,
                                             boolean fromHoraireSpecial,
                                             String montantFixe) {
        boolean montantFixeOui = "Y".equalsIgnoreCase(montantFixe);
        if (hasPresence) {
            if (isOff) {
                if (fromHoraireSpecial) {
                    return montantFixeOui
                            ? GenerationDecision.of("SUPP_JOUR_OFF_PLANIFIE_FIXE", SupplementaireEmploye.BaseCalcul.FIXE, SupplementaireEmploye.TypeSupplementaire.OFF)
                            : GenerationDecision.of("SUPP_JOUR_OFF_PLANIFIE", SupplementaireEmploye.BaseCalcul.TAUX_HORAIRE, SupplementaireEmploye.TypeSupplementaire.OFF);
                }
                if (isFerie) {
                    return montantFixeOui
                            ? GenerationDecision.of("SUPP_JOUR_OFF_FERIE_FIXE", SupplementaireEmploye.BaseCalcul.FIXE, SupplementaireEmploye.TypeSupplementaire.OFF)
                            : GenerationDecision.of("SUPP_JOUR_OFF_FERIE", SupplementaireEmploye.BaseCalcul.TAUX_HORAIRE, SupplementaireEmploye.TypeSupplementaire.OFF);
                }
                return montantFixeOui
                        ? GenerationDecision.of("SUPP_JOUR_OFF_FIXE", SupplementaireEmploye.BaseCalcul.FIXE, SupplementaireEmploye.TypeSupplementaire.OFF)
                        : GenerationDecision.of("SUPP_JOUR_OFF", SupplementaireEmploye.BaseCalcul.TAUX_HORAIRE, SupplementaireEmploye.TypeSupplementaire.OFF);
            }

            if (isNightPlan && isFerie) {
                return montantFixeOui
                        ? GenerationDecision.of("SUPP_SOIR_FERIE_FIXE", SupplementaireEmploye.BaseCalcul.FIXE, SupplementaireEmploye.TypeSupplementaire.NUIT)
                        : GenerationDecision.of("SUPP_SOIR_FERIE", SupplementaireEmploye.BaseCalcul.TAUX_HORAIRE, SupplementaireEmploye.TypeSupplementaire.NUIT);
            }

            if (isFerie) {
                return montantFixeOui
                        ? GenerationDecision.of("SUPP_JOUR_FERIE_FIXE", SupplementaireEmploye.BaseCalcul.FIXE, SupplementaireEmploye.TypeSupplementaire.FERIE)
                        : GenerationDecision.of("SUPP_JOUR_FERIE", SupplementaireEmploye.BaseCalcul.TAUX_HORAIRE, SupplementaireEmploye.TypeSupplementaire.FERIE);
            }

            if (isNightPlan) {
                return montantFixeOui
                        ? GenerationDecision.of("SUPP_SOIR_FIXE", SupplementaireEmploye.BaseCalcul.FIXE, SupplementaireEmploye.TypeSupplementaire.NUIT)
                        : GenerationDecision.of("SUPP_SOIR", SupplementaireEmploye.BaseCalcul.TAUX_HORAIRE, SupplementaireEmploye.TypeSupplementaire.NUIT);
            }

            if (!fromHoraireSpecial) {
                return GenerationDecision.of("SUPP_REGULIER", SupplementaireEmploye.BaseCalcul.TAUX_HORAIRE, SupplementaireEmploye.TypeSupplementaire.HEURE);
            }
        } else if (isConge) {
            if (isOff) {
                return null;
            }
            if (isFerie) {
                return isNightPlan
                        ? GenerationDecision.of("SUPP_JOUR_CONGE_FERIE_NUIT_FIXE", SupplementaireEmploye.BaseCalcul.FIXE, SupplementaireEmploye.TypeSupplementaire.CONGE)
                        : GenerationDecision.of("SUPP_JOUR_CONGE_FERIE_FIXE", SupplementaireEmploye.BaseCalcul.FIXE, SupplementaireEmploye.TypeSupplementaire.CONGE);
            }
			return isNightPlan
					? GenerationDecision.of("SUPP_JOUR_CONGE_NUIT_FIXE", SupplementaireEmploye.BaseCalcul.FIXE, SupplementaireEmploye.TypeSupplementaire.CONGE)
					: GenerationDecision.of("SUPP_JOUR_CONGE_FIXE", SupplementaireEmploye.BaseCalcul.FIXE, SupplementaireEmploye.TypeSupplementaire.CONGE);
        }
		
        if (!hasPresence && isFerie) {
            return isNightPlan
                    ? GenerationDecision.of("SUPP_AJUST_SOIR_FERIE", SupplementaireEmploye.BaseCalcul.TAUX_HORAIRE, SupplementaireEmploye.TypeSupplementaire.NUIT)
                    : GenerationDecision.of("SUPP_AJUST_JOUR_FERIE", SupplementaireEmploye.BaseCalcul.TAUX_HORAIRE, SupplementaireEmploye.TypeSupplementaire.FERIE);
        }
        return null;
    }

    private Map<String, Object> buildFormulaVariables(EmploiEmploye emploi,
                                                      Horaire horaire,
                                                      EmployeSalaire salaire,
                                                      RegimePaie regimePaie,
                                                      PresenceInfo presence,
                                                      String supplementStart) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("amt.ot.fix", horaire != null ? horaire.getMontantFixe() : "N");
        variables.put("nb.ot.d", horaire != null && horaire.getDefaultNbHovertime() != null ? horaire.getDefaultNbHovertime() : 0);
        variables.put("h.ot.d", computeOvertimeHours(presence, supplementStart));
        variables.put("amt.ot.hr", emploi.getTauxSupplementaire() != null ? emploi.getTauxSupplementaire() : BigDecimal.ZERO);
        variables.put("coef.ot", horaire != null && horaire.getMontantHeureSup() != null ? horaire.getMontantHeureSup() : BigDecimal.ZERO);
        variables.put("coef.off", horaire != null && horaire.getCoeffSuppOff() != null ? horaire.getCoeffSuppOff() : BigDecimal.ZERO);
        variables.put("coef.hol", horaire != null && horaire.getCoeffSuppJourFerie() != null ? horaire.getCoeffSuppJourFerie() : BigDecimal.ZERO);
        variables.put("coef.eve", horaire != null && horaire.getCoeffSoir() != null ? horaire.getCoeffSoir() : BigDecimal.ZERO);

        SalaryAmounts salaryAmounts = computeSalaryAmounts(salaire, regimePaie);
        variables.put("amt.sal", salaryAmounts.montant);
        variables.put("amt.sal.hr", salaryAmounts.hourly);
        variables.put("amt.sal.d", salaryAmounts.daily);
        variables.put("amt.sal.w", salaryAmounts.weekly);
        variables.put("amt.sal.pp", salaryAmounts.payPeriod);
        variables.put("amt.sal.pq", salaryAmounts.payQuinzaine);
        variables.put("amt.sal.m", salaryAmounts.monthly);
        variables.put("amt.sal.y", salaryAmounts.yearly);
        return variables;
    }

    private BigDecimal computeFormulaAmount(String formulaCode, Map<String, Object> variables, LocalDate date) {
        Formule formule = formuleRepository.findByCodeVariable(formulaCode).orElse(null);
        if (formule == null || !"Y".equalsIgnoreCase(formule.getActif())) {
            return null;
        }
        if (formule.getDateEffectif() != null && date.isBefore(formule.getDateEffectif())) {
            return null;
        }
        if (formule.getDateFin() != null && !formule.getDateFin().isBefore(LocalDate.now())) {
            return null;
        }
        String expression = normalizeExpression(formule.getExpression());
        if (!isNotBlank(expression)) {
            return null;
        }
        try {
            return formulaEvaluator.evaluate(expression, variables, false);
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeExpression(String expression) {
        if (expression == null) {
            return null;
        }
        return expression.replaceAll("\\$\\{([^}]+)\\}", "$1");
    }


    private BigDecimal computeOvertimeHours(PresenceInfo presence, String supplementStart) {
        if (presence == null || !isNotBlank(presence.heureFin) || !isNotBlank(supplementStart)) {
            return BigDecimal.ZERO;
        }
        try {
            LocalTime start = LocalTime.parse(supplementStart);
            LocalTime end = LocalTime.parse(presence.heureFin);
            long minutes = Duration.between(start, end).toMinutes();
            if (minutes < 0) {
                minutes += 24 * 60;
            }
            return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private SalaryAmounts computeSalaryAmounts(EmployeSalaire salaire, RegimePaie regimePaie) {
        if (salaire == null || salaire.getMontant() == null) {
            return SalaryAmounts.zero();
        }
        BigDecimal montant = salaire.getMontant();
        RegimePaie.Periodicite periodicite = regimePaie != null ? regimePaie.getPeriodicite() : null;
        BigDecimal daily = montant;
        BigDecimal weekly = montant;
        BigDecimal monthly = montant;
        BigDecimal yearly = montant;

        if (periodicite != null) {
            switch (periodicite) {
                case JOURNALIER:
                    daily = montant;
                    weekly = daily.multiply(BigDecimal.valueOf(7));
                    monthly = daily.multiply(BigDecimal.valueOf(30));
                    yearly = daily.multiply(BigDecimal.valueOf(365));
                    break;
                case HEBDO:
                    weekly = montant;
                    daily = weekly.divide(BigDecimal.valueOf(7), 6, RoundingMode.HALF_UP);
                    monthly = weekly.multiply(BigDecimal.valueOf(4));
                    yearly = weekly.multiply(BigDecimal.valueOf(52));
                    break;
                case QUINZAINE:
                    daily = montant.divide(BigDecimal.valueOf(14), 6, RoundingMode.HALF_UP);
                    weekly = daily.multiply(BigDecimal.valueOf(7));
                    monthly = daily.multiply(BigDecimal.valueOf(30));
                    yearly = daily.multiply(BigDecimal.valueOf(365));
                    break;
                case QUINZOMADAIRE:
                    daily = montant.divide(BigDecimal.valueOf(15), 6, RoundingMode.HALF_UP);
                    weekly = daily.multiply(BigDecimal.valueOf(7));
                    monthly = daily.multiply(BigDecimal.valueOf(30));
                    yearly = daily.multiply(BigDecimal.valueOf(365));
                    break;
                case TRIMESTRIEL:
                    monthly = montant.divide(BigDecimal.valueOf(3), 6, RoundingMode.HALF_UP);
                    yearly = montant.multiply(BigDecimal.valueOf(4));
                    daily = yearly.divide(BigDecimal.valueOf(365), 6, RoundingMode.HALF_UP);
                    weekly = daily.multiply(BigDecimal.valueOf(7));
                    break;
                case SEMESTRIEL:
                    monthly = montant.divide(BigDecimal.valueOf(6), 6, RoundingMode.HALF_UP);
                    yearly = montant.multiply(BigDecimal.valueOf(2));
                    daily = yearly.divide(BigDecimal.valueOf(365), 6, RoundingMode.HALF_UP);
                    weekly = daily.multiply(BigDecimal.valueOf(7));
                    break;
                case ANNUEL:
                    yearly = montant;
                    monthly = yearly.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
                    daily = yearly.divide(BigDecimal.valueOf(365), 6, RoundingMode.HALF_UP);
                    weekly = daily.multiply(BigDecimal.valueOf(7));
                    break;
                default:
                    break;
            }
        }

        BigDecimal hourly = daily.divide(BigDecimal.valueOf(8), 6, RoundingMode.HALF_UP);
        return new SalaryAmounts(montant, hourly, daily, weekly, monthly, yearly,
                montant, montant);
    }

    private PresenceInfo findLastPresence(Long employeId, LocalDate date) {
        String sqlA = "SELECT date_jour, heure_arrivee, heure_depart, 0 AS no_presence " +
                      "FROM presence_employe WHERE employe_id = ? AND date_jour = ? " +
                      "AND statut_presence = 'VALIDE' " +
                      "ORDER BY id DESC LIMIT 1";
        String sqlB = "SELECT date_jour, heure_entree, heure_sortie, no_presence " +
                      "FROM presence_employe WHERE employe_id = ? AND date_jour = ? " +
                      "ORDER BY no_presence DESC LIMIT 1";
        try {
            return jdbcTemplate.query(sqlA, rs -> {
                if (rs.next()) {
                    return new PresenceInfo(
                            rs.getString("heure_arrivee"),
                            rs.getString("heure_depart"),
                            rs.getInt("no_presence"));
                }
                return null;
            }, employeId, date);
        } catch (DataAccessException ex) {
            try {
                return jdbcTemplate.query(sqlB, rs -> {
                    if (rs.next()) {
                        return new PresenceInfo(
                                rs.getString("heure_entree"),
                                rs.getString("heure_sortie"),
                                rs.getInt("no_presence"));
                    }
                    return null;
                }, employeId, date);
            } catch (DataAccessException ignored) {
                return null;
            }
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static class PresenceInfo {
        private final String heureDebut;
        private final String heureFin;
        private final int noPresence;

        private PresenceInfo(String heureDebut, String heureFin, int noPresence) {
            this.heureDebut = heureDebut;
            this.heureFin = heureFin;
            this.noPresence = noPresence;
        }
    }

    private static class Planification {
        private final boolean isNight;
        private final String supplementStart;
        private final String planStart;
        private final String planEnd;
        private final boolean fromHoraireSpecial;

        private Planification(boolean isNight, String supplementStart, String planStart, String planEnd, boolean fromHoraireSpecial) {
            this.isNight = isNight;
            this.supplementStart = supplementStart;
            this.planStart = planStart;
            this.planEnd = planEnd;
            this.fromHoraireSpecial = fromHoraireSpecial;
        }
    }

    private static class GenerationDecision {
        private final String formuleCode;
        private final SupplementaireEmploye.BaseCalcul baseCalcul;
        private final SupplementaireEmploye.TypeSupplementaire typeSupplementaire;

        private GenerationDecision(String formuleCode,
                                   SupplementaireEmploye.BaseCalcul baseCalcul,
                                   SupplementaireEmploye.TypeSupplementaire typeSupplementaire) {
            this.formuleCode = formuleCode;
            this.baseCalcul = baseCalcul;
            this.typeSupplementaire = typeSupplementaire;
        }

        private static GenerationDecision of(String formuleCode,
                                             SupplementaireEmploye.BaseCalcul baseCalcul,
                                             SupplementaireEmploye.TypeSupplementaire typeSupplementaire) {
            return new GenerationDecision(formuleCode, baseCalcul, typeSupplementaire);
        }
    }

    private static class SalaryAmounts {
        private final BigDecimal montant;
        private final BigDecimal hourly;
        private final BigDecimal daily;
        private final BigDecimal weekly;
        private final BigDecimal monthly;
        private final BigDecimal yearly;
        private final BigDecimal payPeriod;
        private final BigDecimal payQuinzaine;

        private SalaryAmounts(BigDecimal montant,
                              BigDecimal hourly,
                              BigDecimal daily,
                              BigDecimal weekly,
                              BigDecimal monthly,
                              BigDecimal yearly,
                              BigDecimal payPeriod,
                              BigDecimal payQuinzaine) {
            this.montant = montant;
            this.hourly = hourly;
            this.daily = daily;
            this.weekly = weekly;
            this.monthly = monthly;
            this.yearly = yearly;
            this.payPeriod = payPeriod;
            this.payQuinzaine = payQuinzaine;
        }

        private static SalaryAmounts zero() {
            BigDecimal zero = BigDecimal.ZERO;
            return new SalaryAmounts(zero, zero, zero, zero, zero, zero, zero, zero);
        }
    }
    
    private SupplementaireEmployeDTO toDTO(SupplementaireEmploye entity) {
        SupplementaireEmployeDTO dto = new SupplementaireEmployeDTO();
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
        if (entity.getEmploiEmploye() != null) {
            dto.setEmploiEmployeId(entity.getEmploiEmploye().getId());
        }
        
        dto.setMemo(entity.getMemo());
        dto.setDateJour(entity.getDateJour());
        dto.setHeureDebut(entity.getHeureDebut());
        dto.setHeureFin(entity.getHeureFin());
        dto.setTypeSupplementaire(entity.getTypeSupplementaire() != null ? entity.getTypeSupplementaire().name() : null);
        dto.setBaseCalcul(entity.getBaseCalcul() != null ? entity.getBaseCalcul().name() : null);
        dto.setMontantBase(entity.getMontantBase());
        
        if (entity.getDevise() != null) {
            dto.setDeviseId(entity.getDevise().getId());
            dto.setDeviseCode(entity.getDevise().getCodeDevise());
            dto.setDeviseDescription(entity.getDevise().getDescription());
        }
        
        dto.setMontantCalcule(entity.getMontantCalcule());
        dto.setAutomatique(entity.getAutomatique());
        dto.setDetails(entity.getDetails()); // JSON string
        dto.setStatut(entity.getStatut() != null ? entity.getStatut().name() : null);
        
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        return dto;
    }

    private EmploiEmploye resolveEmploiEmploye(Long employeId, Long emploiEmployeId) {
        if (employeId == null) {
            return null;
        }
        if (emploiEmployeId != null) {
            EmploiEmploye emploi = emploiEmployeRepository.findById(emploiEmployeId)
                    .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiEmployeId));
            if (!emploi.getEmploye().getId().equals(employeId)) {
                throw new RuntimeException("EmploiEmploye does not belong to employe " + employeId);
            }
            if (emploi.getStatutEmploi() == EmploiEmploye.StatutEmploi.TERMINE) {
                throw new RuntimeException("EmploiEmploye is terminated and cannot be used.");
            }
            return emploi;
        }

        List<EmploiEmploye> emplois = emploiEmployeRepository.findByEmployeIdAndStatutEmploiNot(
                employeId,
                EmploiEmploye.StatutEmploi.TERMINE);
        if (emplois.isEmpty()) {
            throw new RuntimeException("No non-terminated emploi found for employe " + employeId);
        }
        if (emplois.size() > 1) {
            throw new RuntimeException("Multiple non-terminated emplois found for employe " + employeId + ". Please specify emploiEmployeId.");
        }
        return emplois.get(0);
    }
}
