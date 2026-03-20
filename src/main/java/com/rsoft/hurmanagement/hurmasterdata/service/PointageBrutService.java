package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PointageBrutCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PointageBrutDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PointageBrutUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PointageBrutUsageDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoading;
import com.rsoft.hurmanagement.hurmasterdata.entity.PointageBrut;
import com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceLoadingRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PointageBrutRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PresenceEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointageBrutService {

    private final PointageBrutRepository repository;
    private final EmployeRepository employeRepository;
    private final PresenceEmployeRepository presenceEmployeRepository;
    private final InterfaceLoadingRepository interfaceLoadingRepository;
    private final InterfaceLoadingService interfaceLoadingService;

    @Transactional(readOnly = true)
    public Page<PointageBrutDTO> findByFilters(String dateDebut, String dateFin, Long employeId, Long entrepriseId, Pageable pageable) {
        LocalDate dateDebutLocal = LocalDate.parse(dateDebut);
        LocalDate dateFinLocal = LocalDate.parse(dateFin);
        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime start = dateDebutLocal.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endExclusive = dateFinLocal.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
        return repository.findByFilters(start, endExclusive, employeId, entrepriseId, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public PointageBrutDTO findById(Long id) {
        PointageBrut entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PointageBrut not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<PointageBrutUsageDTO> findByPresenceEmployeId(Long presenceEmployeId) {
        return repository.findByPresenceEmployeIdOrderByDateHeurePointageAsc(presenceEmployeId)
                .stream()
                .map(this::toUsageDTO)
                .toList();
    }

    @Transactional
    public PointageBrutDTO create(PointageBrutCreateDTO dto, String username) {
        PointageBrut entity = new PointageBrut();
        applyDto(entity, dto.getEmployeId(), dto.getPresenceEmployeId());
        entity.setSystemeSource(defaultString(dto.getSystemeSource(), "HORODATEUR"));
        entity.setIdPointageSource(dto.getIdPointageSource());
        entity.setIdAppareil(dto.getIdAppareil());
        entity.setIdBadge(dto.getIdBadge());
        entity.setDateHeurePointage(dto.getDateHeurePointage());
        entity.setTypeEvenement(parseTypeEvenement(dto.getTypeEvenement(), PointageBrut.TypeEvenement.UNKNOWN));
        entity.setQualitePointage(parseQualite(dto.getQualitePointage(), PointageBrut.QualitePointage.BRUT));
        entity.setMotifRejet(dto.getMotifRejet());
        entity.setStatutTraitement(parseStatut(dto.getStatutTraitement(), PointageBrut.StatutTraitement.BRUT));
        entity.setTraiteLe(dto.getTraiteLe());
        entity.setTraitePar(dto.getTraitePar());
        entity.setImporteLe(OffsetDateTime.now());
        entity.setImportePar(defaultString(dto.getImportePar(), username != null ? username : "SYSTEM"));
        entity.setRowscn(1);
        return toDTO(repository.save(entity));
    }

    @Transactional
    public PointageBrutDTO update(Long id, PointageBrutUpdateDTO dto, String username) {
        PointageBrut entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PointageBrut not found with id: " + id));
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }

        if (dto.getEmployeId() != null || dto.getPresenceEmployeId() != null) {
            applyDto(entity, dto.getEmployeId(), dto.getPresenceEmployeId());
        }
        if (dto.getSystemeSource() != null) {
            entity.setSystemeSource(dto.getSystemeSource());
        }
        if (dto.getIdPointageSource() != null) {
            entity.setIdPointageSource(dto.getIdPointageSource());
        }
        if (dto.getIdAppareil() != null) {
            entity.setIdAppareil(dto.getIdAppareil());
        }
        if (dto.getIdBadge() != null) {
            entity.setIdBadge(dto.getIdBadge());
        }
        if (dto.getDateHeurePointage() != null) {
            entity.setDateHeurePointage(dto.getDateHeurePointage());
        }
        if (dto.getTypeEvenement() != null) {
            entity.setTypeEvenement(parseTypeEvenement(dto.getTypeEvenement(), entity.getTypeEvenement()));
        }
        if (dto.getQualitePointage() != null) {
            entity.setQualitePointage(parseQualite(dto.getQualitePointage(), entity.getQualitePointage()));
        }
        if (dto.getMotifRejet() != null) {
            entity.setMotifRejet(dto.getMotifRejet());
        }
        if (dto.getStatutTraitement() != null) {
            entity.setStatutTraitement(parseStatut(dto.getStatutTraitement(), entity.getStatutTraitement()));
        }
        if (dto.getTraiteLe() != null) {
            entity.setTraiteLe(dto.getTraiteLe());
        }
        if (dto.getTraitePar() != null) {
            entity.setTraitePar(dto.getTraitePar());
        }
        if (dto.getImportePar() != null) {
            entity.setImportePar(dto.getImportePar());
        }
        entity.setRowscn(entity.getRowscn() + 1);
        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, Integer rowscn) {
        PointageBrut entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PointageBrut not found with id: " + id));
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        repository.delete(entity);
    }

    @Transactional
    public java.util.Map<String, Object> loadFromClockInterface(String username) {
        InterfaceLoading loading = interfaceLoadingRepository.findByCodeLoading("CLOCK_INOUT")
                .orElseThrow(() -> new RuntimeException("InterfaceLoading not found for code CLOCK_INOUT"));
        if (loading.getSource() == InterfaceLoading.Source.RDB) {
            return interfaceLoadingService.loadFromDatabase(loading.getId(), username);
        }
        if (loading.getSource() == InterfaceLoading.Source.API) {
            return interfaceLoadingService.loadFromApi(loading.getId(), username);
        }
        throw new RuntimeException("CLOCK_INOUT is not configured for RDB or API source");
    }

    private void applyDto(PointageBrut entity, Long employeId, Long presenceEmployeId) {
        if (employeId != null) {
            Employe employe = employeRepository.findById(employeId)
                    .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
            entity.setEmploye(employe);
            entity.setEntreprise(resolveEntreprise(employe));
        }
        if (presenceEmployeId != null) {
            PresenceEmploye presence = presenceEmployeRepository.findById(presenceEmployeId)
                    .orElseThrow(() -> new RuntimeException("PresenceEmploye not found with id: " + presenceEmployeId));
            entity.setPresenceEmploye(presence);
        }
    }

    private Entreprise resolveEntreprise(Employe employe) {
        return employe != null ? employe.getEntreprise() : null;
    }

    private PointageBrutDTO toDTO(PointageBrut entity) {
        PointageBrutDTO dto = new PointageBrutDTO();
        dto.setId(entity.getId());
        if (entity.getEmploye() != null) {
            dto.setEmployeId(entity.getEmploye().getId());
            dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
            dto.setEmployeNom(entity.getEmploye().getNom());
            dto.setEmployePrenom(entity.getEmploye().getPrenom());
        }
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
        }
        dto.setSystemeSource(entity.getSystemeSource());
        dto.setIdPointageSource(entity.getIdPointageSource());
        dto.setIdAppareil(entity.getIdAppareil());
        dto.setIdBadge(entity.getIdBadge());
        dto.setDateHeurePointage(entity.getDateHeurePointage());
        dto.setTypeEvenement(entity.getTypeEvenement() != null ? entity.getTypeEvenement().name() : null);
        dto.setQualitePointage(entity.getQualitePointage() != null ? entity.getQualitePointage().name() : null);
        dto.setMotifRejet(entity.getMotifRejet());
        dto.setStatutTraitement(entity.getStatutTraitement() != null ? entity.getStatutTraitement().name() : null);
        if (entity.getPresenceEmploye() != null) {
            dto.setPresenceEmployeId(entity.getPresenceEmploye().getId());
        }
        dto.setTraiteLe(entity.getTraiteLe());
        dto.setTraitePar(entity.getTraitePar());
        dto.setImporteLe(entity.getImporteLe());
        dto.setImportePar(entity.getImportePar());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private PointageBrutUsageDTO toUsageDTO(PointageBrut entity) {
        PointageBrutUsageDTO dto = new PointageBrutUsageDTO();
        dto.setSystemeSource(entity.getSystemeSource());
        dto.setIdAppareil(entity.getIdAppareil());
        dto.setIdBadge(entity.getIdBadge());
        dto.setDateHeurePointage(entity.getDateHeurePointage());
        dto.setQualitePointage(entity.getQualitePointage() != null ? entity.getQualitePointage().name() : null);
        dto.setStatutTraitement(entity.getStatutTraitement() != null ? entity.getStatutTraitement().name() : null);
        return dto;
    }

    private PointageBrut.TypeEvenement parseTypeEvenement(String value, PointageBrut.TypeEvenement fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return PointageBrut.TypeEvenement.valueOf(value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private PointageBrut.QualitePointage parseQualite(String value, PointageBrut.QualitePointage fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return PointageBrut.QualitePointage.valueOf(value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private PointageBrut.StatutTraitement parseStatut(String value, PointageBrut.StatutTraitement fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return PointageBrut.StatutTraitement.valueOf(value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
