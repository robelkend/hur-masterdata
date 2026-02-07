package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.ProductionPieceCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ProductionPieceDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.ProductionPieceUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionPieceService {
    private final ProductionPieceRepository repository;
    private final EmployeRepository employeRepository;
    private final TypePieceRepository typePieceRepository;
    private final TarifPieceRepository tarifPieceRepository;
    private final EmployeSalaireRepository employeSalaireRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;

    @Transactional(readOnly = true)
    public Page<ProductionPieceDTO> findByFilters(LocalDate dateDebut,
                                                  LocalDate dateFin,
                                                  Long employeId,
                                                  Long entrepriseId,
                                                  Pageable pageable) {
        return repository.findByFilters(dateDebut, dateFin, employeId, entrepriseId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ProductionPieceDTO findById(Long id) {
        ProductionPiece entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductionPiece not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional
    public ProductionPieceDTO create(ProductionPieceCreateDTO dto, String username) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        TypePiece typePiece = typePieceRepository.findById(dto.getTypePieceId())
                .orElseThrow(() -> new RuntimeException("TypePiece not found with id: " + dto.getTypePieceId()));
        Entreprise entreprise = resolveEntreprise(employe, typePiece);

        if (repository.existsByEntrepriseIdAndEmployeIdAndDateJourAndTypePieceId(
                entreprise.getId(), employe.getId(), dto.getDateJour(), typePiece.getId())) {
            throw new RuntimeException("productionPiece.error.duplicate");
        }

        ProductionPiece entity = new ProductionPiece();
        entity.setEmploye(employe);
        entity.setEntreprise(entreprise);
        entity.setTypePiece(typePiece);
        entity.setDateJour(dto.getDateJour());
        entity.setQuantite(defaultValue(dto.getQuantite()));
        entity.setQuantiteRejet(defaultValue(dto.getQuantiteRejet()));
        entity.setNote(dto.getNote());
        entity.setPayrollId(dto.getPayrollId());
        entity.setStatut(parseStatut(dto.getStatut(), ProductionPiece.StatutProduction.BROUILLON));

        applyDerived(entity, dto.getEmploiEmployeId(), dto.getEmployeSalaireId());

        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public ProductionPieceDTO update(Long id, ProductionPieceUpdateDTO dto, String username) {
        ProductionPiece entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductionPiece not found with id: " + id));
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        if (entity.getStatut() == ProductionPiece.StatutProduction.PAYE) {
            throw new RuntimeException("productionPiece.error.cannotEdit");
        }

        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        TypePiece typePiece = typePieceRepository.findById(dto.getTypePieceId())
                .orElseThrow(() -> new RuntimeException("TypePiece not found with id: " + dto.getTypePieceId()));
        Entreprise entreprise = resolveEntreprise(employe, typePiece);

        if (repository.existsDuplicate(entreprise.getId(), employe.getId(), dto.getDateJour(), typePiece.getId(), id)) {
            throw new RuntimeException("productionPiece.error.duplicate");
        }

        entity.setEmploye(employe);
        entity.setEntreprise(entreprise);
        entity.setTypePiece(typePiece);
        entity.setDateJour(dto.getDateJour());
        entity.setQuantite(defaultValue(dto.getQuantite()));
        entity.setQuantiteRejet(defaultValue(dto.getQuantiteRejet()));
        entity.setNote(dto.getNote());
        entity.setPayrollId(dto.getPayrollId());
        if (dto.getStatut() != null) {
            entity.setStatut(parseStatut(dto.getStatut(), entity.getStatut()));
        }

        applyDerived(entity, dto.getEmploiEmployeId(), dto.getEmployeSalaireId());

        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, Integer rowscn) {
        ProductionPiece entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductionPiece not found with id: " + id));
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        if (entity.getStatut() == ProductionPiece.StatutProduction.PAYE) {
            throw new RuntimeException("productionPiece.error.cannotDelete");
        }
        repository.delete(entity);
    }

    @Transactional
    public int validateByFilters(LocalDate dateDebut, LocalDate dateFin, Long employeId, Long entrepriseId, String username) {
        return repository.updateStatusByFilters(ProductionPiece.StatutProduction.VALIDE, ProductionPiece.StatutProduction.BROUILLON,
                username, OffsetDateTime.now(), dateDebut, dateFin, employeId, entrepriseId);
    }

    @Transactional
    public int cancelByFilters(LocalDate dateDebut, LocalDate dateFin, Long employeId, Long entrepriseId, String username) {
        return repository.updateStatusByFilters(ProductionPiece.StatutProduction.ANNULE, ProductionPiece.StatutProduction.BROUILLON,
                username, OffsetDateTime.now(), dateDebut, dateFin, employeId, entrepriseId);
    }

    private void applyDerived(ProductionPiece entity, Long emploiEmployeId, Long employeSalaireId) {
        BigDecimal quantiteValide = entity.getQuantite().subtract(entity.getQuantiteRejet());
        if (quantiteValide.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("productionPiece.error.invalidQuantite");
        }
        entity.setQuantiteValide(quantiteValide);

        TarifPiece tarif = resolveTarif(entity.getTypePiece().getId(), entity.getDateJour());
        entity.setDevise(tarif.getDevise());
        entity.setPrixUnitaire(tarif.getPrixUnitaire());
        entity.setMontantTotal(tarif.getPrixUnitaire().multiply(quantiteValide));

        EmployeSalaire salaire = resolveEmployeSalaire(entity.getEmploye().getId(), employeSalaireId);
        entity.setEmployeSalaire(salaire);
        if (emploiEmployeId != null) {
            EmploiEmploye emploi = emploiEmployeRepository.findById(emploiEmployeId)
                    .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiEmployeId));
            entity.setEmploiEmploye(emploi);
        } else if (salaire != null) {
            entity.setEmploiEmploye(salaire.getEmploi());
        }
    }

    private TarifPiece resolveTarif(Long typePieceId, LocalDate dateJour) {
        Pageable pageable = PageRequest.of(0, 1);
        Page<TarifPiece> tarifs = tarifPieceRepository.findActiveForDate(typePieceId, dateJour, pageable);
        if (tarifs.isEmpty()) {
            throw new RuntimeException("productionPiece.error.tarifNotFound");
        }
        return tarifs.getContent().get(0);
    }

    private EmployeSalaire resolveEmployeSalaire(Long employeId, Long employeSalaireId) {
        if (employeSalaireId != null) {
            EmployeSalaire salaire = employeSalaireRepository.findById(employeSalaireId)
                    .orElseThrow(() -> new RuntimeException("EmployeSalaire not found with id: " + employeSalaireId));
            if (!isPieceRemuneration(salaire.getRegimePaie())) {
                throw new RuntimeException("productionPiece.error.modeRemuneration");
            }
            return salaire;
        }
        List<EmployeSalaire> salaires = employeSalaireRepository.findActiveByEmployeIdAndMode(
                employeId,
                List.of(RegimePaie.ModeRemuneration.PIECE, RegimePaie.ModeRemuneration.PIECE_FIXE)
        );
        if (salaires.isEmpty()) {
            throw new RuntimeException("productionPiece.error.modeRemuneration");
        }
        return salaires.get(0);
    }

    private boolean isPieceRemuneration(RegimePaie regimePaie) {
        return regimePaie != null && (regimePaie.getModeRemuneration() == RegimePaie.ModeRemuneration.PIECE
                || regimePaie.getModeRemuneration() == RegimePaie.ModeRemuneration.PIECE_FIXE);
    }

    private BigDecimal defaultValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private ProductionPiece.StatutProduction parseStatut(String statut, ProductionPiece.StatutProduction defaultValue) {
        if (statut == null || statut.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return ProductionPiece.StatutProduction.valueOf(statut);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    private ProductionPieceDTO toDTO(ProductionPiece entity) {
        ProductionPieceDTO dto = new ProductionPieceDTO();
        dto.setId(entity.getId());
        dto.setDateJour(entity.getDateJour());
        dto.setQuantite(entity.getQuantite());
        dto.setQuantiteRejet(entity.getQuantiteRejet());
        dto.setQuantiteValide(entity.getQuantiteValide());
        dto.setPrixUnitaire(entity.getPrixUnitaire());
        dto.setMontantTotal(entity.getMontantTotal());
        dto.setStatut(entity.getStatut().name());
        dto.setNote(entity.getNote());
        dto.setPayrollId(entity.getPayrollId());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        if (entity.getEmploye() != null) {
            dto.setEmployeId(entity.getEmploye().getId());
            dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
            dto.setEmployeNom(entity.getEmploye().getNom());
            dto.setEmployePrenom(entity.getEmploye().getPrenom());
        }
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        if (entity.getTypePiece() != null) {
            dto.setTypePieceId(entity.getTypePiece().getId());
            dto.setTypePieceCode(entity.getTypePiece().getCodePiece());
            dto.setTypePieceLibelle(entity.getTypePiece().getLibelle());
        }
        if (entity.getDevise() != null) {
            dto.setDeviseId(entity.getDevise().getId());
            dto.setDeviseCode(entity.getDevise().getCodeDevise());
            dto.setDeviseDescription(entity.getDevise().getDescription());
        }
        if (entity.getEmploiEmploye() != null) {
            dto.setEmploiEmployeId(entity.getEmploiEmploye().getId());
        }
        if (entity.getEmployeSalaire() != null) {
            dto.setEmployeSalaireId(entity.getEmployeSalaire().getId());
        }
        return dto;
    }

    private Entreprise resolveEntreprise(Employe employe, TypePiece typePiece) {
        if (employe.getEntreprise() != null) {
            return employe.getEntreprise();
        }
        if (typePiece.getEntreprise() != null) {
            return typePiece.getEntreprise();
        }
        throw new RuntimeException("productionPiece.error.entrepriseMissing");
    }
}
