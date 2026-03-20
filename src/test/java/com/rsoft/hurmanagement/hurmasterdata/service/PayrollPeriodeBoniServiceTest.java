package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollPeriodeBoniCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollPeriodeBoniDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollPeriodeBoniUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollPeriodeBoni;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollPeriodeBoniRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollPeriodeBoniServiceTest {

    @Mock
    private PayrollPeriodeBoniRepository repository;

    @InjectMocks
    private PayrollPeriodeBoniService service;

    @Test
    void createGeneratesCodeWhenMissingAndDeactivatesOthersWhenActif() {
        PayrollPeriodeBoniCreateDTO dto = new PayrollPeriodeBoniCreateDTO();
        dto.setLibelle("Boni Fin Annee");
        dto.setDateDebut(LocalDate.parse("2025-12-01"));
        dto.setDateFin(LocalDate.parse("2026-01-15"));
        dto.setStatut(PayrollPeriodeBoni.Statut.ACTIF);

        PayrollPeriodeBoni saved = new PayrollPeriodeBoni();
        saved.setId(10L);
        saved.setCode("2025-2026");
        saved.setLibelle(dto.getLibelle());
        saved.setDateDebut(dto.getDateDebut());
        saved.setDateFin(dto.getDateFin());
        saved.setStatut(PayrollPeriodeBoni.Statut.ACTIF);
        saved.setCreatedBy("tester");
        saved.setCreatedOn(OffsetDateTime.now());
        saved.setUpdatedOn(OffsetDateTime.now());
        saved.setRowscn(1);

        when(repository.existsByCodeIgnoreCase("2025-2026")).thenReturn(false);
        when(repository.save(any(PayrollPeriodeBoni.class))).thenReturn(saved);

        PayrollPeriodeBoniDTO result = service.create(dto, "tester");

        assertNotNull(result);
        assertEquals("2025-2026", result.getCode());
        assertEquals(PayrollPeriodeBoni.Statut.ACTIF, result.getStatut());
        verify(repository).deactivateOtherActive(-1L);
    }

    @Test
    void updateKeepsSingleActive() {
        PayrollPeriodeBoni existing = new PayrollPeriodeBoni();
        existing.setId(11L);
        existing.setCode("2025-2025");
        existing.setLibelle("Old");
        existing.setDateDebut(LocalDate.parse("2025-01-01"));
        existing.setDateFin(LocalDate.parse("2025-12-31"));
        existing.setStatut(PayrollPeriodeBoni.Statut.INACTIF);
        existing.setRowscn(3);

        PayrollPeriodeBoniUpdateDTO dto = new PayrollPeriodeBoniUpdateDTO();
        dto.setRowscn(3);
        dto.setCode("2025-2025");
        dto.setLibelle("New");
        dto.setDateDebut(LocalDate.parse("2025-01-01"));
        dto.setDateFin(LocalDate.parse("2025-12-31"));
        dto.setStatut(PayrollPeriodeBoni.Statut.ACTIF);

        when(repository.findById(11L)).thenReturn(Optional.of(existing));
        when(repository.existsByCodeIgnoreCaseAndIdNot("2025-2025", 11L)).thenReturn(false);
        when(repository.save(any(PayrollPeriodeBoni.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollPeriodeBoniDTO result = service.update(11L, dto, "tester");

        assertEquals(PayrollPeriodeBoni.Statut.ACTIF, result.getStatut());
        verify(repository).deactivateOtherActive(11L);
    }
}
