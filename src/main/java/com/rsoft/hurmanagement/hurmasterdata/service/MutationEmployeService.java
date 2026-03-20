package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.MutationEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.MutationEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.MutationEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.EmploiEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MutationEmployeService {
    
    private final MutationEmployeRepository repository;
    private final EmployeRepository employeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final EmployeSalaireRepository employeSalaireRepository;
    private final RegimePaieRepository regimePaieRepository;
    private final TypeEmployeRepository typeEmployeRepository;
    private final UniteOrganisationnelleRepository uniteOrganisationnelleRepository;
    private final PosteRepository posteRepository;
    private final HoraireRepository horaireRepository;
    private final FonctionRepository fonctionRepository;
    private final TypeCongeRepository typeCongeRepository;
    
    @Transactional(readOnly = true)
    public Page<MutationEmployeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<MutationEmployeDTO> findAllWithFilters(Long employeId, String typeMutation, String statut, LocalDate dateDebut, LocalDate dateFin, Pageable pageable) {
        MutationEmploye.TypeMutation typeEnum = null;
        if (typeMutation != null && !typeMutation.trim().isEmpty()) {
            try {
                typeEnum = MutationEmploye.TypeMutation.valueOf(typeMutation);
            } catch (IllegalArgumentException e) {
                // Invalid type, will be ignored
            }
        }
        
        MutationEmploye.StatutMutation statutEnum = null;
        if (statut != null && !statut.trim().isEmpty()) {
            try {
                statutEnum = MutationEmploye.StatutMutation.valueOf(statut);
            } catch (IllegalArgumentException e) {
                // Invalid statut, will be ignored
            }
        }
        
        return repository.findAllWithFilters(employeId, typeEnum, statutEnum, dateDebut, dateFin, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public MutationEmployeDTO findById(Long id) {
        MutationEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("MutationEmploye not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public MutationEmployeDTO create(MutationEmployeCreateDTO dto, String username) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        
        MutationEmploye.TypeMutation typeMutation = MutationEmploye.TypeMutation.valueOf(dto.getTypeMutation());
        if (typeMutation == MutationEmploye.TypeMutation.NOMINATION) {
            validateNominationEligibility(employe);
        }
        validateEmploiSelection(typeMutation, dto.getEmploiEmployeAvantId(), dto.getEmploiEmployeApresId());
        if (typeMutation == MutationEmploye.TypeMutation.LICENCIEMENT && (dto.getMotif() == null || dto.getMotif().trim().isEmpty())) {
            throw new RuntimeException("Motif is required for LICENCIEMENT");
        }

        MutationEmploye entity = new MutationEmploye();
        entity.setEmploye(employe);
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        }
        
        entity.setTypeMutation(typeMutation);
        entity.setDateEffet(dto.getDateEffet());
        entity.setDateSaisie(dto.getDateSaisie() != null ? dto.getDateSaisie() : LocalDate.now());
        entity.setStatut(dto.getStatut() != null ? MutationEmploye.StatutMutation.valueOf(dto.getStatut()) : MutationEmploye.StatutMutation.BROUILLON);
        entity.setMotif(dto.getMotif());
        entity.setReference(dto.getReference());
        
        // Generate avant/apres JSON based on mutation type
        String avantJson = genererAvant(dto.getEmployeId(), dto.getTypeMutation(), dto);
        String apresJson = genererApres(dto.getEmployeId(), dto.getTypeMutation(), dto);
        entity.setAvant(avantJson);
        entity.setApres(apresJson);
        
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public MutationEmployeDTO update(Long id, MutationEmployeUpdateDTO dto, String username) {
        MutationEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("MutationEmploye not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Cannot update if already APPLIQUE or ANNULE
        if (entity.getStatut() == MutationEmploye.StatutMutation.APPLIQUE || 
            entity.getStatut() == MutationEmploye.StatutMutation.ANNULE) {
            throw new RuntimeException("Cannot update mutation with status: " + entity.getStatut());
        }
        
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        entity.setEmploye(employe);
        
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));
            entity.setEntreprise(entreprise);
        } else {
            entity.setEntreprise(null);
        }
        
        MutationEmploye.TypeMutation typeMutation = MutationEmploye.TypeMutation.valueOf(dto.getTypeMutation());
        if (typeMutation == MutationEmploye.TypeMutation.NOMINATION) {
            validateNominationEligibility(employe);
        }
        validateEmploiSelection(typeMutation, dto.getEmploiEmployeAvantId(), dto.getEmploiEmployeApresId());
        if (typeMutation == MutationEmploye.TypeMutation.LICENCIEMENT && (dto.getMotif() == null || dto.getMotif().trim().isEmpty())) {
            throw new RuntimeException("Motif is required for LICENCIEMENT");
        }
        entity.setTypeMutation(typeMutation);
        entity.setDateEffet(dto.getDateEffet());
        entity.setDateSaisie(dto.getDateSaisie());
        entity.setStatut(MutationEmploye.StatutMutation.valueOf(dto.getStatut()));
        entity.setMotif(dto.getMotif());
        entity.setReference(dto.getReference());
        if (dto.getAvant() != null) {
            entity.setAvant(dto.getAvant());
        }
        if (dto.getApres() != null) {
            entity.setApres(dto.getApres());
        }
        
        // Regenerate avant/apres JSON if mutation type changed
        // For now, keep existing JSON - can be regenerated if mutation type or data changed
        // TODO: Regenerate JSON if mutation type or key fields changed
        
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        MutationEmploye entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("MutationEmploye not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Can only delete if status is BROUILLON
        if (entity.getStatut() != MutationEmploye.StatutMutation.BROUILLON) {
            throw new RuntimeException("Cannot delete mutation with status: " + entity.getStatut() + ". Only BROUILLON mutations can be deleted.");
        }
        
        repository.delete(entity);
    }
    
    @Transactional
    public MutationEmployeDTO appliquer(Long id, String username) {
        MutationEmploye mutation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("MutationEmploye not found with id: " + id));
        
        if (mutation.getTypeMutation() == MutationEmploye.TypeMutation.NOMINATION ||
                mutation.getTypeMutation() == MutationEmploye.TypeMutation.REVISION_SALAIRE ||
                mutation.getTypeMutation() == MutationEmploye.TypeMutation.AJOUT_REGIME_PAIE ||
                mutation.getTypeMutation() == MutationEmploye.TypeMutation.CHG_POSTE ||
                mutation.getTypeMutation() == MutationEmploye.TypeMutation.CHG_UNITE ||
                mutation.getTypeMutation() == MutationEmploye.TypeMutation.PROMOTION ||
                mutation.getTypeMutation() == MutationEmploye.TypeMutation.LICENCIEMENT ||
                mutation.getTypeMutation() == MutationEmploye.TypeMutation.FIN_CONTRAT ||
                mutation.getTypeMutation() == MutationEmploye.TypeMutation.ABANDON_POSTE ||
                mutation.getTypeMutation() == MutationEmploye.TypeMutation.SUSPENSION ||
                mutation.getTypeMutation() == MutationEmploye.TypeMutation.REINTEGRATION) {
            if (mutation.getStatut() != MutationEmploye.StatutMutation.BROUILLON && 
                mutation.getStatut() != MutationEmploye.StatutMutation.APPROUVE && 
                mutation.getStatut() != MutationEmploye.StatutMutation.SOUMIS) {
                throw new RuntimeException("Cannot apply mutation with status: " + mutation.getStatut());
            }
        } else if (mutation.getStatut() != MutationEmploye.StatutMutation.APPROUVE &&
                mutation.getStatut() != MutationEmploye.StatutMutation.SOUMIS) {
            throw new RuntimeException("Cannot apply mutation with status: " + mutation.getStatut() + ". Only APPROUVE or SOUMIS mutations can be applied.");
        }
        
        // Apply mutation based on type
        appliquerMutation(mutation);
        
        mutation.setStatut(MutationEmploye.StatutMutation.APPLIQUE);
        mutation.setUpdatedBy(username);
        mutation.setUpdatedOn(OffsetDateTime.now());
        
        return toDTO(repository.save(mutation));
    }
    
    @Transactional
    public MutationEmployeDTO annuler(Long id, String username) {
        MutationEmploye mutation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("MutationEmploye not found with id: " + id));
        
        if (mutation.getStatut() != MutationEmploye.StatutMutation.APPLIQUE) {
            throw new RuntimeException("Cannot cancel mutation with status: " + mutation.getStatut() + ". Only APPLIQUE mutations can be cancelled.");
        }
        
        // Cancel mutation - restore previous state
        annulerMutation(mutation);
        
        mutation.setStatut(MutationEmploye.StatutMutation.ANNULE);
        mutation.setUpdatedBy(username);
        mutation.setUpdatedOn(OffsetDateTime.now());
        
        return toDTO(repository.save(mutation));
    }

    @Transactional
    public Map<String, Object> autoApplyApprovedForCurrentDate(Long entrepriseId, String username) {
        LocalDate today = LocalDate.now();
        List<MutationEmploye> mutations = repository.findByStatutAndDateEffetLessThanEqual(
                MutationEmploye.StatutMutation.APPROUVE,
                today
        );
        int totalRows = 0;
        int appliedRows = 0;
        for (MutationEmploye mutation : mutations) {
            if (entrepriseId != null) {
                Long mutationEntrepriseId = mutation.getEntreprise() != null ? mutation.getEntreprise().getId() : null;
                if (!entrepriseId.equals(mutationEntrepriseId)) {
                    continue;
                }
            }
            totalRows++;
            appliquer(mutation.getId(), username);
            appliedRows++;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalRows", totalRows);
        result.put("appliedRows", appliedRows);
        result.put("message", "Applied " + appliedRows + " mutations");
        return result;
    }

    @Transactional
    public Map<String, Object> autoCreateAndApplyReintegrationForExpiredSuspensions(Long entrepriseId, String username) {
        LocalDate today = LocalDate.now();
        List<EmploiEmploye> emplois = emploiEmployeRepository.findByStatutEmploiAndDateFinStatutLessThanEqual(
                EmploiEmploye.StatutEmploi.SUSPENDU,
                today
        );
        int totalRows = 0;
        int createdRows = 0;
        int appliedRows = 0;
        for (EmploiEmploye emploi : emplois) {
            if (emploi == null || emploi.getEmploye() == null || emploi.getEmploye().getId() == null) {
                continue;
            }
            if (entrepriseId != null) {
                Long emploiEntrepriseId = emploi.getEmploye().getEntreprise() != null
                        ? emploi.getEmploye().getEntreprise().getId()
                        : null;
                if (!entrepriseId.equals(emploiEntrepriseId)) {
                    continue;
                }
            }
            totalRows++;
            MutationEmployeCreateDTO dto = new MutationEmployeCreateDTO();
            dto.setEmployeId(emploi.getEmploye().getId());
            dto.setEntrepriseId(emploi.getEmploye().getEntreprise() != null ? emploi.getEmploye().getEntreprise().getId() : null);
            dto.setTypeMutation(MutationEmploye.TypeMutation.REINTEGRATION.name());
            dto.setDateEffet(emploi.getDateFinStatut() != null ? emploi.getDateFinStatut() : today);
            dto.setDateSaisie(today);
            dto.setStatut(MutationEmploye.StatutMutation.BROUILLON.name());
            dto.setMotif("Reintegration automatique");
            dto.setReference("AUTO-REINTEGRATION-" + emploi.getId() + "-" + today);
            dto.setEmploiEmployeAvantId(emploi.getId());
            dto.setEmploiEmployeApresId(emploi.getId());

            MutationEmployeDTO created = create(dto, username);
            createdRows++;
            appliquer(created.getId(), username);
            appliedRows++;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalRows", totalRows);
        result.put("createdRows", createdRows);
        result.put("appliedRows", appliedRows);
        result.put("message", "Created and applied " + appliedRows + " reintegrations");
        return result;
    }
    
    @Transactional(readOnly = true)
    public List<EmploiEmployeDTO> getEmploisDisponibles(Long employeId, String typeMutation) {
        List<EmploiEmploye> emplois = emploiEmployeRepository.findByEmployeId(employeId);
        
        List<EmploiEmploye> filteredEmplois;
        if (typeMutation == null) {
            filteredEmplois = emplois;
        } else {
            MutationEmploye.TypeMutation type = MutationEmploye.TypeMutation.valueOf(typeMutation);
            
            switch (type) {
                case NOMINATION:
                    filteredEmplois = List.of();
                    break;
                case SUSPENSION:
                    // Only ACTIF emplois
                    filteredEmplois = emplois.stream()
                        .filter(e -> e.getStatutEmploi() == EmploiEmploye.StatutEmploi.ACTIF)
                        .collect(Collectors.toList());
                    break;
                        
                case REINTEGRATION:
                    // Only SUSPENDU emplois
                    filteredEmplois = emplois.stream()
                        .filter(e -> e.getStatutEmploi() == EmploiEmploye.StatutEmploi.SUSPENDU)
                        .collect(Collectors.toList());
                    break;
                        
                case DEMISSION:
                case LICENCIEMENT:
                case FIN_CONTRAT:
                case ABANDON_POSTE:
                case CHG_POSTE:
                case CHG_UNITE:
                case PROMOTION:
                case AJOUT_REGIME_PAIE:
                    // ACTIF or SUSPENDU
                    filteredEmplois = emplois.stream()
                        .filter(e -> e.getStatutEmploi() == EmploiEmploye.StatutEmploi.ACTIF || 
                                    e.getStatutEmploi() == EmploiEmploye.StatutEmploi.SUSPENDU)
                        .collect(Collectors.toList());
                    break;
                        
                default:
                    filteredEmplois = emplois;
            }
        }
        
        return filteredEmplois.stream()
            .map(this::toEmploiEmployeDTO)
            .collect(Collectors.toList());
    }
    
    // Private helper methods
    
    private String genererAvant(Long employeId, String typeMutation, MutationEmployeCreateDTO dto) {
        try {
            MutationEmploye.TypeMutation type = MutationEmploye.TypeMutation.valueOf(typeMutation);
            Map<String, Object> avant = new HashMap<>();
            
            switch (type) {
                case NOMINATION:
                    break;
                case DEMISSION:
                case LICENCIEMENT:
                case FIN_CONTRAT:
                case ABANDON_POSTE:
                case SUSPENSION:
                case REINTEGRATION:
                    if (dto.getEmploiEmployeAvantId() != null) {
                        EmploiEmploye emploi = emploiEmployeRepository.findById(dto.getEmploiEmployeAvantId())
                            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + dto.getEmploiEmployeAvantId()));
                        avant.put("emploi_employe_id", emploi.getId());
                        avant.put("statut_emploi", emploi.getStatutEmploi() != null ? emploi.getStatutEmploi().name() : null);
                    }
                    break;
                    
                case REVISION_SALAIRE:
                    // Get active/suspended emploi and salaire
                    Optional<EmploiEmploye> emploiActif;
                    if (dto.getEmploiEmployeAvantId() != null) {
                        EmploiEmploye emploi = emploiEmployeRepository.findById(dto.getEmploiEmployeAvantId())
                            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + dto.getEmploiEmployeAvantId()));
                        emploiActif = Optional.of(emploi);
                    } else {
                        emploiActif = emploiEmployeRepository.findByEmployeId(employeId).stream()
                            .filter(e -> e.getStatutEmploi() == EmploiEmploye.StatutEmploi.ACTIF || 
                                        e.getStatutEmploi() == EmploiEmploye.StatutEmploi.SUSPENDU)
                            .max((e1, e2) -> e1.getDateDebut().compareTo(e2.getDateDebut()));
                    }
                    
                    if (emploiActif.isPresent()) {
                        EmploiEmploye emploi = emploiActif.get();
                        avant.put("emploi_employe_id", emploi.getId());
                        avant.put("taux_supplementaire", emploi.getTauxSupplementaire());
                        
                        Optional<EmployeSalaire> salaire;
                        if (dto.getSalaireEmployeId() != null) {
                            EmployeSalaire salaireEmploye = employeSalaireRepository.findById(dto.getSalaireEmployeId())
                                .orElseThrow(() -> new RuntimeException("EmployeSalaire not found with id: " + dto.getSalaireEmployeId()));
                            salaire = Optional.of(salaireEmploye);
                        } else {
                            salaire = employeSalaireRepository.findByEmploiId(emploi.getId()).stream()
                                .filter(s -> "Y".equalsIgnoreCase(s.getActif()))
                                .findFirst();
                        }
                        if (salaire.isPresent()) {
                            avant.put("montant_salaire", salaire.get().getMontant());
                            avant.put("salaire_employe_id", salaire.get().getId());
                        }
                    }
                    break;
                case AJOUT_REGIME_PAIE:
                    Optional<EmploiEmploye> emploiRegime;
                    if (dto.getEmploiEmployeAvantId() != null) {
                        EmploiEmploye emploi = emploiEmployeRepository.findById(dto.getEmploiEmployeAvantId())
                            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + dto.getEmploiEmployeAvantId()));
                        emploiRegime = Optional.of(emploi);
                    } else {
                        emploiRegime = emploiEmployeRepository.findByEmployeId(employeId).stream()
                            .filter(e -> e.getStatutEmploi() == EmploiEmploye.StatutEmploi.ACTIF || 
                                        e.getStatutEmploi() == EmploiEmploye.StatutEmploi.SUSPENDU)
                            .max((e1, e2) -> e1.getDateDebut().compareTo(e2.getDateDebut()));
                    }
                    
                    if (emploiRegime.isPresent()) {
                        EmploiEmploye emploi = emploiRegime.get();
                        avant.put("emploi_employe_id", emploi.getId());
                        
                        Optional<EmployeSalaire> salaire;
                        if (dto.getSalaireEmployeId() != null) {
                            EmployeSalaire salaireEmploye = employeSalaireRepository.findById(dto.getSalaireEmployeId())
                                .orElseThrow(() -> new RuntimeException("EmployeSalaire not found with id: " + dto.getSalaireEmployeId()));
                            salaire = Optional.of(salaireEmploye);
                        } else {
                            salaire = employeSalaireRepository.findByEmploiId(emploi.getId()).stream()
                                .filter(s -> "Y".equalsIgnoreCase(s.getActif()))
                                .findFirst();
                        }
                        if (salaire.isPresent()) {
                            avant.put("salaire_employe_id", salaire.get().getId());
                            avant.put("regime_paie_id", salaire.get().getRegimePaie() != null ? salaire.get().getRegimePaie().getId() : null);
                            avant.put("montant_salaire", salaire.get().getMontant());
                        }
                    }
                    break;
                case CHG_POSTE:
                case CHG_UNITE:
                case PROMOTION:
                    Optional<EmploiEmploye> emploiChg;
                    if (dto.getEmploiEmployeAvantId() != null) {
                        EmploiEmploye emploi = emploiEmployeRepository.findById(dto.getEmploiEmployeAvantId())
                            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + dto.getEmploiEmployeAvantId()));
                        emploiChg = Optional.of(emploi);
                    } else {
                        emploiChg = emploiEmployeRepository.findByEmployeId(employeId).stream()
                            .filter(e -> e.getStatutEmploi() == EmploiEmploye.StatutEmploi.ACTIF ||
                                        e.getStatutEmploi() == EmploiEmploye.StatutEmploi.SUSPENDU)
                            .max((e1, e2) -> e1.getDateDebut().compareTo(e2.getDateDebut()));
                    }

                    if (emploiChg.isPresent()) {
                        EmploiEmploye emploi = emploiChg.get();
                        avant.put("emploi_employe_id", emploi.getId());
                        avant.put("type_employe_id", emploi.getTypeEmploye() != null ? emploi.getTypeEmploye().getId() : null);
                        avant.put("unite_organisationnelle_id", emploi.getUniteOrganisationnelle() != null ? emploi.getUniteOrganisationnelle().getId() : null);
                        avant.put("poste_id", emploi.getPoste() != null ? emploi.getPoste().getId() : null);
                        avant.put("type_contrat", emploi.getTypeContrat() != null ? emploi.getTypeContrat().name() : null);
                        avant.put("temps_travail", emploi.getTempsTravail() != null ? emploi.getTempsTravail().name() : null);
                        avant.put("horaire_id", emploi.getHoraire() != null ? emploi.getHoraire().getId() : null);
                        avant.put("fonction_id", emploi.getFonction() != null ? emploi.getFonction().getId() : null);
                        avant.put("type_conge_id", emploi.getTypeConge() != null ? emploi.getTypeConge().getId() : null);
                        avant.put("gestionnaire_id", emploi.getGestionnaire() != null ? emploi.getGestionnaire().getId() : null);
                        avant.put("taux_supplementaire", emploi.getTauxSupplementaire());
                        avant.put("statut_emploi", emploi.getStatutEmploi() != null ? emploi.getStatutEmploi().name() : null);

                        Optional<EmployeSalaire> salaire;
                        if (dto.getSalaireEmployeId() != null) {
                            EmployeSalaire salaireEmploye = employeSalaireRepository.findById(dto.getSalaireEmployeId())
                                .orElseThrow(() -> new RuntimeException("EmployeSalaire not found with id: " + dto.getSalaireEmployeId()));
                            salaire = Optional.of(salaireEmploye);
                        } else {
                            salaire = employeSalaireRepository.findByEmploiId(emploi.getId()).stream()
                                .filter(s -> "Y".equalsIgnoreCase(s.getActif()))
                                .findFirst();
                        }
                        if (salaire.isPresent()) {
                            avant.put("salaire_employe_id", salaire.get().getId());
                            avant.put("regime_paie_id", salaire.get().getRegimePaie() != null ? salaire.get().getRegimePaie().getId() : null);
                            avant.put("montant_salaire", salaire.get().getMontant());
                        }
                    }
                    break;
                    
                default:
                    break;
            }
            
            // Simple JSON serialization
            return mapToJson(avant);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private String genererApres(Long employeId, String typeMutation, MutationEmployeCreateDTO dto) {
        try {
            MutationEmploye.TypeMutation type = MutationEmploye.TypeMutation.valueOf(typeMutation);
            Map<String, Object> apres = new HashMap<>();
            
            switch (type) {
                case NOMINATION:
                    break;
                case DEMISSION:
                    if (dto.getEmploiEmployeAvantId() != null) {
                        apres.put("emploi_employe_id", dto.getEmploiEmployeAvantId());
                        apres.put("statut_emploi", "TERMINE"); // Using TERMINE as DEMISSIONNE doesn't exist in enum
                    }
                    break;
                    
                case LICENCIEMENT:
                    if (dto.getEmploiEmployeAvantId() != null) {
                        apres.put("emploi_employe_id", dto.getEmploiEmployeAvantId());
                        apres.put("statut_emploi", "LICENCIE");
                    }
                    break;
                    
                case FIN_CONTRAT:
                    if (dto.getEmploiEmployeAvantId() != null) {
                        apres.put("emploi_employe_id", dto.getEmploiEmployeAvantId());
                        apres.put("statut_emploi", "TERMINE");
                    }
                    break;
                    
                case ABANDON_POSTE:
                    if (dto.getEmploiEmployeAvantId() != null) {
                        apres.put("emploi_employe_id", dto.getEmploiEmployeAvantId());
                        apres.put("statut_emploi", "ABANDONNE");
                    }
                    break;
                    
                case SUSPENSION:
                    if (dto.getEmploiEmployeAvantId() != null) {
                        apres.put("emploi_employe_id", dto.getEmploiEmployeAvantId());
                        apres.put("statut_emploi", "SUSPENDU");
                    }
                    break;
                    
                case REINTEGRATION:
                    if (dto.getEmploiEmployeApresId() != null) {
                        apres.put("emploi_employe_id", dto.getEmploiEmployeApresId());
                        apres.put("statut_emploi", "ACTIF");
                    }
                    break;
                    
                case REVISION_SALAIRE:
                    if (dto.getEmploiEmployeAvantId() != null) {
                        apres.put("emploi_employe_id", dto.getEmploiEmployeAvantId());
                    }
                    if (dto.getMontantSalaire() != null) {
                        apres.put("montant_salaire", dto.getMontantSalaire());
                    }
                    if (dto.getTauxSupplementaire() != null) {
                        apres.put("taux_supplementaire", dto.getTauxSupplementaire());
                    }
                    break;
                case CHG_POSTE:
                case CHG_UNITE:
                case PROMOTION:
                    if (dto.getEmploiEmployeAvantId() != null) {
                        apres.put("emploi_employe_id", dto.getEmploiEmployeAvantId());
                    }
                    if (dto.getTypeEmployeId() != null) {
                        apres.put("type_employe_id", dto.getTypeEmployeId());
                    }
                    if (dto.getUniteOrganisationnelleId() != null) {
                        apres.put("unite_organisationnelle_id", dto.getUniteOrganisationnelleId());
                    }
                    if (dto.getPosteId() != null) {
                        apres.put("poste_id", dto.getPosteId());
                    }
                    if (dto.getTypeContrat() != null) {
                        apres.put("type_contrat", dto.getTypeContrat());
                    }
                    if (dto.getTempsTravail() != null) {
                        apres.put("temps_travail", dto.getTempsTravail());
                    }
                    if (dto.getHoraireId() != null) {
                        apres.put("horaire_id", dto.getHoraireId());
                    }
                    if (dto.getFonctionId() != null) {
                        apres.put("fonction_id", dto.getFonctionId());
                    }
                    if (dto.getTypeCongeId() != null) {
                        apres.put("type_conge_id", dto.getTypeCongeId());
                    }
                    if (dto.getGestionnaireId() != null) {
                        apres.put("gestionnaire_id", dto.getGestionnaireId());
                    }
                    if (dto.getTauxSupplementaire() != null) {
                        apres.put("taux_supplementaire", dto.getTauxSupplementaire());
                    }
                    if (dto.getRegimePaieId() != null) {
                        apres.put("regime_paie_id", dto.getRegimePaieId());
                    }
                    if (dto.getMontantSalaire() != null) {
                        apres.put("montant_salaire", dto.getMontantSalaire());
                    }
                    break;
                case AJOUT_REGIME_PAIE:
                    if (dto.getEmploiEmployeAvantId() != null) {
                        apres.put("emploi_employe_id", dto.getEmploiEmployeAvantId());
                    }
                    if (dto.getRegimePaieId() != null) {
                        apres.put("regime_paie_id", dto.getRegimePaieId());
                    }
                    if (dto.getMontantSalaire() != null) {
                        apres.put("montant_salaire", dto.getMontantSalaire());
                    }
                    break;
                    
                default:
                    break;
            }
            
            return mapToJson(apres);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private void appliquerMutation(MutationEmploye mutation) {
        try {
            Map<String, Object> avant = jsonToMap(mutation.getAvant());
            Map<String, Object> apres = jsonToMap(mutation.getApres());
            
            switch (mutation.getTypeMutation()) {
                case NOMINATION:
                    appliquerNomination(mutation);
                    break;
                case DEMISSION:
                case LICENCIEMENT:
                case FIN_CONTRAT:
                case ABANDON_POSTE:
                    appliquerChangementStatut(mutation.getEmploye().getId(), avant, apres);
                    break;
                    
                case SUSPENSION:
                    appliquerSuspension(mutation.getEmploye().getId(), avant, apres);
                    break;
                    
                case REINTEGRATION:
                    appliquerReintegration(mutation.getEmploye().getId(), avant, apres);
                    break;
                    
                case REVISION_SALAIRE:
                    appliquerRevisionSalaire(mutation.getEmploye().getId(), avant, apres, mutation.getDateEffet(), mutation.getCreatedBy());
                    break;
                    
                case AJOUT_REGIME_PAIE:
                    appliquerAjoutRegimePaie(mutation.getEmploye().getId(), avant, apres, mutation.getDateEffet(), mutation.getCreatedBy());
                    break;
                    
                case CHG_POSTE:
                case CHG_UNITE:
                case PROMOTION:
                    appliquerChangement(mutation.getEmploye().getId(), avant, apres, mutation.getDateEffet(), mutation.getCreatedBy());
                    break;
                    
                default:
                    throw new RuntimeException("Unknown mutation type: " + mutation.getTypeMutation());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error applying mutation: " + e.getMessage(), e);
        }
    }
    
    private void annulerMutation(MutationEmploye mutation) {
        try {
            Map<String, Object> avant = jsonToMap(mutation.getAvant());
            Map<String, Object> apres = jsonToMap(mutation.getApres());
            
            switch (mutation.getTypeMutation()) {
                case NOMINATION:
                    // No rollback implemented for nomination
                    break;
                case DEMISSION:
                case LICENCIEMENT:
                case FIN_CONTRAT:
                case ABANDON_POSTE:
                    annulerChangementStatut(mutation.getEmploye().getId(), avant, apres);
                    break;
                    
                case SUSPENSION:
                    annulerSuspension(mutation.getEmploye().getId(), avant, apres);
                    break;
                    
                case REINTEGRATION:
                    annulerReintegration(mutation.getEmploye().getId(), avant, apres);
                    break;
                    
                case REVISION_SALAIRE:
                    annulerRevisionSalaire(mutation.getEmploye().getId(), avant, apres);
                    break;
                    
                case AJOUT_REGIME_PAIE:
                    annulerAjoutRegimePaie(mutation.getEmploye().getId(), avant, apres, mutation.getDateEffet());
                    break;
                    
                case CHG_POSTE:
                case CHG_UNITE:
                case PROMOTION:
                    annulerChangement(mutation.getEmploye().getId(), avant, apres);
                    break;
                    
                default:
                    throw new RuntimeException("Unknown mutation type: " + mutation.getTypeMutation());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error cancelling mutation: " + e.getMessage(), e);
        }
    }
    
    // Helper methods for applying mutations
    
    private void appliquerChangementStatut(Long employeId, Map<String, Object> avant, Map<String, Object> apres) {
        Object emploiIdObj = apres.get("emploi_employe_id");
        if (emploiIdObj == null) return;
        
        Long emploiId = toLong(emploiIdObj);
        final Long emploiIdFinal = emploiId;
        EmploiEmploye emploi = emploiEmployeRepository.findById(emploiIdFinal)
            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiIdFinal));
        
        // Set statut based on apres, default to TERMINE
        Object statutApres = apres.get("statut_emploi");
        if (statutApres != null) {
            emploi.setStatutEmploi(EmploiEmploye.StatutEmploi.valueOf(statutApres.toString()));
        } else {
            emploi.setStatutEmploi(EmploiEmploye.StatutEmploi.TERMINE);
        }
        boolean principalSelected = "Y".equalsIgnoreCase(emploi.getPrincipal());
        List<EmploiEmploye> emploisToUpdate;
        if (principalSelected) {
            emploisToUpdate = emploiEmployeRepository.findByEmployeId(employeId).stream()
                .filter(e -> e.getStatutEmploi() == EmploiEmploye.StatutEmploi.ACTIF ||
                             e.getStatutEmploi() == EmploiEmploye.StatutEmploi.SUSPENDU)
                .collect(Collectors.toList());
        } else {
            emploisToUpdate = List.of(emploi);
        }

        for (EmploiEmploye target : emploisToUpdate) {
            if (statutApres != null) {
                target.setStatutEmploi(EmploiEmploye.StatutEmploi.valueOf(statutApres.toString()));
            } else {
                target.setStatutEmploi(EmploiEmploye.StatutEmploi.TERMINE);
            }
            target.setDateFinStatut(LocalDate.now());
            emploiEmployeRepository.save(target);
        }
        
        // Check if any ACTIF emploi remains
        List<EmploiEmploye> emploisActifs = emploiEmployeRepository.findByEmployeId(employeId).stream()
            .filter(e -> e.getStatutEmploi() == EmploiEmploye.StatutEmploi.ACTIF)
            .collect(Collectors.toList());
        
        if (emploisActifs.isEmpty()) {
            Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
            employe.setActif("N");
            employeRepository.save(employe);
        }
    }
    
    private void appliquerSuspension(Long employeId, Map<String, Object> avant, Map<String, Object> apres) {
        Object emploiIdObj = apres.get("emploi_employe_id");
        if (emploiIdObj == null) return;
        
        Long emploiId = toLong(emploiIdObj);
        final Long emploiIdFinal = emploiId;
        EmploiEmploye emploi = emploiEmployeRepository.findById(emploiIdFinal)
            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiIdFinal));
        
        boolean principalSelected = "Y".equalsIgnoreCase(emploi.getPrincipal());
        List<EmploiEmploye> emploisToUpdate;
        if (principalSelected) {
            emploisToUpdate = emploiEmployeRepository.findByEmployeId(employeId).stream()
                .filter(e -> e.getStatutEmploi() == EmploiEmploye.StatutEmploi.ACTIF)
                .collect(Collectors.toList());
        } else {
            emploisToUpdate = List.of(emploi);
        }

        for (EmploiEmploye target : emploisToUpdate) {
            target.setStatutEmploi(EmploiEmploye.StatutEmploi.SUSPENDU);
            target.setDateFinStatut(LocalDate.now());
            emploiEmployeRepository.save(target);
        }
    }

    private void validateEmploiSelection(MutationEmploye.TypeMutation type, Long emploiEmployeId, Long emploiEmployeApresId) {
        if (type == MutationEmploye.TypeMutation.REINTEGRATION) {
            if (emploiEmployeId == null || emploiEmployeApresId == null) {
                throw new RuntimeException("EmploiEmploye AVANT and APRES are required for REINTEGRATION");
            }
            EmploiEmploye emploi = emploiEmployeRepository.findById(emploiEmployeId)
                .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiEmployeId));
            if (emploi.getStatutEmploi() != EmploiEmploye.StatutEmploi.SUSPENDU) {
                throw new RuntimeException("Reintegration requires a SUSPENDU emploi for AVANT");
            }
            return;
        }

        if ((type == MutationEmploye.TypeMutation.REVISION_SALAIRE ||
             type == MutationEmploye.TypeMutation.AJOUT_REGIME_PAIE ||
             type == MutationEmploye.TypeMutation.CHG_POSTE ||
             type == MutationEmploye.TypeMutation.CHG_UNITE ||
             type == MutationEmploye.TypeMutation.PROMOTION) && emploiEmployeId != null) {
            EmploiEmploye emploi = emploiEmployeRepository.findById(emploiEmployeId)
                .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiEmployeId));
            if (emploi.getStatutEmploi() != EmploiEmploye.StatutEmploi.ACTIF &&
                emploi.getStatutEmploi() != EmploiEmploye.StatutEmploi.SUSPENDU) {
                throw new RuntimeException("Mutation requires an ACTIF or SUSPENDU emploi");
            }
            return;
        }

        if (type == MutationEmploye.TypeMutation.LICENCIEMENT ||
            type == MutationEmploye.TypeMutation.FIN_CONTRAT ||
            type == MutationEmploye.TypeMutation.ABANDON_POSTE ||
            type == MutationEmploye.TypeMutation.SUSPENSION) {
            if (emploiEmployeId == null) {
                throw new RuntimeException("EmploiEmploye is required for this mutation type");
            }
            EmploiEmploye emploi = emploiEmployeRepository.findById(emploiEmployeId)
                .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiEmployeId));
            if (type == MutationEmploye.TypeMutation.SUSPENSION) {
                if (emploi.getStatutEmploi() != EmploiEmploye.StatutEmploi.ACTIF) {
                    throw new RuntimeException("Suspension requires an ACTIF emploi");
                }
            } else if (emploi.getStatutEmploi() != EmploiEmploye.StatutEmploi.ACTIF &&
                       emploi.getStatutEmploi() != EmploiEmploye.StatutEmploi.SUSPENDU) {
                throw new RuntimeException("Mutation requires an ACTIF or SUSPENDU emploi");
            }
        }
    }
    
    private void appliquerReintegration(Long employeId, Map<String, Object> avant, Map<String, Object> apres) {
        Object emploiIdObjAvant = avant.get("emploi_employe_id");
        Object emploiIdObjApres = apres.get("emploi_employe_id");
        if (emploiIdObjApres == null) return;
        
        Long emploiIdAvant = emploiIdObjAvant != null ? toLong(emploiIdObjAvant) : null;
        Long emploiIdApres = toLong(emploiIdObjApres);
        
        // If different, close old emploi
        if (emploiIdAvant != null && !emploiIdAvant.equals(emploiIdApres)) {
            EmploiEmploye emploiAvant = emploiEmployeRepository.findById(emploiIdAvant)
                .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiIdAvant));
            emploiAvant.setStatutEmploi(EmploiEmploye.StatutEmploi.TERMINE);
            emploiAvant.setDateFinStatut(LocalDate.now());
            emploiEmployeRepository.save(emploiAvant);
        }
        
        // Activate new emploi
        EmploiEmploye emploiApres = emploiEmployeRepository.findById(emploiIdApres)
            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiIdApres));
        emploiApres.setStatutEmploi(EmploiEmploye.StatutEmploi.ACTIF);
        emploiApres.setDateFinStatut(null);
        emploiEmployeRepository.save(emploiApres);
        
        // Activate employe if needed
        Employe employe = employeRepository.findById(employeId)
            .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        if ("N".equalsIgnoreCase(employe.getActif())) {
            employe.setActif("Y");
            employeRepository.save(employe);
        }
    }
    
    private void appliquerRevisionSalaire(Long employeId, Map<String, Object> avant, Map<String, Object> apres, LocalDate dateEffet, String username) {
        Object emploiIdObj = avant.get("emploi_employe_id");
        Object salaireIdObj = avant.get("salaire_employe_id");
        if (emploiIdObj == null || salaireIdObj == null) return;
        
        Long emploiId = toLong(emploiIdObj);
        Long salaireId = toLong(salaireIdObj);
        
        // Update salaire montant by creating a new record (traceability)
        Object montantObj = apres.get("montant_salaire");
        if (montantObj != null) {
            EmployeSalaire salaire = employeSalaireRepository.findById(salaireId)
                .orElseThrow(() -> new RuntimeException("EmployeSalaire not found with id: " + salaireId));
            String principalFlag = salaire.getPrincipal();
            salaire.setActif("N");
            salaire.setDateFin(dateEffet);
            salaire.setUpdatedBy(username != null ? username : salaire.getUpdatedBy());
            salaire.setUpdatedOn(OffsetDateTime.now());
            employeSalaireRepository.save(salaire);

            EmployeSalaire nouveauSalaire = new EmployeSalaire();
            nouveauSalaire.setEmploye(salaire.getEmploye());
            nouveauSalaire.setEmploi(salaire.getEmploi());
            nouveauSalaire.setRegimePaie(salaire.getRegimePaie());
            nouveauSalaire.setMontant(new java.math.BigDecimal(montantObj.toString()));
            nouveauSalaire.setDateDebut(dateEffet);
            nouveauSalaire.setDateFin(null);
            nouveauSalaire.setPrincipal(principalFlag);
            nouveauSalaire.setActif("Y");
            nouveauSalaire.setCreatedBy(username != null ? username : salaire.getCreatedBy());
            nouveauSalaire.setCreatedOn(OffsetDateTime.now());
            employeSalaireRepository.save(nouveauSalaire);
        }
        
        // Update emploi taux_supplementaire
        Object tauxObj = apres.get("taux_supplementaire");
        if (tauxObj != null) {
            EmploiEmploye emploi = emploiEmployeRepository.findById(emploiId)
                .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiId));
            emploi.setTauxSupplementaire(new java.math.BigDecimal(tauxObj.toString()));
            emploiEmployeRepository.save(emploi);
        }
    }
    
    private void appliquerAjoutRegimePaie(Long employeId, Map<String, Object> avant, Map<String, Object> apres, LocalDate dateEffet, String username) {
        Long emploiId = toLong(avant.get("emploi_employe_id"));
        if (emploiId == null) {
            emploiId = toLong(apres.get("emploi_employe_id"));
        }
        if (emploiId == null) {
            return;
        }
        Long regimePaieId = toLong(apres.get("regime_paie_id"));
        if (regimePaieId == null) {
            throw new RuntimeException("RegimePaie is required for AJOUT_REGIME_PAIE");
        }
        
        List<EmployeSalaire> salaires = employeSalaireRepository.findByEmploiId(emploiId);
        boolean alreadyAssigned = salaires.stream()
            .anyMatch(s -> "Y".equalsIgnoreCase(s.getActif())
                && s.getRegimePaie() != null
                && regimePaieId.equals(s.getRegimePaie().getId()));
        if (alreadyAssigned) {
            throw new RuntimeException("RegimePaie already assigned to this emploi");
        }
        
        java.math.BigDecimal montant = null;
        Object montantObj = apres.get("montant_salaire");
        if (montantObj != null) {
            montant = new java.math.BigDecimal(montantObj.toString());
        }
        if (montant == null) {
            EmployeSalaire salaireRef = salaires.stream()
                .filter(s -> "Y".equalsIgnoreCase(s.getActif()))
                .findFirst()
                .orElse(null);
            if (salaireRef != null) {
                montant = salaireRef.getMontant();
            }
        }
        if (montant == null) {
            throw new RuntimeException("Montant salaire is required for AJOUT_REGIME_PAIE");
        }
        
        final Long emploiIdFinal = emploiId;
        EmploiEmploye emploi = emploiEmployeRepository.findById(emploiIdFinal)
            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiIdFinal));
        RegimePaie regimePaie = regimePaieRepository.findById(regimePaieId)
            .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + regimePaieId));
        
        EmployeSalaire nouveauSalaire = new EmployeSalaire();
        nouveauSalaire.setEmploye(emploi.getEmploye());
        nouveauSalaire.setEmploi(emploi);
        nouveauSalaire.setRegimePaie(regimePaie);
        nouveauSalaire.setMontant(montant);
        nouveauSalaire.setDateDebut(dateEffet);
        nouveauSalaire.setDateFin(null);
        nouveauSalaire.setPrincipal("N");
        nouveauSalaire.setActif("Y");
        nouveauSalaire.setCreatedBy(username != null ? username : "SYSTEME");
        nouveauSalaire.setCreatedOn(OffsetDateTime.now());
        employeSalaireRepository.save(nouveauSalaire);
    }
    
    private void appliquerChangement(Long employeId, Map<String, Object> avant, Map<String, Object> apres, LocalDate dateEffet, String username) {
        Object emploiIdObj = avant.get("emploi_employe_id");
        if (emploiIdObj == null) {
            return;
        }
        Long emploiId = toLong(emploiIdObj);
        EmploiEmploye emploiAvant = emploiEmployeRepository.findById(emploiId)
            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiId));

        EmploiEmploye.StatutEmploi oldStatut = emploiAvant.getStatutEmploi();

        // Terminate old emploi
        emploiAvant.setStatutEmploi(EmploiEmploye.StatutEmploi.TERMINE);
        emploiAvant.setDateFinStatut(dateEffet);
        emploiAvant.setDateFin(dateEffet);
        emploiAvant.setUpdatedBy(username);
        emploiAvant.setUpdatedOn(OffsetDateTime.now());
        emploiEmployeRepository.save(emploiAvant);

        EmploiEmploye.StatutEmploi newStatut = oldStatut == EmploiEmploye.StatutEmploi.SUSPENDU
            ? EmploiEmploye.StatutEmploi.SUSPENDU
            : EmploiEmploye.StatutEmploi.ACTIF;

        EmploiEmploye emploiNouveau = new EmploiEmploye();
        emploiNouveau.setEmploye(emploiAvant.getEmploye());
        emploiNouveau.setDateDebut(dateEffet);
        emploiNouveau.setDateFin(null);
        emploiNouveau.setMotifFin(null);
        emploiNouveau.setStatutEmploi(newStatut);
        emploiNouveau.setDateFinStatut(null);

        Long typeEmployeId = toLong(apres.get("type_employe_id"));
        Long uniteId = toLong(apres.get("unite_organisationnelle_id"));
        Long posteId = toLong(apres.get("poste_id"));
        Long horaireId = toLong(apres.get("horaire_id"));
        Long fonctionId = toLong(apres.get("fonction_id"));
        Long typeCongeId = toLong(apres.get("type_conge_id"));
        Long gestionnaireId = toLong(apres.get("gestionnaire_id"));
        Object typeContratObj = apres.get("type_contrat");
        Object tempsTravailObj = apres.get("temps_travail");
        Object tauxSupObj = apres.get("taux_supplementaire");

        emploiNouveau.setTypeEmploye(typeEmployeId != null
            ? typeEmployeRepository.findById(typeEmployeId)
                .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + typeEmployeId))
            : emploiAvant.getTypeEmploye());
        emploiNouveau.setUniteOrganisationnelle(uniteId != null
            ? uniteOrganisationnelleRepository.findById(uniteId)
                .orElseThrow(() -> new RuntimeException("UniteOrganisationnelle not found with id: " + uniteId))
            : emploiAvant.getUniteOrganisationnelle());
        emploiNouveau.setPoste(posteId != null
            ? posteRepository.findById(posteId)
                .orElseThrow(() -> new RuntimeException("Poste not found with id: " + posteId))
            : emploiAvant.getPoste());
        emploiNouveau.setHoraire(horaireId != null
            ? horaireRepository.findById(horaireId)
                .orElseThrow(() -> new RuntimeException("Horaire not found with id: " + horaireId))
            : emploiAvant.getHoraire());
        emploiNouveau.setFonction(fonctionId != null
            ? fonctionRepository.findById(fonctionId)
                .orElseThrow(() -> new RuntimeException("Fonction not found with id: " + fonctionId))
            : emploiAvant.getFonction());
        emploiNouveau.setTypeConge(typeCongeId != null
            ? typeCongeRepository.findById(typeCongeId)
                .orElseThrow(() -> new RuntimeException("TypeConge not found with id: " + typeCongeId))
            : emploiAvant.getTypeConge());
        emploiNouveau.setGestionnaire(gestionnaireId != null
            ? employeRepository.findById(gestionnaireId)
                .orElseThrow(() -> new RuntimeException("Gestionnaire not found with id: " + gestionnaireId))
            : emploiAvant.getGestionnaire());
        emploiNouveau.setTypeContrat(typeContratObj != null
            ? EmploiEmploye.TypeContrat.valueOf(typeContratObj.toString())
            : emploiAvant.getTypeContrat());
        emploiNouveau.setTempsTravail(tempsTravailObj != null
            ? EmploiEmploye.TempsTravail.valueOf(tempsTravailObj.toString())
            : emploiAvant.getTempsTravail());
        emploiNouveau.setTauxSupplementaire(tauxSupObj != null
            ? new java.math.BigDecimal(tauxSupObj.toString())
            : emploiAvant.getTauxSupplementaire());
        emploiNouveau.setJourOff1(emploiAvant.getJourOff1());
        emploiNouveau.setJourOff2(emploiAvant.getJourOff2());
        emploiNouveau.setJourOff3(emploiAvant.getJourOff3());
        emploiNouveau.setEnConge(emploiAvant.getEnConge());
        emploiNouveau.setEnProbation(emploiAvant.getEnProbation());
        emploiNouveau.setPrincipal(emploiAvant.getPrincipal());
        emploiNouveau.setCreatedBy(username != null ? username : emploiAvant.getCreatedBy());
        emploiNouveau.setCreatedOn(OffsetDateTime.now());
        emploiEmployeRepository.save(emploiNouveau);

        // Transfer active salaries to new emploi and apply changes if provided
        List<EmployeSalaire> salairesActifs = employeSalaireRepository.findByEmploiId(emploiAvant.getId()).stream()
            .filter(s -> "Y".equalsIgnoreCase(s.getActif()))
            .collect(Collectors.toList());

        Long salaireAvantId = toLong(avant.get("salaire_employe_id"));
        Long regimePaieId = toLong(apres.get("regime_paie_id"));
        Object montantObj = apres.get("montant_salaire");
        java.math.BigDecimal montantApres = montantObj != null ? new java.math.BigDecimal(montantObj.toString()) : null;

        for (EmployeSalaire salaire : salairesActifs) {
            salaire.setActif("N");
            salaire.setDateFin(dateEffet);
            salaire.setUpdatedBy(username);
            salaire.setUpdatedOn(OffsetDateTime.now());
            employeSalaireRepository.save(salaire);

            EmployeSalaire nouveauSalaire = new EmployeSalaire();
            nouveauSalaire.setEmploye(salaire.getEmploye());
            nouveauSalaire.setEmploi(emploiNouveau);
            if (salaireAvantId != null && salaire.getId().equals(salaireAvantId)) {
                if (regimePaieId != null) {
                    nouveauSalaire.setRegimePaie(regimePaieRepository.findById(regimePaieId)
                        .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + regimePaieId)));
                } else {
                    nouveauSalaire.setRegimePaie(salaire.getRegimePaie());
                }
                nouveauSalaire.setMontant(montantApres != null ? montantApres : salaire.getMontant());
            } else {
                nouveauSalaire.setRegimePaie(salaire.getRegimePaie());
                nouveauSalaire.setMontant(salaire.getMontant());
            }
            nouveauSalaire.setDateDebut(dateEffet);
            nouveauSalaire.setDateFin(null);
            nouveauSalaire.setPrincipal(salaire.getPrincipal());
            nouveauSalaire.setActif("Y");
            nouveauSalaire.setCreatedBy(username != null ? username : salaire.getCreatedBy());
            nouveauSalaire.setCreatedOn(OffsetDateTime.now());
            employeSalaireRepository.save(nouveauSalaire);
        }
    }

    private void appliquerNomination(MutationEmploye mutation) {
        Employe employe = employeRepository.findById(mutation.getEmploye().getId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + mutation.getEmploye().getId()));
        validateNominationEligibility(employe);

        LocalDate dateEffet = mutation.getDateEffet();

        List<EmploiEmploye> emplois = emploiEmployeRepository.findByEmployeId(employe.getId());
        LocalDate premiereEmbauche = null;
        for (EmploiEmploye emploi : emplois) {
            if (emploi.getStatutEmploi() == EmploiEmploye.StatutEmploi.NOUVEAU) {
                emploi.setStatutEmploi(EmploiEmploye.StatutEmploi.ACTIF);
                emploi.setDateFinStatut(dateEffet);
                if (emploi.getDateDebut() != null) {
                    if (premiereEmbauche == null || emploi.getDateDebut().isBefore(premiereEmbauche)) {
                        premiereEmbauche = emploi.getDateDebut();
                    }
                }
                emploiEmployeRepository.save(emploi);
            }
        }

        List<EmployeSalaire> salaires = employeSalaireRepository.findByEmployeIdAndActif(employe.getId(), "N");
        for (EmployeSalaire salaire : salaires) {
            salaire.setActif("Y");
            employeSalaireRepository.save(salaire);
        }

        employe.setActif("Y");
        employe.setNomme("Y");
        if (employe.getDatePremiereEmbauche() == null) {
            employe.setDatePremiereEmbauche(premiereEmbauche != null ? premiereEmbauche : dateEffet);
        }
        employeRepository.save(employe);
    }

    private void validateNominationEligibility(Employe employe) {
        if (!"N".equalsIgnoreCase(employe.getActif()) || !"N".equalsIgnoreCase(employe.getNomme())) {
            throw new RuntimeException("Nomination not allowed for this employee");
        }
        boolean hasNouveauEmploi = emploiEmployeRepository.existsByEmployeIdAndStatutEmploi(
                employe.getId(), EmploiEmploye.StatutEmploi.NOUVEAU);
        if (!hasNouveauEmploi) {
            throw new RuntimeException("Nomination requires at least one emploi with statut NOUVEAU");
        }
        boolean hasInactiveSalaire = employeSalaireRepository.existsByEmployeIdAndActif(employe.getId(), "N");
        if (!hasInactiveSalaire) {
            throw new RuntimeException("Nomination requires at least one salaire with actif N");
        }
    }
    
    // Helper methods for cancelling mutations
    
    private void annulerChangementStatut(Long employeId, Map<String, Object> avant, Map<String, Object> apres) {
        Object emploiIdObj = apres.get("emploi_employe_id");
        if (emploiIdObj == null) return;
        
        Long emploiId = toLong(emploiIdObj);
        EmploiEmploye emploi = emploiEmployeRepository.findById(emploiId)
            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiId));
        
        // Restore previous statut
        Object statutAvant = avant.get("statut_emploi");
        if (statutAvant != null) {
            emploi.setStatutEmploi(EmploiEmploye.StatutEmploi.valueOf(statutAvant.toString()));
            emploi.setDateFinStatut(null);
            emploiEmployeRepository.save(emploi);
        }
        
        // Restore employe actif if needed
        List<EmploiEmploye> emploisActifs = emploiEmployeRepository.findByEmployeId(employeId).stream()
            .filter(e -> e.getStatutEmploi() == EmploiEmploye.StatutEmploi.ACTIF)
            .collect(Collectors.toList());
        
        if (!emploisActifs.isEmpty()) {
            Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
            employe.setActif("Y");
            employeRepository.save(employe);
        }
    }
    
    private void annulerSuspension(Long employeId, Map<String, Object> avant, Map<String, Object> apres) {
        Object emploiIdObj = apres.get("emploi_employe_id");
        if (emploiIdObj == null) return;
        
        Long emploiId = toLong(emploiIdObj);
        EmploiEmploye emploi = emploiEmployeRepository.findById(emploiId)
            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiId));
        
        // Restore to ACTIF
        emploi.setStatutEmploi(EmploiEmploye.StatutEmploi.ACTIF);
        emploi.setDateFinStatut(null);
        emploiEmployeRepository.save(emploi);
    }
    
    private void annulerReintegration(Long employeId, Map<String, Object> avant, Map<String, Object> apres) {
        Object emploiIdObjAvant = avant.get("emploi_employe_id");
        Object emploiIdObjApres = apres.get("emploi_employe_id");
        if (emploiIdObjAvant == null || emploiIdObjApres == null) return;
        
        Long emploiIdAvant = toLong(emploiIdObjAvant);
        Long emploiIdApres = toLong(emploiIdObjApres);
        
        // Restore old emploi to SUSPENDU
        EmploiEmploye emploiAvant = emploiEmployeRepository.findById(emploiIdAvant)
            .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiIdAvant));
        emploiAvant.setStatutEmploi(EmploiEmploye.StatutEmploi.SUSPENDU);
        emploiEmployeRepository.save(emploiAvant);
        
        // Restore new emploi to previous state (or TERMINE if different)
        if (!emploiIdAvant.equals(emploiIdApres)) {
            EmploiEmploye emploiApres = emploiEmployeRepository.findById(emploiIdApres)
                .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiIdApres));
            emploiApres.setStatutEmploi(EmploiEmploye.StatutEmploi.TERMINE);
            emploiEmployeRepository.save(emploiApres);
        }
    }
    
    private void annulerRevisionSalaire(Long employeId, Map<String, Object> avant, Map<String, Object> apres) {
        Object salaireIdObj = avant.get("salaire_employe_id");
        Object emploiIdObj = avant.get("emploi_employe_id");
        if (salaireIdObj == null || emploiIdObj == null) return;
        
        Long salaireId = toLong(salaireIdObj);
        Long emploiId = toLong(emploiIdObj);
        
        // Restore salaire montant
        Object montantAvant = avant.get("montant_salaire");
        if (montantAvant != null) {
            EmployeSalaire salaire = employeSalaireRepository.findById(salaireId)
                .orElseThrow(() -> new RuntimeException("EmployeSalaire not found with id: " + salaireId));
            salaire.setMontant(new java.math.BigDecimal(montantAvant.toString()));
            employeSalaireRepository.save(salaire);
        }
        
        // Restore taux_supplementaire
        Object tauxAvant = avant.get("taux_supplementaire");
        if (tauxAvant != null) {
            EmploiEmploye emploi = emploiEmployeRepository.findById(emploiId)
                .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiId));
            emploi.setTauxSupplementaire(new java.math.BigDecimal(tauxAvant.toString()));
            emploiEmployeRepository.save(emploi);
        }
    }
    
    private void annulerAjoutRegimePaie(Long employeId, Map<String, Object> avant, Map<String, Object> apres, LocalDate dateEffet) {
        Long emploiId = toLong(avant.get("emploi_employe_id"));
        if (emploiId == null) {
            emploiId = toLong(apres.get("emploi_employe_id"));
        }
        if (emploiId == null) {
            return;
        }
        Long regimePaieId = toLong(apres.get("regime_paie_id"));
        if (regimePaieId == null) {
            return;
        }
        
        List<EmployeSalaire> salaires = employeSalaireRepository.findByEmploiId(emploiId);
        EmployeSalaire salaireToCancel = salaires.stream()
            .filter(s -> "Y".equalsIgnoreCase(s.getActif())
                && s.getRegimePaie() != null
                && regimePaieId.equals(s.getRegimePaie().getId()))
            .filter(s -> dateEffet == null || dateEffet.equals(s.getDateDebut()))
            .sorted((a, b) -> b.getId().compareTo(a.getId()))
            .findFirst()
            .orElse(null);
        if (salaireToCancel == null) {
            return;
        }
        salaireToCancel.setActif("N");
        salaireToCancel.setDateFin(dateEffet != null ? dateEffet : LocalDate.now());
        employeSalaireRepository.save(salaireToCancel);
    }
    
    private void annulerChangement(Long employeId, Map<String, Object> avant, Map<String, Object> apres) {
        // Simplified version - restore similar to REVISION_SALAIRE
        annulerRevisionSalaire(employeId, avant, apres);
        // TODO: Implement full cancellation logic
    }
    
    // JSON helper methods (simple implementation)
    
    private String mapToJson(Map<String, Object> map) {
        if (map.isEmpty()) return "{}";
        
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else {
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
    
    private Map<String, Object> jsonToMap(String json) {
        Map<String, Object> map = new HashMap<>();
        if (json == null || json.trim().isEmpty() || "{}".equals(json.trim())) {
            return map;
        }
        
        try {
            // Simple JSON parsing (for production, use a proper JSON library)
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                String[] pairs = json.split(",");
                for (String pair : pairs) {
                    String[] kv = pair.split(":", 2);
                    if (kv.length == 2) {
                        String key = kv[0].trim().replaceAll("^\"|\"$", "");
                        String value = kv[1].trim();
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            map.put(key, value.substring(1, value.length() - 1));
                        } else if ("null".equals(value)) {
                            map.put(key, null);
                        } else {
                            try {
                                map.put(key, Double.parseDouble(value));
                            } catch (NumberFormatException e) {
                                map.put(key, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // If parsing fails, return empty map
        }
        return map;
    }
    
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }
    
    private EmploiEmployeDTO toEmploiEmployeDTO(EmploiEmploye entity) {
        EmploiEmployeDTO dto = new EmploiEmployeDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye().getId());
        dto.setDateDebut(entity.getDateDebut());
        dto.setDateFin(entity.getDateFin());
        dto.setMotifFin(entity.getMotifFin());
        dto.setStatutEmploi(entity.getStatutEmploi() != null ? entity.getStatutEmploi().name() : null);
        dto.setDateFinStatut(entity.getDateFinStatut());
        dto.setTypeContrat(entity.getTypeContrat() != null ? entity.getTypeContrat().name() : null);
        dto.setTempsTravail(entity.getTempsTravail() != null ? entity.getTempsTravail().name() : null);
        
        if (entity.getTypeEmploye() != null) {
            dto.setTypeEmployeId(entity.getTypeEmploye().getId());
            dto.setTypeEmployeDescription(entity.getTypeEmploye().getDescription());
        }
        if (entity.getUniteOrganisationnelle() != null) {
            dto.setUniteOrganisationnelleId(entity.getUniteOrganisationnelle().getId());
            dto.setUniteOrganisationnelleCode(entity.getUniteOrganisationnelle().getCode());
            dto.setUniteOrganisationnelleNom(entity.getUniteOrganisationnelle().getNom());
        }
        if (entity.getPoste() != null) {
            dto.setPosteId(entity.getPoste().getId());
            dto.setPosteCode(entity.getPoste().getCodePoste());
            dto.setPosteDescription(entity.getPoste().getDescription());
        }
        if (entity.getHoraire() != null) {
            dto.setHoraireId(entity.getHoraire().getId());
            dto.setHoraireCode(entity.getHoraire().getCodeHoraire());
            dto.setHoraireDescription(entity.getHoraire().getDescription());
        }
        dto.setTauxSupplementaire(entity.getTauxSupplementaire());
        if (entity.getFonction() != null) {
            dto.setFonctionId(entity.getFonction().getId());
            dto.setFonctionCode(entity.getFonction().getCodeFonction());
            dto.setFonctionDescription(entity.getFonction().getDescription());
        }
        if (entity.getGestionnaire() != null) {
            dto.setGestionnaireId(entity.getGestionnaire().getId());
            dto.setGestionnaireCode(entity.getGestionnaire().getCodeEmploye());
            dto.setGestionnaireNom(entity.getGestionnaire().getNom());
            dto.setGestionnairePrenom(entity.getGestionnaire().getPrenom());
        }
        if (entity.getTypeConge() != null) {
            dto.setTypeCongeId(entity.getTypeConge().getId());
            dto.setTypeCongeCode(entity.getTypeConge().getCodeConge());
            dto.setTypeCongeDescription(entity.getTypeConge().getDescription());
            dto.setTypeCongeAnnuel(entity.getTypeConge().getCongeAnnuel() != null
                    ? entity.getTypeConge().getCongeAnnuel().name()
                    : null);
        }
        dto.setJourOff1(entity.getJourOff1());
        dto.setJourOff2(entity.getJourOff2());
        dto.setJourOff3(entity.getJourOff3());
        dto.setEnConge(entity.getEnConge());
        dto.setEnProbation(entity.getEnProbation());
        dto.setPrincipal(entity.getPrincipal());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
    
    private MutationEmployeDTO toDTO(MutationEmploye entity) {
        MutationEmployeDTO dto = new MutationEmployeDTO();
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
        
        dto.setTypeMutation(entity.getTypeMutation() != null ? entity.getTypeMutation().name() : null);
        dto.setDateEffet(entity.getDateEffet());
        dto.setDateSaisie(entity.getDateSaisie());
        dto.setStatut(entity.getStatut() != null ? entity.getStatut().name() : null);
        dto.setMotif(entity.getMotif());
        dto.setReference(entity.getReference());
        dto.setAvant(entity.getAvant());
        dto.setApres(entity.getApres());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        return dto;
    }
}
