package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayrollEmployeRepository extends JpaRepository<PayrollEmploye, Long> {
    List<PayrollEmploye> findByPayrollId(Long payrollId);

    @Query("SELECT pe FROM PayrollEmploye pe WHERE " +
           "pe.employe.id = :employeId AND " +
           "pe.payroll.regimePaie.id = :regimePaieId AND " +
           "pe.payroll.statut IN :statuts AND " +
           "pe.payroll.dateFin > :dateAfter AND " +
           "pe.payroll.dateFin <= :dateFin " +
           "ORDER BY pe.payroll.dateFin ASC")
    List<PayrollEmploye> findForTaxeCycle(
            @Param("employeId") Long employeId,
            @Param("regimePaieId") Long regimePaieId,
            @Param("statuts") List<Payroll.StatutPayroll> statuts,
            @Param("dateAfter") LocalDate dateAfter,
            @Param("dateFin") LocalDate dateFin);

    @Query("SELECT pe FROM PayrollEmploye pe WHERE " +
           "pe.employe.id = :employeId AND " +
           "pe.payroll.regimePaie.id = :regimePaieId AND " +
           "pe.payroll.statut IN :statuts AND " +
           "pe.payroll.dateFin <= :dateFin " +
           "ORDER BY pe.payroll.dateFin ASC")
    List<PayrollEmploye> findForTaxeCycleFromStart(
            @Param("employeId") Long employeId,
            @Param("regimePaieId") Long regimePaieId,
            @Param("statuts") List<Payroll.StatutPayroll> statuts,
            @Param("dateFin") LocalDate dateFin);
}
