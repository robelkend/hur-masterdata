package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollSanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollSanctionRepository extends JpaRepository<PayrollSanction, Long> {
    List<PayrollSanction> findByPayrollId(Long payrollId);
    List<PayrollSanction> findByPayrollEmployeId(Long payrollEmployeId);
}
