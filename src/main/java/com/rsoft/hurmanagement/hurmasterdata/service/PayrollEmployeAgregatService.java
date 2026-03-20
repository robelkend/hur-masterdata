package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollEmployeAgregatDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PayrollEmployeAgregatDeductionDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Payroll;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeAgregat;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeAgregatDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollPeriodeBoni;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeAgregatDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeAgregatRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollPeriodeBoniRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PayrollEmployeAgregatService {
    private final PayrollEmployeAgregatRepository agregatRepository;
    private final PayrollPeriodeBoniRepository periodeBoniRepository;
    private final PayrollEmployeAgregatDeductionRepository agregatDeductionRepository;

    @Transactional
    public void aggregateForValidatedPayroll(Payroll payroll, List<PayrollEmploye> payrollEmployes, String username) {
        if (payroll == null || payroll.getDateFin() == null || payroll.getRegimePaie() == null || payrollEmployes == null || payrollEmployes.isEmpty()) {
            return;
        }

        PayrollPeriodeBoni periodeBoni = resolveOrCreatePeriodeBoni(payroll.getDateFin(), username);
        if (periodeBoni == null) {
            return;
        }

        for (PayrollEmploye payrollEmploye : payrollEmployes) {
            if (payrollEmploye == null || payrollEmploye.getEmploye() == null || payrollEmploye.getEmploye().getId() == null) {
                continue;
            }

            Long regimePaieId = payroll.getRegimePaie().getId();
            Long employeId = payrollEmploye.getEmploye().getId();
            Long periodeBoniId = periodeBoni.getId();

            PayrollEmployeAgregat agregat = agregatRepository
                    .findByRegimePaieIdAndEmployeIdAndPeriodeBoniId(regimePaieId, employeId, periodeBoniId)
                    .orElseGet(() -> buildEmptyAgregat(payroll, payrollEmploye, periodeBoni, username));

            agregat.setMontantSalaireBase(safe(agregat.getMontantSalaireBase()).add(safe(payrollEmploye.getMontantSalaireBase())));
            agregat.setMontantSupplementaire(safe(agregat.getMontantSupplementaire()).add(safe(payrollEmploye.getMontantSupplementaire())));
            agregat.setMontantAutreRevenu(safe(agregat.getMontantAutreRevenu()).add(safe(payrollEmploye.getMontantAutreRevenu())));
            agregat.setMontantSanctions(safe(agregat.getMontantSanctions()).add(safe(payrollEmploye.getMontantSanctions())));
            agregat.setNoPeriode(resolveNoPeriode(payroll));
            agregat.setNbPaie(safeInt(agregat.getNbPaie()) + 1);
            agregat.setUpdatedBy(username);
            agregat.setUpdatedOn(OffsetDateTime.now());
            agregatRepository.save(agregat);
        }
    }

    @Transactional(readOnly = true)
    public Page<PayrollEmployeAgregatDTO> findByFilters(Long periodeBoniId, Long regimePaieId, Long employeId, Pageable pageable) {
        if (periodeBoniId == null) {
            throw new RuntimeException("periodeBoniId is required");
        }
        return agregatRepository.findByPeriodeBoniAndFilters(periodeBoniId, regimePaieId, employeId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<PayrollEmployeAgregatDeductionDTO> findDeductionsByAgregatId(Long agregatId) {
        if (agregatId == null) {
            throw new RuntimeException("agregatId is required");
        }
        return agregatDeductionRepository.findByPayrollEmployeAgregatIdOrderByCodeDeductionAsc(agregatId)
                .stream()
                .map(this::toDeductionDTO)
                .toList();
    }

    private PayrollPeriodeBoni resolveOrCreatePeriodeBoni(LocalDate payrollDateFin, String username) {
        Optional<PayrollPeriodeBoni> matching = periodeBoniRepository.findContainingDate(payrollDateFin);
        if (matching.isPresent()) {
            return matching.get();
        }

        Optional<PayrollPeriodeBoni> latestOpt = periodeBoniRepository.findTopByOrderByDateFinDesc();
        if (latestOpt.isEmpty()) {
            return null;
        }

        PayrollPeriodeBoni latest = latestOpt.get();
        int targetYear = payrollDateFin.getYear();
        if (targetYear <= latest.getDateFin().getYear()) {
            return null;
        }

        int spanYears = latest.getDateFin().getYear() - latest.getDateDebut().getYear();
        int newEndYear = targetYear;
        int newStartYear = targetYear - Math.max(spanYears, 0);

        LocalDate newDateDebut = safeDate(newStartYear, latest.getDateDebut().getMonthValue(), latest.getDateDebut().getDayOfMonth());
        LocalDate newDateFin = safeDate(newEndYear, latest.getDateFin().getMonthValue(), latest.getDateFin().getDayOfMonth());
        if (newDateFin.isBefore(newDateDebut)) {
            newDateFin = LocalDate.of(newEndYear, 12, 31);
            newDateDebut = LocalDate.of(newStartYear, 1, 1);
        }

        PayrollPeriodeBoni created = new PayrollPeriodeBoni();
        created.setCode(newDateDebut.getYear() + "-" + newDateFin.getYear());
        created.setLibelle(latest.getLibelle());
        created.setDateDebut(newDateDebut);
        created.setDateFin(newDateFin);
        created.setStatut(PayrollPeriodeBoni.Statut.INACTIF);
        created.setCreatedBy(username);
        created.setCreatedOn(OffsetDateTime.now());
        created.setUpdatedBy(username);
        created.setUpdatedOn(OffsetDateTime.now());
        created.setRowscn(1);
        return periodeBoniRepository.save(created);
    }

    private LocalDate safeDate(int year, int month, int day) {
        int maxDay = YearMonth.of(year, month).lengthOfMonth();
        return LocalDate.of(year, month, Math.min(day, maxDay));
    }

    private PayrollEmployeAgregat buildEmptyAgregat(Payroll payroll, PayrollEmploye payrollEmploye, PayrollPeriodeBoni periodeBoni, String username) {
        PayrollEmployeAgregat agregat = new PayrollEmployeAgregat();
        agregat.setRegimePaie(payroll.getRegimePaie());
        agregat.setEmploye(payrollEmploye.getEmploye());
        agregat.setPeriodeBoni(periodeBoni);
        agregat.setMontantSalaireBase(BigDecimal.ZERO);
        agregat.setMontantSupplementaire(BigDecimal.ZERO);
        agregat.setMontantAutreRevenu(BigDecimal.ZERO);
        agregat.setMontantSanctions(BigDecimal.ZERO);
        agregat.setNoPeriode(resolveNoPeriode(payroll));
        agregat.setNbPaie(0);
        agregat.setCreatedBy(username);
        agregat.setCreatedOn(OffsetDateTime.now());
        agregat.setUpdatedBy(username);
        agregat.setUpdatedOn(OffsetDateTime.now());
        agregat.setRowscn(1);
        return agregat;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private int resolveNoPeriode(Payroll payroll) {
        if (payroll == null) {
            return 1;
        }
        if (payroll.getRegimePaie() != null
                && payroll.getRegimePaie().getPeriodePaieCourante() != null
                && payroll.getRegimePaie().getPeriodePaieCourante() > 0) {
            return payroll.getRegimePaie().getPeriodePaieCourante();
        }
        if (payroll.getPeriodePaie() != null && payroll.getPeriodePaie() > 0) {
            return payroll.getPeriodePaie();
        }
        return 1;
    }

    private PayrollEmployeAgregatDTO toDTO(PayrollEmployeAgregat entity) {
        PayrollEmployeAgregatDTO dto = new PayrollEmployeAgregatDTO();
        dto.setId(entity.getId());
        dto.setRegimePaieId(entity.getRegimePaie().getId());
        dto.setRegimePaieCode(entity.getRegimePaie().getCodeRegimePaie());
        dto.setRegimePaieLibelle(entity.getRegimePaie().getDescription());
        dto.setEmployeId(entity.getEmploye().getId());
        dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
        dto.setEmployeNom(entity.getEmploye().getNom());
        dto.setEmployePrenom(entity.getEmploye().getPrenom());
        dto.setPeriodeBoniId(entity.getPeriodeBoni().getId());
        dto.setPeriodeBoniCode(entity.getPeriodeBoni().getCode());
        dto.setMontantSalaireBase(entity.getMontantSalaireBase());
        dto.setMontantSupplementaire(entity.getMontantSupplementaire());
        dto.setMontantAutreRevenu(entity.getMontantAutreRevenu());
        dto.setMontantSanctions(entity.getMontantSanctions());
        dto.setMontantDeductions(safe(agregatDeductionRepository.sumMontantByAgregatId(entity.getId())));
        dto.setMontantDeductionsCouvert(safe(agregatDeductionRepository.sumMontantCouvertByAgregatId(entity.getId())));
        dto.setNbPaie(entity.getNbPaie());
        dto.setNoPeriode(entity.getNoPeriode());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private PayrollEmployeAgregatDeductionDTO toDeductionDTO(PayrollEmployeAgregatDeduction entity) {
        PayrollEmployeAgregatDeductionDTO dto = new PayrollEmployeAgregatDeductionDTO();
        dto.setId(entity.getId());
        dto.setPayrollEmployeAgregatId(entity.getPayrollEmployeAgregat().getId());
        dto.setCodeDeduction(entity.getCodeDeduction());
        dto.setLibelle(entity.getLibelle());
        dto.setCategorie(entity.getCategorie() != null ? entity.getCategorie().name() : null);
        dto.setBaseMontant(entity.getBaseMontant());
        dto.setTaux(entity.getTaux());
        dto.setMontant(entity.getMontant());
        dto.setMontantCouvert(entity.getMontantCouvert());
        return dto;
    }
}
