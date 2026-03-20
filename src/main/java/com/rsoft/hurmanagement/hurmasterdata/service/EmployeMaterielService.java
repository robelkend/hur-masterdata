package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.EmployeMaterielCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.EmployeMaterielDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.EmployeMaterielEvenementDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.EmployeMaterielUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeMateriel;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeMaterielEvenement;
import com.rsoft.hurmanagement.hurmasterdata.entity.RefMateriel;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeMaterielEvenementRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeMaterielRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RefMaterielRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeMaterielService {

    private final EmployeMaterielRepository repository;
    private final EmployeMaterielEvenementRepository evenementRepository;
    private final EmployeRepository employeRepository;
    private final RefMaterielRepository refMaterielRepository;

    @Transactional(readOnly = true)
    public Page<EmployeMaterielDTO> findAll(Long employeId,
                                            Long materielId,
                                            String statut,
                                            LocalDate dateDebut,
                                            LocalDate dateFin,
                                            Pageable pageable) {
        EmployeMateriel.StatutMateriel statutEnum = null;
        if (statut != null && !statut.isBlank()) {
            try {
                statutEnum = EmployeMateriel.StatutMateriel.valueOf(statut);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return repository.findAllWithFilters(employeId, materielId, statutEnum, dateDebut, dateFin, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public EmployeMaterielDTO findById(Long id) {
        EmployeMateriel entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeMateriel not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional
    public EmployeMaterielDTO create(EmployeMaterielCreateDTO dto, String username) {
        EmployeMateriel entity = new EmployeMateriel();
        apply(dto, entity);
        applyBusinessRules(entity);
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        EmployeMateriel saved = repository.save(entity);
        createEvent(saved, mapToEvent(saved.getStatut()), saved.getDateAttribution(), null, "Attribution materiel", username);
        return toDTO(saved);
    }

    @Transactional
    public EmployeMaterielDTO update(Long id, EmployeMaterielUpdateDTO dto, String username) {
        EmployeMateriel entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeMateriel not found with id: " + id));
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }

        EmployeMateriel.StatutMateriel oldStatut = entity.getStatut();
        apply(dto, entity);
        applyBusinessRules(entity);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        EmployeMateriel saved = repository.save(entity);

        if (oldStatut != saved.getStatut()) {
            BigDecimal montantEvt = saved.getStatut() == EmployeMateriel.StatutMateriel.FACTURE
                    ? saved.getValeurResiduelleCalculee()
                    : null;
            createEvent(saved,
                    mapToEvent(saved.getStatut()),
                    LocalDate.now(),
                    montantEvt,
                    "Changement statut: " + oldStatut + " -> " + saved.getStatut(),
                    username);
        }
        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id, Integer rowscn) {
        EmployeMateriel entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeMateriel not found with id: " + id));
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before deleting.");
        }
        evenementRepository.deleteByEmployeMaterielId(entity.getId());
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<EmployeMaterielEvenementDTO> findEvenements(Long employeMaterielId) {
        return evenementRepository.findByEmployeMaterielIdOrderByDateEvenementDescIdDesc(employeMaterielId)
                .stream()
                .map(this::toEvenementDTO)
                .toList();
    }

    private void apply(EmployeMaterielCreateDTO dto, EmployeMateriel entity) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        RefMateriel materiel = refMaterielRepository.findById(dto.getMaterielId())
                .orElseThrow(() -> new RuntimeException("RefMateriel not found with id: " + dto.getMaterielId()));
        entity.setEmploye(employe);
        entity.setMateriel(materiel);
        entity.setNumeroSerie(dto.getNumeroSerie());
        entity.setDateAttribution(dto.getDateAttribution());
        entity.setDateFinPrevue(dto.getDateFinPrevue());
        entity.setValeurAttribution(dto.getValeurAttribution() != null ? dto.getValeurAttribution() : BigDecimal.ZERO);
        entity.setDateTransfertPropriete(dto.getDateTransfertPropriete());
        entity.setDateRestitutionEffective(dto.getDateRestitutionEffective());
        entity.setObservations(dto.getObservations());

        if (dto.getStatut() != null && !dto.getStatut().isBlank()) {
            entity.setStatut(EmployeMateriel.StatutMateriel.valueOf(dto.getStatut()));
        } else if (entity.getStatut() == null) {
            entity.setStatut(EmployeMateriel.StatutMateriel.ATTRIBUE);
        }
    }

    private void apply(EmployeMaterielUpdateDTO dto, EmployeMateriel entity) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        RefMateriel materiel = refMaterielRepository.findById(dto.getMaterielId())
                .orElseThrow(() -> new RuntimeException("RefMateriel not found with id: " + dto.getMaterielId()));
        entity.setEmploye(employe);
        entity.setMateriel(materiel);
        entity.setNumeroSerie(dto.getNumeroSerie());
        entity.setDateAttribution(dto.getDateAttribution());
        entity.setDateFinPrevue(dto.getDateFinPrevue());
        entity.setValeurAttribution(dto.getValeurAttribution() != null ? dto.getValeurAttribution() : BigDecimal.ZERO);
        entity.setDateTransfertPropriete(dto.getDateTransfertPropriete());
        entity.setDateRestitutionEffective(dto.getDateRestitutionEffective());
        entity.setObservations(dto.getObservations());
        if (dto.getStatut() != null && !dto.getStatut().isBlank()) {
            entity.setStatut(EmployeMateriel.StatutMateriel.valueOf(dto.getStatut()));
        }
    }

    private void applyBusinessRules(EmployeMateriel entity) {
        LocalDate refDate = entity.getDateRestitutionEffective() != null
                ? entity.getDateRestitutionEffective()
                : (entity.getDateTransfertPropriete() != null ? entity.getDateTransfertPropriete() : LocalDate.now());

        BigDecimal residuelle = computeValeurResiduelle(entity, refDate);
        entity.setValeurResiduelleCalculee(residuelle);

        if (entity.getDateAttribution() != null
                && entity.getMateriel() != null
                && entity.getDateRestitutionEffective() == null
                && entity.getStatut() != EmployeMateriel.StatutMateriel.RESTITUE) {
            int delaiTransfert = entity.getMateriel().getDureeTransfertProprieteMois() != null
                    ? entity.getMateriel().getDureeTransfertProprieteMois()
                    : 0;
            if (delaiTransfert > 0) {
                LocalDate dateEligibleTransfert = entity.getDateAttribution().plusMonths(delaiTransfert);
                if (!LocalDate.now().isBefore(dateEligibleTransfert)) {
                    entity.setStatut(EmployeMateriel.StatutMateriel.TRANSFERE_EMPLOYE);
                    if (entity.getDateTransfertPropriete() == null) {
                        entity.setDateTransfertPropriete(dateEligibleTransfert);
                    }
                }
            }
        }

        if (entity.getStatut() == EmployeMateriel.StatutMateriel.RESTITUE && entity.getDateRestitutionEffective() == null) {
            entity.setDateRestitutionEffective(LocalDate.now());
        }
    }

    private BigDecimal computeValeurResiduelle(EmployeMateriel entity, LocalDate refDate) {
        if (entity.getValeurAttribution() == null || entity.getValeurAttribution().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (entity.getDateAttribution() == null || entity.getMateriel() == null) {
            return entity.getValeurAttribution().setScale(2, RoundingMode.HALF_UP);
        }
        if (!"Y".equalsIgnoreCase(entity.getMateriel().getDepreciable())) {
            return entity.getValeurAttribution().setScale(2, RoundingMode.HALF_UP);
        }

        int dureeMois = entity.getMateriel().getDureeDepreciationMois() != null
                ? entity.getMateriel().getDureeDepreciationMois()
                : 0;
        if (dureeMois <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        LocalDate effectiveRefDate = refDate != null ? refDate : LocalDate.now();
        long nbMoisUtilises = Math.max(0, ChronoUnit.MONTHS.between(entity.getDateAttribution(), effectiveRefDate));
        BigDecimal depreciationMensuelle = entity.getValeurAttribution()
                .divide(BigDecimal.valueOf(dureeMois), 6, RoundingMode.HALF_UP);
        BigDecimal valeurResiduelle = entity.getValeurAttribution()
                .subtract(depreciationMensuelle.multiply(BigDecimal.valueOf(nbMoisUtilises)));
        if (valeurResiduelle.compareTo(BigDecimal.ZERO) < 0) {
            valeurResiduelle = BigDecimal.ZERO;
        }
        return valeurResiduelle.setScale(2, RoundingMode.HALF_UP);
    }

    private EmployeMaterielEvenement.TypeEvenement mapToEvent(EmployeMateriel.StatutMateriel statut) {
        if (statut == null) {
            return EmployeMaterielEvenement.TypeEvenement.ATTRIBUTION;
        }
        return switch (statut) {
            case ATTRIBUE -> EmployeMaterielEvenement.TypeEvenement.ATTRIBUTION;
            case RESTITUE -> EmployeMaterielEvenement.TypeEvenement.RESTITUTION;
            case TRANSFERE_EMPLOYE -> EmployeMaterielEvenement.TypeEvenement.TRANSFERT;
            case PERDU -> EmployeMaterielEvenement.TypeEvenement.PERTE;
            case ENDOMMAGE -> EmployeMaterielEvenement.TypeEvenement.DETERIORATION;
            case FACTURE -> EmployeMaterielEvenement.TypeEvenement.FACTURATION;
            case CLOTURE -> EmployeMaterielEvenement.TypeEvenement.ANNULATION;
        };
    }

    private void createEvent(EmployeMateriel employeMateriel,
                             EmployeMaterielEvenement.TypeEvenement type,
                             LocalDate dateEvent,
                             BigDecimal montant,
                             String commentaire,
                             String username) {
        EmployeMaterielEvenement evt = new EmployeMaterielEvenement();
        evt.setEmployeMateriel(employeMateriel);
        evt.setTypeEvenement(type);
        evt.setDateEvenement(dateEvent != null ? dateEvent : LocalDate.now());
        evt.setMontant(montant);
        evt.setCommentaire(commentaire);
        evt.setCreatedBy(username);
        evt.setCreatedOn(OffsetDateTime.now());
        evt.setRowscn(1);
        evenementRepository.save(evt);
    }

    private EmployeMaterielDTO toDTO(EmployeMateriel entity) {
        EmployeMaterielDTO dto = new EmployeMaterielDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye() != null ? entity.getEmploye().getId() : null);
        dto.setEmployeCode(entity.getEmploye() != null ? entity.getEmploye().getCodeEmploye() : null);
        dto.setEmployeNom(entity.getEmploye() != null ? entity.getEmploye().getNom() : null);
        dto.setEmployePrenom(entity.getEmploye() != null ? entity.getEmploye().getPrenom() : null);
        dto.setMaterielId(entity.getMateriel() != null ? entity.getMateriel().getId() : null);
        dto.setMaterielCode(entity.getMateriel() != null ? entity.getMateriel().getCodeMateriel() : null);
        dto.setMaterielLibelle(entity.getMateriel() != null ? entity.getMateriel().getLibelle() : null);
        dto.setNumeroSerie(entity.getNumeroSerie());
        dto.setDateAttribution(entity.getDateAttribution());
        dto.setDateFinPrevue(entity.getDateFinPrevue());
        dto.setValeurAttribution(entity.getValeurAttribution());
        dto.setStatut(entity.getStatut() != null ? entity.getStatut().name() : null);
        dto.setDateTransfertPropriete(entity.getDateTransfertPropriete());
        dto.setDateRestitutionEffective(entity.getDateRestitutionEffective());
        dto.setValeurResiduelleCalculee(entity.getValeurResiduelleCalculee());
        dto.setObservations(entity.getObservations());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private EmployeMaterielEvenementDTO toEvenementDTO(EmployeMaterielEvenement entity) {
        EmployeMaterielEvenementDTO dto = new EmployeMaterielEvenementDTO();
        dto.setId(entity.getId());
        dto.setEmployeMaterielId(entity.getEmployeMateriel() != null ? entity.getEmployeMateriel().getId() : null);
        dto.setTypeEvenement(entity.getTypeEvenement() != null ? entity.getTypeEvenement().name() : null);
        dto.setDateEvenement(entity.getDateEvenement());
        dto.setMontant(entity.getMontant());
        dto.setCommentaire(entity.getCommentaire());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
