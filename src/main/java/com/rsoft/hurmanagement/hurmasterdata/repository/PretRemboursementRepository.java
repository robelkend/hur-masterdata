package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PretRemboursement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PretRemboursementRepository extends JpaRepository<PretRemboursement, Long> {
    
    List<PretRemboursement> findByPretEmployeId(Long pretEmployeId);
    
    Page<PretRemboursement> findByPretEmployeId(Long pretEmployeId, Pageable pageable);
    
    @Query("SELECT r FROM PretRemboursement r WHERE r.pretEmploye.id = :pretEmployeId ORDER BY r.dateRemboursement DESC")
    List<PretRemboursement> findByPretEmployeIdOrderByDateDesc(@Param("pretEmployeId") Long pretEmployeId);
    
    @Query("SELECT SUM(r.montantRembourse) FROM PretRemboursement r WHERE r.pretEmploye.id = :pretEmployeId")
    java.math.BigDecimal sumMontantRembourseByPretEmployeId(@Param("pretEmployeId") Long pretEmployeId);
    
    @Query("SELECT MAX(r.dateRemboursement) FROM PretRemboursement r WHERE r.pretEmploye.id = :pretEmployeId")
    LocalDate findLastRemboursementDate(@Param("pretEmployeId") Long pretEmployeId);

    List<PretRemboursement> findByNoPayroll(Integer noPayroll);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PretRemboursement r SET r.noPayroll = 0, r.statut = com.rsoft.hurmanagement.hurmasterdata.entity.PretRemboursement.StatutRemboursement.BROUILLON WHERE r.noPayroll = :payrollId")
    int clearPayrollNo(@Param("payrollId") Integer payrollId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PretRemboursement r WHERE r.noPayroll = :payrollId")
    int deleteByNoPayroll(@Param("payrollId") Integer payrollId);
}
