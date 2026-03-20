package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollDeductionRepository extends JpaRepository<PayrollDeduction, Long> {
    List<PayrollDeduction> findByPayrollId(Long payrollId);
    List<PayrollDeduction> findByPayrollEmployeId(Long payrollEmployeId);

    @Query("SELECT pd FROM PayrollDeduction pd " +
           "JOIN FETCH pd.payrollEmploye pe " +
           "JOIN FETCH pe.payroll p " +
           "WHERE pd.payrollEmploye.id IN :payrollEmployeIds")
    List<PayrollDeduction> findByPayrollEmployeIds(@Param("payrollEmployeIds") List<Long> payrollEmployeIds);
}
