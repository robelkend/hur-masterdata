package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.TypePieceCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypePieceDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.TypePieceUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypePiece;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypePieceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TypePieceService {
    private final TypePieceRepository repository;
    private final EntrepriseRepository entrepriseRepository;

    @Transactional(readOnly = true)
    public Page<TypePieceDTO> findAll(Pageable pageable, String codePiece, Long entrepriseId) {
        return repository.findAllWithFilters(codePiece, entrepriseId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public TypePieceDTO findById(Long id) {
        TypePiece entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypePiece not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional
    public TypePieceDTO create(TypePieceCreateDTO dto, String username) {
        if (repository.existsByEntrepriseIdAndCodePiece(dto.getEntrepriseId(), dto.getCodePiece())) {
            throw new RuntimeException("TypePiece with code " + dto.getCodePiece() + " already exists for entreprise");
        }
        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));

        TypePiece entity = new TypePiece();
        entity.setEntreprise(entreprise);
        entity.setCodePiece(dto.getCodePiece());
        entity.setLibelle(dto.getLibelle());
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public TypePieceDTO update(Long id, TypePieceUpdateDTO dto, String username) {
        TypePiece entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypePiece not found with id: " + id));
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        if (!entity.getEntreprise().getId().equals(dto.getEntrepriseId()) &&
                repository.existsByEntrepriseIdAndCodePiece(dto.getEntrepriseId(), entity.getCodePiece())) {
            throw new RuntimeException("TypePiece with code " + entity.getCodePiece() + " already exists for entreprise");
        }
        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + dto.getEntrepriseId()));

        entity.setEntreprise(entreprise);
        entity.setLibelle(dto.getLibelle());
        entity.setActif(dto.getActif() != null ? dto.getActif() : entity.getActif());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, Integer rowscn) {
        TypePiece entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TypePiece not found with id: " + id));
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        repository.delete(entity);
    }

    private TypePieceDTO toDTO(TypePiece entity) {
        TypePieceDTO dto = new TypePieceDTO();
        dto.setId(entity.getId());
        dto.setCodePiece(entity.getCodePiece());
        dto.setLibelle(entity.getLibelle());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        return dto;
    }
}
