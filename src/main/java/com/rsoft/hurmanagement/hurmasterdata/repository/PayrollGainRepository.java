package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollGain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollGainRepository extends JpaRepository<PayrollGain, Long> {
    List<PayrollGain> findByPayrollId(Long payrollId);
    List<PayrollGain> findByPayrollEmployeId(Long payrollEmployeId);
}
