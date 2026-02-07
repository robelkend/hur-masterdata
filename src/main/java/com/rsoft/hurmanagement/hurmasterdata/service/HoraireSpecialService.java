package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.HoraireSpecialCreateDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.HoraireSpecialDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.HoraireSpecialUpdateDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireSpecial;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireSpecialRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HoraireSpecialService {
    
    private final HoraireSpecialRepository repository;
    private final EmployeRepository employeRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    
    @Transactional(readOnly = true)
    public Page<HoraireSpecialDTO> findAll(Pageable pageable, Long employeId, LocalDate dateDebutFrom, LocalDate dateDebutTo) {
        return repository.findByCriteria(employeId, null, dateDebutFrom, dateDebutTo, pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<HoraireSpecialDTO> findByEmployeId(Long employeId, Pageable pageable) {
        return repository.findByEmployeId(employeId, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public HoraireSpecialDTO findById(Long id) {
        HoraireSpecial entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("HoraireSpecial not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public HoraireSpecialDTO create(HoraireSpecialCreateDTO dto, String username) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        
        // Validate date range
        if (dto.getDateFin() != null && dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new RuntimeException("Date fin must be >= date debut");
        }
        
        validateTimeRange(dto.getDateDebut(), dto.getDateFin(), dto.getHeureDebut(), dto.getHeureFin());
        
        HoraireSpecial entity = new HoraireSpecial();
        entity.setEmploye(employe);
        entity.setEmploiEmploye(resolveEmploiEmploye(dto.getEmployeId(), dto.getEmploiEmployeId()));
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setHeureDebut(dto.getHeureDebut());
        entity.setHeureFin(dto.getHeureFin());
        entity.setPriorite(HoraireSpecial.Priorite.valueOf(dto.getPriorite()));
        HoraireSpecial.Frequence frequence = HoraireSpecial.Frequence.valueOf(dto.getFrequence());
        entity.setFrequence(frequence);
        entity.setUniteFreq(normalizeUniteFreq(frequence, dto.getUniteFreq()));
        entity.setActif(dto.getActif());
        entity.setDuplique(dto.getDuplique() != null ? dto.getDuplique() : "N");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public HoraireSpecialDTO update(Long id, HoraireSpecialUpdateDTO dto, String username) {
        HoraireSpecial entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("HoraireSpecial not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + dto.getEmployeId()));
        
        // Validate date range
        if (dto.getDateFin() != null && dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new RuntimeException("Date fin must be >= date debut");
        }
        
        validateTimeRange(dto.getDateDebut(), dto.getDateFin(), dto.getHeureDebut(), dto.getHeureFin());
        
        entity.setEmploye(employe);
        entity.setEmploiEmploye(resolveEmploiEmploye(dto.getEmployeId(), dto.getEmploiEmployeId()));
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setHeureDebut(dto.getHeureDebut());
        entity.setHeureFin(dto.getHeureFin());
        entity.setPriorite(HoraireSpecial.Priorite.valueOf(dto.getPriorite()));
        HoraireSpecial.Frequence frequence = HoraireSpecial.Frequence.valueOf(dto.getFrequence());
        entity.setFrequence(frequence);
        entity.setUniteFreq(normalizeUniteFreq(frequence, dto.getUniteFreq()));
        entity.setActif(dto.getActif());
        if (dto.getDuplique() != null) {
            entity.setDuplique(dto.getDuplique());
        }
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(repository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        HoraireSpecial entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("HoraireSpecial not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }

    @Transactional
    public int dupliquerParCritere(Long employeId, LocalDate dateDebutFrom, LocalDate dateDebutTo, String username) {
        return dupliquerParCritere(null, employeId, dateDebutFrom, dateDebutTo, username);
    }

    @Transactional
    public int dupliquerParCritere(Long entrepriseId, Long employeId, LocalDate dateDebutFrom, LocalDate dateDebutTo, String username) {
        List<HoraireSpecial> sources = repository.findByCriteria(employeId, entrepriseId, dateDebutFrom, dateDebutTo);
        LocalDate today = LocalDate.now();
        int duplicated = 0;

        for (HoraireSpecial source : sources) {
            if (source.getDateDebut() == null || source.getDateDebut().isAfter(today)) {
                continue;
            }
            if (source.getFrequence() == HoraireSpecial.Frequence.AUCUN) {
                continue;
            }

            LocalDate newDateDebut = addByFrequence(source.getDateDebut(), source.getFrequence(), source.getUniteFreq());
            LocalDate newDateFin = source.getDateFin() != null
                    ? addByFrequence(source.getDateFin(), source.getFrequence(), source.getUniteFreq())
                    : null;

            HoraireSpecial clone = new HoraireSpecial();
            clone.setEmploye(source.getEmploye());
            clone.setEmploiEmploye(source.getEmploiEmploye());
            clone.setDateDebut(newDateDebut);
            clone.setDateFin(newDateFin);
            clone.setHeureDebut(source.getHeureDebut());
            clone.setHeureFin(source.getHeureFin());
            clone.setPriorite(source.getPriorite());
            clone.setFrequence(source.getFrequence());
            clone.setUniteFreq(source.getUniteFreq());
            clone.setActif(source.getActif());
            clone.setDuplique("Y");
            clone.setCreatedBy(username);
            clone.setCreatedOn(OffsetDateTime.now());
            clone.setRowscn(1);

            repository.save(clone);
            duplicated++;
        }

        return duplicated;
    }

    private void validateTimeRange(java.time.LocalDate dateDebut,
                                   java.time.LocalDate dateFin,
                                   String heureDebut,
                                   String heureFin) {
        if (heureDebut == null || heureFin == null) {
            return;
        }
        if (dateFin != null && dateDebut != null && dateFin.isAfter(dateDebut)) {
            // Multi-day range: allow end time earlier than start time
            return;
        }
        String[] debutParts = heureDebut.split(":");
        String[] finParts = heureFin.split(":");
        if (debutParts.length == 2 && finParts.length == 2) {
            int debutMinutes = Integer.parseInt(debutParts[0]) * 60 + Integer.parseInt(debutParts[1]);
            int finMinutes = Integer.parseInt(finParts[0]) * 60 + Integer.parseInt(finParts[1]);
            if (finMinutes <= debutMinutes) {
                throw new RuntimeException("Heure fin must be > heure debut");
            }
        }
    }
    
    private HoraireSpecialDTO toDTO(HoraireSpecial entity) {
        HoraireSpecialDTO dto = new HoraireSpecialDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye() != null ? entity.getEmploye().getId() : null);
        dto.setEmployeCode(entity.getEmploye() != null ? entity.getEmploye().getCodeEmploye() : null);
        dto.setEmployeNom(entity.getEmploye() != null ? entity.getEmploye().getNom() : null);
        dto.setEmployePrenom(entity.getEmploye() != null ? entity.getEmploye().getPrenom() : null);
        dto.setEmploiEmployeId(entity.getEmploiEmploye() != null ? entity.getEmploiEmploye().getId() : null);
        dto.setDateDebut(entity.getDateDebut());
        dto.setDateFin(entity.getDateFin());
        dto.setHeureDebut(entity.getHeureDebut());
        dto.setHeureFin(entity.getHeureFin());
        dto.setPriorite(entity.getPriorite() != null ? entity.getPriorite().name() : null);
        dto.setFrequence(entity.getFrequence() != null ? entity.getFrequence().name() : null);
        dto.setUniteFreq(entity.getUniteFreq());
        dto.setActif(entity.getActif());
        dto.setDuplique(entity.getDuplique());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private LocalDate addByFrequence(LocalDate base, HoraireSpecial.Frequence frequence, Integer uniteFreq) {
        int unit = uniteFreq != null ? uniteFreq : 1;
        if (frequence == null) {
            return base;
        }
        return switch (frequence) {
            case AUCUN -> base;
            case JOUR -> base.plusDays(unit);
            case SEMAINE -> base.plusWeeks(unit);
            case QUINZAINE -> base.plusWeeks(2L * unit);
            case MOIS -> base.plusMonths(unit);
        };
    }

    private Integer normalizeUniteFreq(HoraireSpecial.Frequence frequence, Integer uniteFreq) {
        if (frequence == HoraireSpecial.Frequence.AUCUN) {
            return 0;
        }
        int value = uniteFreq != null ? uniteFreq : 1;
        if (value < 1) {
            throw new RuntimeException("Unite frequence must be >= 1 for frequence " + frequence);
        }
        return value;
    }

    private EmploiEmploye resolveEmploiEmploye(Long employeId, Long emploiEmployeId) {
        if (employeId == null) {
            return null;
        }
        if (emploiEmployeId != null) {
            EmploiEmploye emploi = emploiEmployeRepository.findById(emploiEmployeId)
                    .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiEmployeId));
            if (!emploi.getEmploye().getId().equals(employeId)) {
                throw new RuntimeException("EmploiEmploye does not belong to employe " + employeId);
            }
            if (emploi.getStatutEmploi() == EmploiEmploye.StatutEmploi.TERMINE) {
                throw new RuntimeException("EmploiEmploye is terminated and cannot be used.");
            }
            return emploi;
        }

        List<EmploiEmploye> emplois = emploiEmployeRepository.findByEmployeIdAndStatutEmploiNot(
                employeId,
                EmploiEmploye.StatutEmploi.TERMINE);
        if (emplois.isEmpty()) {
            throw new RuntimeException("No non-terminated emploi found for employe " + employeId);
        }
        if (emplois.size() > 1) {
            throw new RuntimeException("Multiple non-terminated emplois found for employe " + employeId + ". Please specify emploiEmployeId.");
        }
        return emplois.get(0);
    }
}
