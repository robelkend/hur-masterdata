package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.AutreRevenuEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AutreRevenuEmployeRepository extends JpaRepository<AutreRevenuEmploye, Long> {

    @Query("SELECT a FROM AutreRevenuEmploye a WHERE " +
           "(:entrepriseId IS NULL OR a.entreprise.id = :entrepriseId) AND " +
           "(:employeId IS NULL OR a.employe.id = :employeId) AND " +
           "(:typeRevenuId IS NULL OR a.typeRevenu.id = :typeRevenuId) AND " +
           "(:statut IS NULL OR a.statut = :statut) AND " +
           "(a.dateRevenu >= COALESCE(:dateDebut, a.dateRevenu)) AND " +
           "(a.dateRevenu <= COALESCE(:dateFin, a.dateRevenu))")
    Page<AutreRevenuEmploye> findAllWithFilters(
            @Param("entrepriseId") Long entrepriseId,
            @Param("employeId") Long employeId,
            @Param("typeRevenuId") Long typeRevenuId,
            @Param("statut") AutreRevenuEmploye.StatutAutreRevenu statut,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            Pageable pageable);

    @Query("SELECT a FROM AutreRevenuEmploye a WHERE " +
           "a.employe.id = :employeId AND " +
           "a.statut = com.rsoft.hurmanagement.hurmasterdata.entity.AutreRevenuEmploye.StatutAutreRevenu.VALIDE AND " +
           "a.dateRevenu >= :dateDebut AND a.dateRevenu <= :dateFin AND " +
           "a.payrollNo = 0")
    List<AutreRevenuEmploye> findValidesForPayroll(
            @Param("employeId") Long employeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AutreRevenuEmploye a SET a.payrollNo = 0 WHERE a.payrollNo = :payrollId")
    int clearPayrollNo(@Param("payrollId") Integer payrollId);

    boolean existsByEmployeIdAndTypeRevenuIdAndDateRevenuBetween(Long employeId,
                                                                 Long typeRevenuId,
                                                                 LocalDate dateDebut,
                                                                 LocalDate dateFin);
}
