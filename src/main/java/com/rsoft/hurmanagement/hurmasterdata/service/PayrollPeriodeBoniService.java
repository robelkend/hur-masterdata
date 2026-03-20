package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollPeriodeBoniCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollPeriodeBoniDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollPeriodeBoniUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollPeriodeBoni;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollPeriodeBoniRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PayrollPeriodeBoniService {

    private final PayrollPeriodeBoniRepository repository;

    @Transactional(readOnly = true)
    public Page<PayrollPeriodeBoniDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public PayrollPeriodeBoniDTO findById(Long id) {
        PayrollPeriodeBoni entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayrollPeriodeBoni not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional
    public PayrollPeriodeBoniDTO create(PayrollPeriodeBoniCreateDTO dto, String username) {
        validateDates(dto.getDateDebut(), dto.getDateFin());
        String code = normalizeCodeOrGenerate(dto.getCode(), dto.getDateDebut(), dto.getDateFin());

        if (repository.existsByCodeIgnoreCase(code)) {
            throw new RuntimeException("PayrollPeriodeBoni with code " + code + " already exists");
        }

        PayrollPeriodeBoni entity = new PayrollPeriodeBoni();
        entity.setCode(code);
        entity.setLibelle(dto.getLibelle().trim());
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setStatut(dto.getStatut());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(1);

        if (entity.getStatut() == PayrollPeriodeBoni.Statut.ACTIF) {
            repository.deactivateOtherActive(-1L);
        }
        PayrollPeriodeBoni saved = repository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public PayrollPeriodeBoniDTO update(Long id, PayrollPeriodeBoniUpdateDTO dto, String username) {
        PayrollPeriodeBoni entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayrollPeriodeBoni not found with id: " + id));

        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }

        validateDates(dto.getDateDebut(), dto.getDateFin());
        String code = normalizeCodeOrGenerate(dto.getCode(), dto.getDateDebut(), dto.getDateFin());

        if (repository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new RuntimeException("PayrollPeriodeBoni with code " + code + " already exists");
        }

        entity.setCode(code);
        entity.setLibelle(dto.getLibelle().trim());
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setStatut(dto.getStatut());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);

        if (entity.getStatut() == PayrollPeriodeBoni.Statut.ACTIF) {
            repository.deactivateOtherActive(entity.getId());
        }
        PayrollPeriodeBoni saved = repository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id, Integer rowscn) {
        PayrollPeriodeBoni entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayrollPeriodeBoni not found with id: " + id));

        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }

        repository.delete(entity);
    }

    private void validateDates(java.time.LocalDate dateDebut, java.time.LocalDate dateFin) {
        if (dateDebut != null && dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("dateFin must be greater than or equal to dateDebut");
        }
    }

    private String normalizeCodeOrGenerate(String code, java.time.LocalDate dateDebut, java.time.LocalDate dateFin) {
        if (code != null && !code.trim().isEmpty()) {
            return code.trim();
        }
        if (dateDebut == null || dateFin == null) {
            throw new RuntimeException("Cannot auto-generate code without dateDebut and dateFin");
        }
        return dateDebut.getYear() + "-" + dateFin.getYear();
    }

    private PayrollPeriodeBoniDTO toDTO(PayrollPeriodeBoni entity) {
        PayrollPeriodeBoniDTO dto = new PayrollPeriodeBoniDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setLibelle(entity.getLibelle());
        dto.setDateDebut(entity.getDateDebut());
        dto.setDateFin(entity.getDateFin());
        dto.setStatut(entity.getStatut());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
