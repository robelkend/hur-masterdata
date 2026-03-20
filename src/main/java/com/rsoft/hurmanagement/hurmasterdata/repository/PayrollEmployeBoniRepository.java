package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeBoni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface PayrollEmployeBoniRepository extends JpaRepository<PayrollEmployeBoni, Long> {
    Optional<PayrollEmployeBoni> findByRegimePaieIdAndPeriodeBoniIdAndEmployeIdAndRubriquePaieId(
            Long regimePaieId,
            Long periodeBoniId,
            Long employeId,
            Long rubriquePaieId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE PayrollEmployeBoni b
               SET b.statut = com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeBoni.StatutBoni.VALIDE,
                   b.updatedBy = :username,
                   b.updatedOn = :updatedOn
             WHERE b.statut = com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmployeBoni.StatutBoni.CALCULE
               AND b.periodeBoni.id = :periodeBoniId
               AND b.rubriquePaie.id = :rubriquePaieId
               AND (:regimePaieId IS NULL OR b.regimePaie.id = :regimePaieId)
               AND (:entrepriseId IS NULL OR b.employe.entreprise.id = :entrepriseId)
            """)
    int validateCalculated(
            @Param("periodeBoniId") Long periodeBoniId,
            @Param("rubriquePaieId") Long rubriquePaieId,
            @Param("regimePaieId") Long regimePaieId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("username") String username,
            @Param("updatedOn") OffsetDateTime updatedOn
    );

    @Query("""
            SELECT b FROM PayrollEmployeBoni b
             WHERE b.periodeBoni.id = :periodeBoniId
               AND b.rubriquePaie.id = :rubriquePaieId
               AND (:regimePaieId IS NULL OR b.regimePaie.id = :regimePaieId)
               AND (:entrepriseId IS NULL OR b.employe.entreprise.id = :entrepriseId)
               AND (:employeId IS NULL OR b.employe.id = :employeId)
             ORDER BY b.employe.codeEmploye ASC, b.id DESC
            """)
    List<PayrollEmployeBoni> findByFilters(
            @Param("periodeBoniId") Long periodeBoniId,
            @Param("rubriquePaieId") Long rubriquePaieId,
            @Param("regimePaieId") Long regimePaieId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("employeId") Long employeId
    );
}
