package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.TarifPieceCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TarifPieceDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TarifPieceUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Devise;
import com.rsoft.hurmanagement.hurmasterdata.entity.TarifPiece;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypePiece;
import com.rsoft.hurmanagement.hurmasterdata.repository.DeviseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TarifPieceRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypePieceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TarifPieceService {
    private final TarifPieceRepository repository;
    private final TypePieceRepository typePieceRepository;
    private final DeviseRepository deviseRepository;

    @Transactional(readOnly = true)
    public Page<TarifPieceDTO> findByTypePieceId(Long typePieceId, Pageable pageable) {
        return repository.findByTypePieceId(typePieceId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public TarifPieceDTO findById(Long id) {
        TarifPiece entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TarifPiece not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional
    public TarifPieceDTO create(TarifPieceCreateDTO dto, String username) {
        TypePiece typePiece = typePieceRepository.findById(dto.getTypePieceId())
                .orElseThrow(() -> new RuntimeException("TypePiece not found with id: " + dto.getTypePieceId()));
        Devise devise = deviseRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));

        validateDateRange(dto.getDateEffectif(), dto.getDateFin());
        validateNoActiveConflict(typePiece.getId(), dto.getDateEffectif(), dto.getDateFin(), null, dto.getActif());

        TarifPiece entity = new TarifPiece();
        entity.setTypePiece(typePiece);
        entity.setDevise(devise);
        entity.setPrixUnitaire(dto.getPrixUnitaire());
        entity.setDateEffectif(dto.getDateEffectif());
        entity.setDateFin(dto.getDateFin());
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public TarifPieceDTO update(Long id, TarifPieceUpdateDTO dto, String username) {
        TarifPiece entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TarifPiece not found with id: " + id));
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        TypePiece typePiece = typePieceRepository.findById(dto.getTypePieceId())
                .orElseThrow(() -> new RuntimeException("TypePiece not found with id: " + dto.getTypePieceId()));
        Devise devise = deviseRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));

        validateDateRange(dto.getDateEffectif(), dto.getDateFin());
        validateNoActiveConflict(typePiece.getId(), dto.getDateEffectif(), dto.getDateFin(), id, dto.getActif());

        entity.setTypePiece(typePiece);
        entity.setDevise(devise);
        entity.setPrixUnitaire(dto.getPrixUnitaire());
        entity.setDateEffectif(dto.getDateEffectif());
        entity.setDateFin(dto.getDateFin());
        entity.setActif(dto.getActif() != null ? dto.getActif() : entity.getActif());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, Integer rowscn) {
        TarifPiece entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TarifPiece not found with id: " + id));
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        repository.delete(entity);
    }

    private void validateDateRange(LocalDate dateEffectif, LocalDate dateFin) {
        if (dateFin != null && dateFin.isBefore(dateEffectif)) {
            throw new RuntimeException("Date fin must be >= date effectif");
        }
    }

    private void validateNoActiveConflict(Long typePieceId, LocalDate dateEffectif, LocalDate dateFin, Long excludeId, String actif) {
        String actifValue = actif != null ? actif : "Y";
        if (!"Y".equalsIgnoreCase(actifValue)) {
            return;
        }
        LocalDate end = dateFin != null ? dateFin : LocalDate.of(9999, 12, 31);
        boolean conflict = repository.existsActiveOverlap(typePieceId, dateEffectif, end, excludeId);
        if (conflict) {
            throw new RuntimeException("Active tarif conflict for the same date range");
        }
    }

    private TarifPieceDTO toDTO(TarifPiece entity) {
        TarifPieceDTO dto = new TarifPieceDTO();
        dto.setId(entity.getId());
        dto.setPrixUnitaire(entity.getPrixUnitaire());
        dto.setDateEffectif(entity.getDateEffectif());
        dto.setDateFin(entity.getDateFin());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
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
        return dto;
    }
}
