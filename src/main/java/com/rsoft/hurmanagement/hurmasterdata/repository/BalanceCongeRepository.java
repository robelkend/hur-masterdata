package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.BalanceConge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface BalanceCongeRepository extends JpaRepository<BalanceConge, Long> {

    @Query("SELECT b FROM BalanceConge b WHERE " +
           "(:entrepriseId IS NULL OR b.entreprise.id = :entrepriseId) AND " +
           "(:employeId IS NULL OR b.employe.id = :employeId) AND " +
           "(:typeCongeId IS NULL OR b.typeConge.id = :typeCongeId)")
    Page<BalanceConge> findByFilters(
            @Param("entrepriseId") Long entrepriseId,
            @Param("employeId") Long employeId,
            @Param("typeCongeId") Long typeCongeId,
            Pageable pageable);

    java.util.Optional<BalanceConge> findFirstByEmploiEmployeIdAndEmployeIdAndTypeCongeId(
            Long emploiEmployeId,
            Long employeId,
            Long typeCongeId);

    @Query("SELECT COALESCE(SUM(b.soldeDisponible), 0) FROM BalanceConge b WHERE b.employe.id = :employeId AND b.actif = 'Y'")
    BigDecimal sumSoldeDisponibleByEmploye(@Param("employeId") Long employeId);
}
