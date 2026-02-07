package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollTaxeCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollTaxeCycleRepository extends JpaRepository<PayrollTaxeCycle, Long> {
    Optional<PayrollTaxeCycle> findByEmployeIdAndRegimePaieId(Long employeId, Long regimePaieId);
}
