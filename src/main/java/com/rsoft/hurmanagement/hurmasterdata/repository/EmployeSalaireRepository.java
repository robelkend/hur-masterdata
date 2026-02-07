package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeSalaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeSalaireRepository extends JpaRepository<EmployeSalaire, Long> {
    List<EmployeSalaire> findByEmployeId(Long employeId);
    List<EmployeSalaire> findByEmployeIdAndActif(Long employeId, String actif);
    List<EmployeSalaire> findByEmploiId(Long emploiId);

    boolean existsByEmployeIdAndActif(Long employeId, String actif);
    
    Optional<EmployeSalaire> findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(Long employeId, String actif);

    @Query("SELECT s FROM EmployeSalaire s WHERE s.regimePaie.id = :regimePaieId AND s.principal = 'Y' AND s.actif = 'Y'")
    List<EmployeSalaire> findPrincipauxActifsByRegimePaie(@Param("regimePaieId") Long regimePaieId);

    @Query("SELECT s FROM EmployeSalaire s WHERE s.regimePaie.id = :regimePaieId AND s.actif = 'Y'")
    List<EmployeSalaire> findActifsByRegimePaie(@Param("regimePaieId") Long regimePaieId);

    @Query("SELECT s FROM EmployeSalaire s WHERE s.employe.id = :employeId AND s.actif = 'Y' " +
           "AND s.regimePaie.modeRemuneration IN :modes ORDER BY s.principal DESC, s.id DESC")
    List<EmployeSalaire> findActiveByEmployeIdAndMode(@Param("employeId") Long employeId,
                                                      @Param("modes") List<com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaie.ModeRemuneration> modes);
}
