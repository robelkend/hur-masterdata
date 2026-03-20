package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollPeriodeBoni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PayrollPeriodeBoniRepository extends JpaRepository<PayrollPeriodeBoni, Long> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    Optional<PayrollPeriodeBoni> findByStatut(PayrollPeriodeBoni.Statut statut);

    @Query("SELECT p FROM PayrollPeriodeBoni p WHERE :dateValue BETWEEN p.dateDebut AND p.dateFin")
    Optional<PayrollPeriodeBoni> findContainingDate(@Param("dateValue") LocalDate dateValue);

    Optional<PayrollPeriodeBoni> findTopByOrderByDateFinDesc();

    @Modifying
    @Query(value = "UPDATE periode_paie SET statut = 'INACTIF', updated_on = CURRENT_TIMESTAMP WHERE id <> :currentId AND statut = 'ACTIF'", nativeQuery = true)
    int deactivateOtherActive(@Param("currentId") Long currentId);
}
