package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollRecouvrement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollRecouvrementRepository extends JpaRepository<PayrollRecouvrement, Long> {
    List<PayrollRecouvrement> findByPayrollId(Long payrollId);
}
