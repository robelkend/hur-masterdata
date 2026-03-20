package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.RefMaterielCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefMaterielDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefMaterielUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.RefMateriel;
import com.rsoft.hurmanagement.hurmasterdata.repository.RefMaterielRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefMaterielService {

    private final RefMaterielRepository repository;

    @Transactional(readOnly = true)
    public Page<RefMaterielDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<RefMaterielDTO> findAllForDropdown() {
        return repository.findByActif("Y", Sort.by(Sort.Direction.ASC, "codeMateriel"))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RefMaterielDTO findById(Long id) {
        return toDTO(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RefMateriel not found with id: " + id)));
    }

    @Transactional
    public RefMaterielDTO create(RefMaterielCreateDTO dto, String username) {
        RefMateriel entity = new RefMateriel();
        apply(dto, entity);
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        return toDTO(repository.save(entity));
    }

    @Transactional
    public RefMaterielDTO update(Long id, RefMaterielUpdateDTO dto, String username) {
        RefMateriel entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RefMateriel not found with id: " + id));
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        apply(dto, entity);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, Integer rowscn) {
        RefMateriel entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RefMateriel not found with id: " + id));
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before deleting.");
        }
        repository.delete(entity);
    }

    private void apply(RefMaterielCreateDTO dto, RefMateriel entity) {
        entity.setCodeMateriel(dto.getCodeMateriel());
        entity.setLibelle(dto.getLibelle());
        entity.setCategorie(dto.getCategorie() != null ? dto.getCategorie() : "AUTRE");
        entity.setDepreciable(dto.getDepreciable() != null ? dto.getDepreciable() : "Y");
        entity.setDureeDepreciationMois(dto.getDureeDepreciationMois() != null ? dto.getDureeDepreciationMois() : 60);
        entity.setDureeTransfertProprieteMois(dto.getDureeTransfertProprieteMois() != null ? dto.getDureeTransfertProprieteMois() : 60);
        entity.setValeurReference(dto.getValeurReference() != null ? dto.getValeurReference() : BigDecimal.ZERO);
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
    }

    private void apply(RefMaterielUpdateDTO dto, RefMateriel entity) {
        entity.setCodeMateriel(dto.getCodeMateriel());
        entity.setLibelle(dto.getLibelle());
        entity.setCategorie(dto.getCategorie() != null ? dto.getCategorie() : "AUTRE");
        entity.setDepreciable(dto.getDepreciable() != null ? dto.getDepreciable() : "Y");
        entity.setDureeDepreciationMois(dto.getDureeDepreciationMois() != null ? dto.getDureeDepreciationMois() : 60);
        entity.setDureeTransfertProprieteMois(dto.getDureeTransfertProprieteMois() != null ? dto.getDureeTransfertProprieteMois() : 60);
        entity.setValeurReference(dto.getValeurReference() != null ? dto.getValeurReference() : BigDecimal.ZERO);
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
    }

    private RefMaterielDTO toDTO(RefMateriel entity) {
        RefMaterielDTO dto = new RefMaterielDTO();
        dto.setId(entity.getId());
        dto.setCodeMateriel(entity.getCodeMateriel());
        dto.setLibelle(entity.getLibelle());
        dto.setCategorie(entity.getCategorie());
        dto.setDepreciable(entity.getDepreciable());
        dto.setDureeDepreciationMois(entity.getDureeDepreciationMois());
        dto.setDureeTransfertProprieteMois(entity.getDureeTransfertProprieteMois());
        dto.setValeurReference(entity.getValeurReference());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
