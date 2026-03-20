package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.Payroll;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeAgregat;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeAgregatDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollPeriodeBoni;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeAgregatDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeAgregatRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollPeriodeBoniRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollEmployeAgregatDeductionService {
    private final PayrollDeductionRepository payrollDeductionRepository;
    private final PayrollPeriodeBoniRepository periodeBoniRepository;
    private final PayrollEmployeAgregatRepository agregatRepository;
    private final PayrollEmployeAgregatDeductionRepository agregatDeductionRepository;

    @Transactional
    public void aggregateForValidatedPayroll(Payroll payroll, List<PayrollEmploye> payrollEmployes, String username) {
        if (payroll == null || payroll.getId() == null || payroll.getDateFin() == null || payroll.getRegimePaie() == null) {
            return;
        }
        PayrollPeriodeBoni periodeBoni = periodeBoniRepository.findContainingDate(payroll.getDateFin()).orElse(null);
        if (periodeBoni == null) {
            return;
        }
        List<PayrollDeduction> deductions = payrollDeductionRepository.findByPayrollId(payroll.getId());
        for (PayrollDeduction deduction : deductions) {
            if (deduction == null || deduction.getPayrollEmploye() == null
                    || deduction.getPayrollEmploye().getEmploye() == null
                    || deduction.getPayrollEmploye().getEmploye().getId() == null) {
                continue;
            }
            Long employeId = deduction.getPayrollEmploye().getEmploye().getId();
            PayrollEmployeAgregat agregat = agregatRepository
                    .findByRegimePaieIdAndEmployeIdAndPeriodeBoniId(
                            payroll.getRegimePaie().getId(),
                            employeId,
                            periodeBoni.getId()
                    )
                    .orElse(null);
            if (agregat == null) {
                continue;
            }
            PayrollEmployeAgregatDeduction agregatDeduction = agregatDeductionRepository
                    .findByPayrollEmployeAgregatIdAndCodeDeductionAndCategorie(
                            agregat.getId(),
                            deduction.getCodeDeduction(),
                            deduction.getCategorie()
                    )
                    .orElseGet(() -> buildEmptyAgregatDeduction(agregat, deduction, username));

            agregatDeduction.setLibelle(deduction.getLibelle());
            agregatDeduction.setTaux(deduction.getTaux() != null ? deduction.getTaux() : BigDecimal.ZERO);
            agregatDeduction.setBaseMontant(safe(agregatDeduction.getBaseMontant()).add(safe(deduction.getBaseMontant())));
            agregatDeduction.setMontant(safe(agregatDeduction.getMontant()).add(safe(deduction.getMontant())));
            agregatDeduction.setMontantCouvert(safe(agregatDeduction.getMontantCouvert()).add(safe(deduction.getMontantCouvert())));
            agregatDeduction.setUpdatedBy(username);
            agregatDeduction.setUpdatedOn(OffsetDateTime.now());
            agregatDeductionRepository.save(agregatDeduction);
        }
    }

    private PayrollEmployeAgregatDeduction buildEmptyAgregatDeduction(PayrollEmployeAgregat agregat,
                                                                      PayrollDeduction deduction,
                                                                      String username) {
        PayrollEmployeAgregatDeduction entity = new PayrollEmployeAgregatDeduction();
        entity.setPayrollEmployeAgregat(agregat);
        entity.setCodeDeduction(deduction.getCodeDeduction());
        entity.setLibelle(deduction.getLibelle());
        entity.setCategorie(deduction.getCategorie());
        entity.setBaseMontant(BigDecimal.ZERO);
        entity.setTaux(deduction.getTaux() != null ? deduction.getTaux() : BigDecimal.ZERO);
        entity.setMontant(BigDecimal.ZERO);
        entity.setMontantCouvert(BigDecimal.ZERO);
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        return entity;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
