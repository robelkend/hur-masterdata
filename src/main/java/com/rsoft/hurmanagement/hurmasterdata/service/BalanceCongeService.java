package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.BalanceCongeAnneeDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.BalanceCongeDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.BalanceConge;
import com.rsoft.hurmanagement.hurmasterdata.entity.BalanceCongeAnnee;
import com.rsoft.hurmanagement.hurmasterdata.repository.BalanceCongeAnneeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.BalanceCongeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceCongeService {

    private final BalanceCongeRepository balanceCongeRepository;
    private final BalanceCongeAnneeRepository balanceCongeAnneeRepository;

    @Transactional(readOnly = true)
    public Page<BalanceCongeDTO> findByFilters(Long entrepriseId,
                                               Long employeId,
                                               Long typeCongeId,
                                               Pageable pageable) {
        return balanceCongeRepository.findByFilters(entrepriseId, employeId, typeCongeId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public BalanceCongeDTO findById(Long id) {
        BalanceConge entity = balanceCongeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BalanceConge not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<BalanceCongeAnneeDTO> findAnneesByBalanceId(Long balanceId) {
        return balanceCongeAnneeRepository.findByBalanceCongeIdOrderByAnneeDesc(balanceId).stream()
                .map(this::toAnneeDTO)
                .toList();
    }

    private BalanceCongeDTO toDTO(BalanceConge entity) {
        BalanceCongeDTO dto = new BalanceCongeDTO();
        dto.setId(entity.getId());
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        if (entity.getEmploiEmploye() != null) {
            dto.setEmploiEmployeId(entity.getEmploiEmploye().getId());
        }
        if (entity.getEmploye() != null) {
            dto.setEmployeId(entity.getEmploye().getId());
            dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
            dto.setEmployeNom(entity.getEmploye().getNom());
            dto.setEmployePrenom(entity.getEmploye().getPrenom());
        }
        if (entity.getTypeConge() != null) {
            dto.setTypeCongeId(entity.getTypeConge().getId());
            dto.setTypeCongeCode(entity.getTypeConge().getCodeConge());
            dto.setTypeCongeDescription(entity.getTypeConge().getDescription());
        }
        dto.setSoldeActuel(entity.getSoldeActuel());
        dto.setSoldeDisponible(entity.getSoldeDisponible());
        dto.setDerniereMiseAJour(entity.getDerniereMiseAJour());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private BalanceCongeAnneeDTO toAnneeDTO(BalanceCongeAnnee entity) {
        BalanceCongeAnneeDTO dto = new BalanceCongeAnneeDTO();
        dto.setId(entity.getId());
        dto.setBalanceCongeId(entity.getBalanceConge() != null ? entity.getBalanceConge().getId() : null);
        dto.setEmploiEmployeId(entity.getEmploiEmploye() != null ? entity.getEmploiEmploye().getId() : null);
        if (entity.getTypeConge() != null) {
            dto.setTypeCongeId(entity.getTypeConge().getId());
            dto.setTypeCongeCode(entity.getTypeConge().getCodeConge());
            dto.setTypeCongeDescription(entity.getTypeConge().getDescription());
        }
        dto.setAnnee(entity.getAnnee());
        dto.setJoursAcquis(entity.getJoursAcquis());
        dto.setJoursPris(entity.getJoursPris());
        dto.setCumulAutorise(entity.getCumulAutorise());
        dto.setPlafondCumul(entity.getPlafondCumul());
        dto.setJoursReportes(entity.getJoursReportes());
        dto.setJoursExpires(entity.getJoursExpires());
        dto.setSoldeFinAnnee(entity.getSoldeFinAnnee());
        dto.setDateCloture(entity.getDateCloture());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
}
