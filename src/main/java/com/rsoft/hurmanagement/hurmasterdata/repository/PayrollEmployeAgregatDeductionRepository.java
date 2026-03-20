package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeAgregatDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PayrollEmployeAgregatDeductionRepository extends JpaRepository<PayrollEmployeAgregatDeduction, Long> {
    Optional<PayrollEmployeAgregatDeduction> findByPayrollEmployeAgregatIdAndCodeDeductionAndCategorie(
            Long payrollEmployeAgregatId,
            String codeDeduction,
            PayrollDeduction.CategorieDeduction categorie
    );

    java.util.List<PayrollEmployeAgregatDeduction> findByPayrollEmployeAgregatIdOrderByCodeDeductionAsc(Long payrollEmployeAgregatId);

    @Query("SELECT COALESCE(SUM(d.montant), 0) FROM PayrollEmployeAgregatDeduction d WHERE d.payrollEmployeAgregat.id = :agregatId")
    BigDecimal sumMontantByAgregatId(@Param("agregatId") Long agregatId);

    @Query("SELECT COALESCE(SUM(d.montantCouvert), 0) FROM PayrollEmployeAgregatDeduction d WHERE d.payrollEmployeAgregat.id = :agregatId")
    BigDecimal sumMontantCouvertByAgregatId(@Param("agregatId") Long agregatId);
}
