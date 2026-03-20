package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.MutationEmployeDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.MutationEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeSalaireRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.FonctionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.MutationEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PosteRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RegimePaieRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeCongeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.UniteOrganisationnelleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MutationEmployeServiceTest {

    @Mock
    private MutationEmployeRepository repository;
    @Mock
    private EmployeRepository employeRepository;
    @Mock
    private EntrepriseRepository entrepriseRepository;
    @Mock
    private EmploiEmployeRepository emploiEmployeRepository;
    @Mock
    private EmployeSalaireRepository employeSalaireRepository;
    @Mock
    private RegimePaieRepository regimePaieRepository;
    @Mock
    private TypeEmployeRepository typeEmployeRepository;
    @Mock
    private UniteOrganisationnelleRepository uniteOrganisationnelleRepository;
    @Mock
    private PosteRepository posteRepository;
    @Mock
    private HoraireRepository horaireRepository;
    @Mock
    private FonctionRepository fonctionRepository;
    @Mock
    private TypeCongeRepository typeCongeRepository;

    @Spy
    @InjectMocks
    private MutationEmployeService service;

    @Test
    void autoApplyApprovedForCurrentDateAppliesOnlyMatchingEntreprise() {
        MutationEmploye m1 = new MutationEmploye();
        m1.setId(1L);
        Entreprise e1 = new Entreprise();
        e1.setId(10L);
        m1.setEntreprise(e1);

        MutationEmploye m2 = new MutationEmploye();
        m2.setId(2L);
        Entreprise e2 = new Entreprise();
        e2.setId(20L);
        m2.setEntreprise(e2);

        when(repository.findByStatutAndDateEffetLessThanEqual(
                eq(MutationEmploye.StatutMutation.APPROUVE),
                eq(LocalDate.now())))
                .thenReturn(List.of(m1, m2));
        doReturn(new MutationEmployeDTO()).when(service).appliquer(eq(1L), eq("tester"));

        Map<String, Object> result = service.autoApplyApprovedForCurrentDate(10L, "tester");

        assertEquals(1, result.get("totalRows"));
        assertEquals(1, result.get("appliedRows"));
        verify(service).appliquer(1L, "tester");
    }

    @Test
    void autoCreateAndApplyReintegrationForExpiredSuspensionsUsesCreateAndApply() {
        Employe employe = new Employe();
        employe.setId(23L);
        Entreprise entreprise = new Entreprise();
        entreprise.setId(10L);
        employe.setEntreprise(entreprise);

        EmploiEmploye emploi = new EmploiEmploye();
        emploi.setId(100L);
        emploi.setEmploye(employe);
        emploi.setStatutEmploi(EmploiEmploye.StatutEmploi.SUSPENDU);
        emploi.setDateFinStatut(LocalDate.now().minusDays(1));

        when(emploiEmployeRepository.findByStatutEmploiAndDateFinStatutLessThanEqual(
                eq(EmploiEmploye.StatutEmploi.SUSPENDU),
                eq(LocalDate.now())))
                .thenReturn(List.of(emploi));

        MutationEmployeDTO created = new MutationEmployeDTO();
        created.setId(99L);
        doReturn(created).when(service).create(any(com.rsoft.hurmanagement.hurmasterdata.dto.MutationEmployeCreateDTO.class), eq("tester"));
        doReturn(new MutationEmployeDTO()).when(service).appliquer(eq(99L), eq("tester"));

        Map<String, Object> result = service.autoCreateAndApplyReintegrationForExpiredSuspensions(10L, "tester");

        assertEquals(1, result.get("totalRows"));
        assertEquals(1, result.get("createdRows"));
        assertEquals(1, result.get("appliedRows"));
        verify(service).create(any(com.rsoft.hurmanagement.hurmasterdata.dto.MutationEmployeCreateDTO.class), eq("tester"));
        verify(service).appliquer(99L, "tester");
    }
}
