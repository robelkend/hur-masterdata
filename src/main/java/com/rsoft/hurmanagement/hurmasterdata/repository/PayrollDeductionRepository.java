package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollDeductionRepository extends JpaRepository<PayrollDeduction, Long> {
    List<PayrollDeduction> findByPayrollId(Long payrollId);
    List<PayrollDeduction> findByPayrollEmployeId(Long payrollEmployeId);
}
