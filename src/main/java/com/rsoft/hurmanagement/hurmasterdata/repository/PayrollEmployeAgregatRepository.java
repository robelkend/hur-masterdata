package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeAgregat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PayrollEmployeAgregatRepository extends JpaRepository<PayrollEmployeAgregat, Long> {
    Optional<PayrollEmployeAgregat> findByRegimePaieIdAndEmployeIdAndPeriodeBoniId(
            Long regimePaieId,
            Long employeId,
            Long periodeBoniId
    );

    @Query("SELECT a FROM PayrollEmployeAgregat a " +
            "WHERE a.periodeBoni.id = :periodeBoniId " +
            "AND (:regimePaieId IS NULL OR a.regimePaie.id = :regimePaieId) " +
            "AND (:employeId IS NULL OR a.employe.id = :employeId)")
    Page<PayrollEmployeAgregat> findByPeriodeBoniAndFilters(
            @Param("periodeBoniId") Long periodeBoniId,
            @Param("regimePaieId") Long regimePaieId,
            @Param("employeId") Long employeId,
            Pageable pageable
    );

    @Query("SELECT a FROM PayrollEmployeAgregat a " +
            "WHERE a.periodeBoni.id = :periodeBoniId " +
            "AND (:regimePaieId IS NULL OR a.regimePaie.id = :regimePaieId) " +
            "AND (:entrepriseId IS NULL OR a.employe.entreprise.id = :entrepriseId)")
    List<PayrollEmployeAgregat> findForBoniGeneration(
            @Param("periodeBoniId") Long periodeBoniId,
            @Param("regimePaieId") Long regimePaieId,
            @Param("entrepriseId") Long entrepriseId
    );
}
