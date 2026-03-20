package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollBoniDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollBoniDeductionRepository extends JpaRepository<PayrollBoniDeduction, Long> {
    void deleteByPayrollBoniId(Long payrollBoniId);
    List<PayrollBoniDeduction> findByPayrollBoniIdOrderByCodeDeductionAsc(Long payrollBoniId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PayrollBoniDeduction d WHERE d.payrollBoni.id IN :payrollBoniIds")
    int deleteByPayrollBoniIds(@Param("payrollBoniIds") List<Long> payrollBoniIds);
}
