package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollEmployeStatsRepository extends JpaRepository<PayrollEmployeStats, Long> {
    List<PayrollEmployeStats> findByPayrollId(Long payrollId);
    List<PayrollEmployeStats> findByPayrollEmployeId(Long payrollEmployeId);
}
