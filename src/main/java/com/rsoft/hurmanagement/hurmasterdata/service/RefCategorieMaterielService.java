package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.RefCategorieMaterielCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefCategorieMaterielDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.RefCategorieMaterielUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.RefCategorieMateriel;
import com.rsoft.hurmanagement.hurmasterdata.repository.RefCategorieMaterielRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefCategorieMaterielService {

    private final RefCategorieMaterielRepository repository;

    @Transactional(readOnly = true)
    public Page<RefCategorieMaterielDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<RefCategorieMaterielDTO> findAllForDropdown() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .sorted((a, b) -> a.getCodeCategorie().compareToIgnoreCase(b.getCodeCategorie()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RefCategorieMaterielDTO findById(String codeCategorie) {
        return toDTO(repository.findById(codeCategorie)
                .orElseThrow(() -> new RuntimeException("RefCategorieMateriel not found with code: " + codeCategorie)));
    }

    @Transactional
    public RefCategorieMaterielDTO create(RefCategorieMaterielCreateDTO dto, String username) {
        RefCategorieMateriel entity = new RefCategorieMateriel();
        entity.setCodeCategorie(dto.getCodeCategorie());
        entity.setLibelle(dto.getLibelle());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        return toDTO(repository.save(entity));
    }

    @Transactional
    public RefCategorieMaterielDTO update(String codeCategorie, RefCategorieMaterielUpdateDTO dto, String username) {
        RefCategorieMateriel entity = repository.findById(codeCategorie)
                .orElseThrow(() -> new RuntimeException("RefCategorieMateriel not found with code: " + codeCategorie));
        entity.setLibelle(dto.getLibelle());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(String codeCategorie) {
        RefCategorieMateriel entity = repository.findById(codeCategorie)
                .orElseThrow(() -> new RuntimeException("RefCategorieMateriel not found with code: " + codeCategorie));
        repository.delete(entity);
    }

    private RefCategorieMaterielDTO toDTO(RefCategorieMateriel entity) {
        RefCategorieMaterielDTO dto = new RefCategorieMaterielDTO();
        dto.setCodeCategorie(entity.getCodeCategorie());
        dto.setLibelle(entity.getLibelle());
        return dto;
    }
}
