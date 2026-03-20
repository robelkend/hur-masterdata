package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.CongeEmployeCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.CongeEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.CongeEmployeUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.CongeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.JourConge;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeConge;
import com.rsoft.hurmanagement.hurmasterdata.repository.CongeEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.JourCongeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PresenceEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeCongeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CongeEmployeServiceTest {

    @Mock
    private CongeEmployeRepository repository;
    @Mock
    private EmployeRepository employeRepository;
    @Mock
    private EntrepriseRepository entrepriseRepository;
    @Mock
    private TypeCongeRepository typeCongeRepository;
    @Mock
    private EmploiEmployeRepository emploiEmployeRepository;
    @Mock
    private PresenceEmployeRepository presenceEmployeRepository;
    @Mock
    private JourCongeRepository jourCongeRepository;

    @InjectMocks
    private CongeEmployeService service;

    @Test
    void createExcludesOffDaysAndHolidaysForPlannedAndRealDays() {
        Employe employe = buildEmploye(23L);
        TypeConge typeConge = buildTypeConge(1L);
        EmploiEmploye emploi = buildEmploiEmploye(10L, employe, 7);

        CongeEmployeCreateDTO dto = new CongeEmployeCreateDTO();
        dto.setEmployeId(23L);
        dto.setEmploiEmployeId(10L);
        dto.setTypeCongeId(1L);
        dto.setDateDebutPlan(LocalDate.parse("2025-12-31"));
        dto.setDateFinPlan(LocalDate.parse("2026-01-12"));
        dto.setDateDebutReel(LocalDate.parse("2025-12-31"));
        dto.setDateFinReel(LocalDate.parse("2026-01-12"));
        dto.setStatut("BROUILLON");

        when(employeRepository.findById(23L)).thenReturn(Optional.of(employe));
        when(typeCongeRepository.findById(1L)).thenReturn(Optional.of(typeConge));
        when(emploiEmployeRepository.findById(10L)).thenReturn(Optional.of(emploi));
        stubHolidays();
        when(repository.save(any(CongeEmploye.class))).thenAnswer(invocation -> {
            CongeEmploye entity = invocation.getArgument(0);
            entity.setId(100L);
            return entity;
        });

        CongeEmployeDTO result = service.create(dto, "tester");

        assertEquals("9.00", result.getNbJoursPlan().toPlainString());
        assertEquals("9.00", result.getNbJoursReel().toPlainString());
    }

    @Test
    void updateExcludesOffDaysAndHolidaysForPlannedAndRealDays() {
        Employe employe = buildEmploye(23L);
        TypeConge typeConge = buildTypeConge(1L);
        EmploiEmploye emploi = buildEmploiEmploye(10L, employe, 7);
        CongeEmploye existing = new CongeEmploye();
        existing.setId(200L);
        existing.setRowscn(1);
        existing.setStatut(CongeEmploye.StatutConge.BROUILLON);
        existing.setEmploye(employe);
        existing.setTypeConge(typeConge);
        existing.setEmploiEmploye(emploi);

        CongeEmployeUpdateDTO dto = new CongeEmployeUpdateDTO();
        dto.setId(200L);
        dto.setRowscn(1);
        dto.setEmployeId(23L);
        dto.setEmploiEmployeId(10L);
        dto.setTypeCongeId(1L);
        dto.setDateDebutPlan(LocalDate.parse("2025-12-31"));
        dto.setDateFinPlan(LocalDate.parse("2026-01-12"));
        dto.setDateDebutReel(LocalDate.parse("2025-12-31"));
        dto.setDateFinReel(LocalDate.parse("2026-01-12"));
        dto.setStatut("BROUILLON");

        when(repository.findById(200L)).thenReturn(Optional.of(existing));
        when(employeRepository.findById(23L)).thenReturn(Optional.of(employe));
        when(typeCongeRepository.findById(1L)).thenReturn(Optional.of(typeConge));
        when(emploiEmployeRepository.findById(10L)).thenReturn(Optional.of(emploi));
        stubHolidays();
        when(repository.save(any(CongeEmploye.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CongeEmployeDTO result = service.update(200L, dto, "tester");

        assertEquals("9.00", result.getNbJoursPlan().toPlainString());
        assertEquals("9.00", result.getNbJoursReel().toPlainString());
    }

    private void stubHolidays() {
        when(jourCongeRepository.existsByDateCongeAndActif(any(LocalDate.class), eq(JourConge.Actif.Y)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(0);
                    return LocalDate.parse("2026-01-01").equals(date) || LocalDate.parse("2026-01-02").equals(date);
                });
    }

    private Employe buildEmploye(Long id) {
        Employe employe = new Employe();
        employe.setId(id);
        employe.setCodeEmploye("E" + id);
        employe.setNom("Nom");
        employe.setPrenom("Prenom");
        return employe;
    }

    private TypeConge buildTypeConge(Long id) {
        TypeConge typeConge = new TypeConge();
        typeConge.setId(id);
        typeConge.setCodeConge("CG");
        typeConge.setDescription("Conge");
        return typeConge;
    }

    private EmploiEmploye buildEmploiEmploye(Long id, Employe employe, Integer jourOff) {
        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setId(id);
        emploi.setEmploye(employe);
        emploi.setJourOff1(jourOff);
        return emploi;
    }
}
