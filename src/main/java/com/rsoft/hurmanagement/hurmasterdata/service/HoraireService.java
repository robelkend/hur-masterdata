package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.Devise;
import com.rsoft.hurmanagement.hurmasterdata.entity.Horaire;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireDt;
import com.rsoft.hurmanagement.hurmasterdata.repository.DeviseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireDtRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoraireService {
    
    private final HoraireRepository repository;
    private final HoraireDtRepository horaireDtRepository;
    private final DeviseRepository deviseRepository;
    
    private static final Map<Integer, String> JOURS_LIBELLES = Map.of(
        1, "Lundi", 2, "Mardi", 3, "Mercredi", 4, "Jeudi",
        5, "Vendredi", 6, "Samedi", 7, "Dimanche"
    );
    
    @Transactional(readOnly = true)
    public Page<HoraireDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public HoraireDTO findById(Long id) {
        Horaire entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horaire not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public HoraireDTO create(HoraireCreateDTO dto, String username) {
        if (repository.existsByCodeHoraire(dto.getCodeHoraire())) {
            throw new RuntimeException("Horaire with code " + dto.getCodeHoraire() + " already exists");
        }
        
        Devise devise = deviseRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
        
        Horaire entity = new Horaire();
        entity.setCodeHoraire(dto.getCodeHoraire());
        entity.setDescription(dto.getDescription());
        entity.setGenererAbsence(dto.getGenererAbsence() != null ? dto.getGenererAbsence() : "Y");
        entity.setPayerSupplementaire(dto.getPayerSupplementaire() != null ? dto.getPayerSupplementaire() : "Y");
        entity.setMontantFixe(dto.getMontantFixe() != null ? dto.getMontantFixe() : "N");
        entity.setMontantHeureSup(dto.getMontantHeureSup() != null ? dto.getMontantHeureSup() : BigDecimal.ZERO);
        entity.setCoeffJourFerie(dto.getCoeffJourFerie() != null ? dto.getCoeffJourFerie() : BigDecimal.ZERO);
        entity.setNbHeuresRef(dto.getNbHeuresRef() != null ? dto.getNbHeuresRef() : BigDecimal.ZERO);
        entity.setCoeffDimanche(dto.getCoeffDimanche() != null ? dto.getCoeffDimanche() : BigDecimal.ZERO);
        entity.setCoeffSuppJourFerie(dto.getCoeffSuppJourFerie() != null ? dto.getCoeffSuppJourFerie() : BigDecimal.ZERO);
        entity.setCoeffSoir(dto.getCoeffSoir() != null ? dto.getCoeffSoir() : BigDecimal.ZERO);
        entity.setCoeffSuppSoir(dto.getCoeffSuppSoir() != null ? dto.getCoeffSuppSoir() : BigDecimal.ZERO);
        entity.setCoeffSuppOff(dto.getCoeffSuppOff() != null ? dto.getCoeffSuppOff() : BigDecimal.ZERO);
        entity.setDevise(devise);
        entity.setAlternerJourNuit(dto.getAlternerJourNuit() != null ? dto.getAlternerJourNuit() : "N");
        entity.setUniteAlternance(dto.getUniteAlternance());
        entity.setNbUniteJour(dto.getNbUniteJour() != null ? dto.getNbUniteJour() : 0);
        entity.setHeureDebutNuit(dto.getHeureDebutNuit());
        entity.setHeureFinNuit(dto.getHeureFinNuit());
        entity.setHeureFermetureAutoJour(dto.getHeureFermetureAutoJour());
        entity.setHeureFermetureAutoNuit(dto.getHeureFermetureAutoNuit());
        entity.setHeureDebut(dto.getHeureDebut());
        entity.setHeureFin(dto.getHeureFin());
        entity.setDetailPresent(dto.getDetailPresent() != null ? dto.getDetailPresent() : "Y");
        entity.setShiftEncours(dto.getShiftEncours());
        entity.setDefaultNbHovertime(dto.getDefaultNbHovertime());
        entity.setDebutSupplementaire(dto.getDebutSupplementaire());
        entity.setMinHeurePonctualite(dto.getMinHeurePonctualite());
        entity.setNbMinutePonctualite(dto.getNbMinutePonctualite());
        entity.setToleranceRetardMin(dto.getToleranceRetardMin() != null ? dto.getToleranceRetardMin() : 5);
        entity.setSeuilDoublonMin(dto.getSeuilDoublonMin() != null ? dto.getSeuilDoublonMin() : 2);
        entity.setMaxSessionHeures(dto.getMaxSessionHeures() != null ? dto.getMaxSessionHeures() : 16);
        entity.setExigerPlanNuit(dto.getExigerPlanNuit() != null ? dto.getExigerPlanNuit() : "Y");
        entity.setPlanifierNuitAuto(dto.getPlanifierNuitAuto() != null ? dto.getPlanifierNuitAuto() : "Y");
        entity.setHeureFinDemiJournee(dto.getHeureFinDemiJournee());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        Horaire saved = repository.save(entity);
        
        // Save horaire_dt if provided
        if (dto.getHoraireDts() != null && !dto.getHoraireDts().isEmpty()) {
            saveHoraireDts(saved.getId(), dto.getHoraireDts(), username);
        }
        
        return toDTO(saved);
    }
    
    @Transactional
    public HoraireDTO update(Long id, HoraireUpdateDTO dto, String username) {
        Horaire entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horaire not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        Devise devise = deviseRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new RuntimeException("Devise not found with id: " + dto.getDeviseId()));
        
        entity.setDescription(dto.getDescription());
        entity.setGenererAbsence(dto.getGenererAbsence() != null ? dto.getGenererAbsence() : "Y");
        entity.setPayerSupplementaire(dto.getPayerSupplementaire() != null ? dto.getPayerSupplementaire() : "Y");
        entity.setMontantFixe(dto.getMontantFixe() != null ? dto.getMontantFixe() : "N");
        entity.setMontantHeureSup(dto.getMontantHeureSup() != null ? dto.getMontantHeureSup() : BigDecimal.ZERO);
        entity.setCoeffJourFerie(dto.getCoeffJourFerie() != null ? dto.getCoeffJourFerie() : BigDecimal.ZERO);
        entity.setNbHeuresRef(dto.getNbHeuresRef() != null ? dto.getNbHeuresRef() : BigDecimal.ZERO);
        entity.setCoeffDimanche(dto.getCoeffDimanche() != null ? dto.getCoeffDimanche() : BigDecimal.ZERO);
        entity.setCoeffSuppJourFerie(dto.getCoeffSuppJourFerie() != null ? dto.getCoeffSuppJourFerie() : BigDecimal.ZERO);
        entity.setCoeffSoir(dto.getCoeffSoir() != null ? dto.getCoeffSoir() : BigDecimal.ZERO);
        entity.setCoeffSuppSoir(dto.getCoeffSuppSoir() != null ? dto.getCoeffSuppSoir() : BigDecimal.ZERO);
        entity.setCoeffSuppOff(dto.getCoeffSuppOff() != null ? dto.getCoeffSuppOff() : BigDecimal.ZERO);
        entity.setDevise(devise);
        entity.setAlternerJourNuit(dto.getAlternerJourNuit() != null ? dto.getAlternerJourNuit() : "N");
        entity.setUniteAlternance(dto.getUniteAlternance());
        entity.setNbUniteJour(dto.getNbUniteJour() != null ? dto.getNbUniteJour() : 0);
        entity.setHeureDebutNuit(dto.getHeureDebutNuit());
        entity.setHeureFinNuit(dto.getHeureFinNuit());
        entity.setHeureFermetureAutoJour(dto.getHeureFermetureAutoJour());
        entity.setHeureFermetureAutoNuit(dto.getHeureFermetureAutoNuit());
        entity.setHeureDebut(dto.getHeureDebut());
        entity.setHeureFin(dto.getHeureFin());
        entity.setDetailPresent(dto.getDetailPresent() != null ? dto.getDetailPresent() : "Y");
        entity.setShiftEncours(dto.getShiftEncours());
        entity.setDefaultNbHovertime(dto.getDefaultNbHovertime());
        entity.setDebutSupplementaire(dto.getDebutSupplementaire());
        entity.setMinHeurePonctualite(dto.getMinHeurePonctualite());
        entity.setNbMinutePonctualite(dto.getNbMinutePonctualite());
        entity.setToleranceRetardMin(dto.getToleranceRetardMin() != null ? dto.getToleranceRetardMin() : 5);
        entity.setSeuilDoublonMin(dto.getSeuilDoublonMin() != null ? dto.getSeuilDoublonMin() : 2);
        entity.setMaxSessionHeures(dto.getMaxSessionHeures() != null ? dto.getMaxSessionHeures() : 16);
        entity.setExigerPlanNuit(dto.getExigerPlanNuit() != null ? dto.getExigerPlanNuit() : "Y");
        entity.setPlanifierNuitAuto(dto.getPlanifierNuitAuto() != null ? dto.getPlanifierNuitAuto() : "Y");
        entity.setHeureFinDemiJournee(dto.getHeureFinDemiJournee());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        Horaire saved = repository.save(entity);
        
        // Update horaire_dt
        if (dto.getHoraireDts() != null) {
            updateHoraireDts(saved.getId(), dto.getHoraireDts(), username);
        }
        
        return toDTO(saved);
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        Horaire entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horaire not found with id: " + id));
        
        // Optimistic concurrency check
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        repository.delete(entity);
    }

    @Transactional
    public HoraireDTO cloneHoraire(Long id, String newCodeHoraire, String username) {
        if (repository.existsByCodeHoraire(newCodeHoraire)) {
            throw new RuntimeException("Horaire with code " + newCodeHoraire + " already exists");
        }
        Horaire source = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horaire not found with id: " + id));

        Horaire clone = new Horaire();
        clone.setCodeHoraire(newCodeHoraire);
        clone.setDescription(source.getDescription());
        clone.setGenererAbsence(source.getGenererAbsence());
        clone.setPayerSupplementaire(source.getPayerSupplementaire());
        clone.setMontantFixe(source.getMontantFixe());
        clone.setMontantHeureSup(source.getMontantHeureSup());
        clone.setCoeffJourFerie(source.getCoeffJourFerie());
        clone.setNbHeuresRef(source.getNbHeuresRef());
        clone.setCoeffDimanche(source.getCoeffDimanche());
        clone.setCoeffSuppJourFerie(source.getCoeffSuppJourFerie());
        clone.setCoeffSoir(source.getCoeffSoir());
        clone.setCoeffSuppSoir(source.getCoeffSuppSoir());
        clone.setCoeffSuppOff(source.getCoeffSuppOff());
        clone.setDevise(source.getDevise());
        clone.setAlternerJourNuit(source.getAlternerJourNuit());
        clone.setUniteAlternance(source.getUniteAlternance());
        clone.setNbUniteJour(source.getNbUniteJour());
        clone.setHeureDebutNuit(source.getHeureDebutNuit());
        clone.setHeureFinNuit(source.getHeureFinNuit());
        clone.setHeureFermetureAutoJour(source.getHeureFermetureAutoJour());
        clone.setHeureFermetureAutoNuit(source.getHeureFermetureAutoNuit());
        clone.setHeureDebut(source.getHeureDebut());
        clone.setHeureFin(source.getHeureFin());
        clone.setDetailPresent(source.getDetailPresent());
        clone.setShiftEncours(source.getShiftEncours());
        clone.setDefaultNbHovertime(source.getDefaultNbHovertime());
        clone.setDebutSupplementaire(source.getDebutSupplementaire());
        clone.setMinHeurePonctualite(source.getMinHeurePonctualite());
        clone.setNbMinutePonctualite(source.getNbMinutePonctualite());
        clone.setToleranceRetardMin(source.getToleranceRetardMin());
        clone.setSeuilDoublonMin(source.getSeuilDoublonMin());
        clone.setMaxSessionHeures(source.getMaxSessionHeures());
        clone.setExigerPlanNuit(source.getExigerPlanNuit());
        clone.setPlanifierNuitAuto(source.getPlanifierNuitAuto());
        clone.setHeureFinDemiJournee(source.getHeureFinDemiJournee());
        clone.setCreatedBy(username);
        clone.setCreatedOn(OffsetDateTime.now());
        clone.setRowscn(1);

        Horaire saved = repository.save(clone);

        List<HoraireDt> sourceDts = horaireDtRepository.findByHoraireId(source.getId());
        for (HoraireDt sourceDt : sourceDts) {
            HoraireDt dt = new HoraireDt();
            dt.setHoraire(saved);
            dt.setJour(sourceDt.getJour());
            dt.setHeureDebutJour(sourceDt.getHeureDebutJour());
            dt.setHeureFinJour(sourceDt.getHeureFinJour());
            dt.setHeureDebutNuit(sourceDt.getHeureDebutNuit());
            dt.setHeureFinNuit(sourceDt.getHeureFinNuit());
            dt.setHeureDebutPause(sourceDt.getHeureDebutPause());
            dt.setHeureFinPause(sourceDt.getHeureFinPause());
            dt.setExigerPresence(sourceDt.getExigerPresence());
            dt.setHeureFermetureAuto(sourceDt.getHeureFermetureAuto());
            dt.setCreatedBy(username);
            dt.setCreatedOn(OffsetDateTime.now());
            dt.setRowscn(1);
            horaireDtRepository.save(dt);
        }

        return toDTO(saved);
    }
    
    private void saveHoraireDts(Long horaireId, List<HoraireDtCreateDTO> dtDtos, String username) {
        for (HoraireDtCreateDTO dtDto : dtDtos) {
            if (horaireDtRepository.existsByHoraireIdAndJour(horaireId, dtDto.getJour())) {
                throw new RuntimeException("HoraireDt for jour " + dtDto.getJour() + " already exists for this horaire");
            }
            
            HoraireDt dt = new HoraireDt();
            Horaire horaireRef = repository.getReferenceById(horaireId);
            dt.setHoraire(horaireRef);
            dt.setJour(dtDto.getJour());
            dt.setHeureDebutJour(dtDto.getHeureDebutJour());
            dt.setHeureFinJour(dtDto.getHeureFinJour());
            dt.setHeureDebutNuit(dtDto.getHeureDebutNuit());
            dt.setHeureFinNuit(dtDto.getHeureFinNuit());
            dt.setHeureDebutPause(dtDto.getHeureDebutPause());
            dt.setHeureFinPause(dtDto.getHeureFinPause());
            dt.setExigerPresence(dtDto.getExigerPresence() != null ? dtDto.getExigerPresence() : "N");
            dt.setHeureFermetureAuto(dtDto.getHeureFermetureAuto() != null ? dtDto.getHeureFermetureAuto() : "N");
            dt.setCreatedBy(username);
            dt.setCreatedOn(OffsetDateTime.now());
            dt.setRowscn(1);
            horaireDtRepository.save(dt);
        }
    }
    
    private void updateHoraireDts(Long horaireId, List<HoraireDtUpdateDTO> dtDtos, String username) {
        // Get existing horaire_dt
        List<HoraireDt> existing = horaireDtRepository.findByHoraireId(horaireId);
        Map<Long, HoraireDt> existingMap = existing.stream()
                .collect(Collectors.toMap(HoraireDt::getId, dt -> dt));
        
        // Process updates and new entries
        List<Long> toKeep = new ArrayList<>();
        for (HoraireDtUpdateDTO dtDto : dtDtos) {
            if (dtDto.getId() != null && dtDto.getId() > 0) {
                // Update existing
                HoraireDt dt = existingMap.get(dtDto.getId());
                if (dt == null) {
                    throw new RuntimeException("HoraireDt not found with id: " + dtDto.getId());
                }
                if (!dt.getRowscn().equals(dtDto.getRowscn())) {
                    throw new RuntimeException("HoraireDt record has been modified by another user");
                }
                dt.setJour(dtDto.getJour());
                dt.setHeureDebutJour(dtDto.getHeureDebutJour());
                dt.setHeureFinJour(dtDto.getHeureFinJour());
                dt.setHeureDebutNuit(dtDto.getHeureDebutNuit());
                dt.setHeureFinNuit(dtDto.getHeureFinNuit());
                dt.setHeureDebutPause(dtDto.getHeureDebutPause());
                dt.setHeureFinPause(dtDto.getHeureFinPause());
                dt.setExigerPresence(dtDto.getExigerPresence() != null ? dtDto.getExigerPresence() : "N");
                dt.setHeureFermetureAuto(dtDto.getHeureFermetureAuto() != null ? dtDto.getHeureFermetureAuto() : "N");
                dt.setUpdatedBy(username);
                dt.setUpdatedOn(OffsetDateTime.now());
                dt.setRowscn(dt.getRowscn() + 1);
                horaireDtRepository.save(dt);
                toKeep.add(dt.getId());
            } else {
                // New entry
                if (horaireDtRepository.existsByHoraireIdAndJour(horaireId, dtDto.getJour())) {
                    throw new RuntimeException("HoraireDt for jour " + dtDto.getJour() + " already exists for this horaire");
                }
                HoraireDt dt = new HoraireDt();
                Horaire horaireRef = repository.getReferenceById(horaireId);
                dt.setHoraire(horaireRef);
                dt.setJour(dtDto.getJour());
                dt.setHeureDebutJour(dtDto.getHeureDebutJour());
                dt.setHeureFinJour(dtDto.getHeureFinJour());
                dt.setHeureDebutNuit(dtDto.getHeureDebutNuit());
                dt.setHeureFinNuit(dtDto.getHeureFinNuit());
                dt.setHeureDebutPause(dtDto.getHeureDebutPause());
                dt.setHeureFinPause(dtDto.getHeureFinPause());
                dt.setExigerPresence(dtDto.getExigerPresence() != null ? dtDto.getExigerPresence() : "N");
                dt.setHeureFermetureAuto(dtDto.getHeureFermetureAuto() != null ? dtDto.getHeureFermetureAuto() : "N");
                dt.setCreatedBy(username);
                dt.setCreatedOn(OffsetDateTime.now());
                dt.setRowscn(1);
                HoraireDt saved = horaireDtRepository.save(dt);
                toKeep.add(saved.getId());
            }
        }
        
        // Delete removed entries
        for (HoraireDt dt : existing) {
            if (!toKeep.contains(dt.getId())) {
                horaireDtRepository.delete(dt);
            }
        }
    }
    
    private HoraireDTO toDTO(Horaire entity) {
        HoraireDTO dto = new HoraireDTO();
        dto.setId(entity.getId());
        dto.setCodeHoraire(entity.getCodeHoraire());
        dto.setDescription(entity.getDescription());
        dto.setGenererAbsence(entity.getGenererAbsence());
        dto.setPayerSupplementaire(entity.getPayerSupplementaire());
        dto.setMontantFixe(entity.getMontantFixe());
        dto.setMontantHeureSup(entity.getMontantHeureSup());
        dto.setCoeffJourFerie(entity.getCoeffJourFerie());
        dto.setNbHeuresRef(entity.getNbHeuresRef());
        dto.setCoeffDimanche(entity.getCoeffDimanche());
        dto.setCoeffSuppJourFerie(entity.getCoeffSuppJourFerie());
        dto.setCoeffSoir(entity.getCoeffSoir());
        dto.setCoeffSuppSoir(entity.getCoeffSuppSoir());
        dto.setCoeffSuppOff(entity.getCoeffSuppOff());
        dto.setDeviseId(entity.getDevise().getId());
        dto.setDeviseCode(entity.getDevise().getCodeDevise());
        dto.setDeviseDescription(entity.getDevise().getDescription());
        dto.setAlternerJourNuit(entity.getAlternerJourNuit());
        dto.setUniteAlternance(entity.getUniteAlternance());
        dto.setNbUniteJour(entity.getNbUniteJour());
        dto.setHeureDebutNuit(entity.getHeureDebutNuit());
        dto.setHeureFinNuit(entity.getHeureFinNuit());
        dto.setHeureFermetureAutoJour(entity.getHeureFermetureAutoJour());
        dto.setHeureFermetureAutoNuit(entity.getHeureFermetureAutoNuit());
        dto.setHeureDebut(entity.getHeureDebut());
        dto.setHeureFin(entity.getHeureFin());
        dto.setDetailPresent(entity.getDetailPresent());
        dto.setShiftEncours(entity.getShiftEncours());
        dto.setDefaultNbHovertime(entity.getDefaultNbHovertime());
        dto.setDebutSupplementaire(entity.getDebutSupplementaire());
        dto.setMinHeurePonctualite(entity.getMinHeurePonctualite());
        dto.setNbMinutePonctualite(entity.getNbMinutePonctualite());
        dto.setToleranceRetardMin(entity.getToleranceRetardMin());
        dto.setSeuilDoublonMin(entity.getSeuilDoublonMin());
        dto.setMaxSessionHeures(entity.getMaxSessionHeures());
        dto.setExigerPlanNuit(entity.getExigerPlanNuit());
        dto.setPlanifierNuitAuto(entity.getPlanifierNuitAuto());
        dto.setHeureFinDemiJournee(entity.getHeureFinDemiJournee());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        // Load horaire_dt
        List<HoraireDt> horaireDts = horaireDtRepository.findByHoraireId(entity.getId());
        dto.setHoraireDts(horaireDts.stream().map(this::toDtDTO).collect(Collectors.toList()));
        
        return dto;
    }
    
    private HoraireDtDTO toDtDTO(HoraireDt entity) {
        HoraireDtDTO dto = new HoraireDtDTO();
        dto.setId(entity.getId());
        dto.setHoraireId(entity.getHoraire().getId());
        dto.setJour(entity.getJour());
        dto.setJourLibelle(JOURS_LIBELLES.get(entity.getJour()));
        dto.setHeureDebutJour(entity.getHeureDebutJour());
        dto.setHeureFinJour(entity.getHeureFinJour());
        dto.setHeureDebutNuit(entity.getHeureDebutNuit());
        dto.setHeureFinNuit(entity.getHeureFinNuit());
        dto.setHeureDebutPause(entity.getHeureDebutPause());
        dto.setHeureFinPause(entity.getHeureFinPause());
        dto.setExigerPresence(entity.getExigerPresence());
        dto.setHeureFermetureAuto(entity.getHeureFermetureAuto());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
