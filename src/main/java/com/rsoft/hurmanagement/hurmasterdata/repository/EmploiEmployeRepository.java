package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmploiEmployeRepository extends JpaRepository<EmploiEmploye, Long> {
    List<EmploiEmploye> findByEmployeId(Long employeId);

    List<EmploiEmploye> findByEmployeIdAndStatutEmploiNot(Long employeId, EmploiEmploye.StatutEmploi statutEmploi);

    boolean existsByEmployeIdAndStatutEmploi(Long employeId, EmploiEmploye.StatutEmploi statutEmploi);
    
    @Query("SELECT e FROM EmploiEmploye e WHERE e.employe.id = :employeId AND e.principal = 'Y'")
    Optional<EmploiEmploye> findPrincipalByEmployeId(@Param("employeId") Long employeId);

    @Query("SELECT ee FROM EmploiEmploye ee JOIN ee.horaire h WHERE " +
           "ee.principal = 'Y' AND ee.horaire IS NOT NULL AND " +
           "h.payerSupplementaire = 'Y' AND " +
           "ee.employe.actif = 'Y' AND " +
           "ee.statutEmploi <> com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye.StatutEmploi.TERMINE AND " +
           "(:entrepriseId IS NULL OR ee.employe.entreprise.id = :entrepriseId) AND " +
           "(:employeId IS NULL OR ee.employe.id = :employeId)")
    List<EmploiEmploye> findEligibleForSupplementaire(
            @Param("entrepriseId") Long entrepriseId,
            @Param("employeId") Long employeId);

    @Query("SELECT ee FROM EmploiEmploye ee JOIN ee.horaire h WHERE " +
           "ee.principal = 'Y' AND ee.horaire IS NOT NULL AND " +
           "h.genererAbsence = 'Y' AND " +
           "ee.employe.actif = 'Y' AND " +
           "ee.statutEmploi <> com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye.StatutEmploi.TERMINE AND " +
           "(:entrepriseId IS NULL OR ee.employe.entreprise.id = :entrepriseId) AND " +
           "(:employeId IS NULL OR ee.employe.id = :employeId)")
    List<EmploiEmploye> findEligibleForAbsence(
            @Param("entrepriseId") Long entrepriseId,
            @Param("employeId") Long employeId);

    @Query("SELECT ee FROM EmploiEmploye ee WHERE ee.employe.id = :employeId " +
           "AND ee.statutEmploi IN (com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye.StatutEmploi.ACTIF, " +
           "com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye.StatutEmploi.SUSPENDU) " +
           "AND ee.dateDebut <= :date AND (ee.dateFin IS NULL OR ee.dateFin >= :date) " +
           "ORDER BY CASE WHEN ee.principal = 'Y' THEN 0 ELSE 1 END, ee.dateDebut DESC, ee.id DESC")
    List<EmploiEmploye> findActiveForDate(@Param("employeId") Long employeId, @Param("date") java.time.LocalDate date);

    List<EmploiEmploye> findByStatutEmploiAndDateFinStatutLessThanEqual(
            EmploiEmploye.StatutEmploi statutEmploi,
            LocalDate dateFinStatut
    );
}
