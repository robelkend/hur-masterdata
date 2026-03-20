package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.CongeEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CongeEmployeRepository extends JpaRepository<CongeEmploye, Long> {

    @Query(value = "SELECT c.* FROM conge_employe c WHERE " +
           "(:entrepriseId IS NULL OR c.entreprise_id = :entrepriseId) AND " +
           "(:employeId IS NULL OR c.employe_id = :employeId) AND " +
           "(:typeCongeId IS NULL OR c.type_conge_id = :typeCongeId) AND " +
           "(:statut IS NULL OR c.statut = CAST(:statut AS VARCHAR)) AND " +
           "c.date_debut_plan >= :dateDebut AND c.date_fin_plan <= :dateFin",
           nativeQuery = true,
           countQuery = "SELECT COUNT(*) FROM conge_employe c WHERE " +
           "(:entrepriseId IS NULL OR c.entreprise_id = :entrepriseId) AND " +
           "(:employeId IS NULL OR c.employe_id = :employeId) AND " +
           "(:typeCongeId IS NULL OR c.type_conge_id = :typeCongeId) AND " +
           "(:statut IS NULL OR c.statut = CAST(:statut AS VARCHAR)) AND " +
           "c.date_debut_plan >= :dateDebut AND c.date_fin_plan <= :dateFin")
    Page<CongeEmploye> findAllWithFilters(
            @Param("entrepriseId") Long entrepriseId,
            @Param("employeId") Long employeId,
            @Param("typeCongeId") Long typeCongeId,
            @Param("statut") String statut,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            Pageable pageable);

    @Query("SELECT COUNT(c) > 0 FROM CongeEmploye c WHERE " +
           "c.employe.id = :employeId AND " +
           "c.statut IN (com.rsoft.hurmanagement.hurmasterdata.entity.CongeEmploye.StatutConge.EN_COURS, " +
           "com.rsoft.hurmanagement.hurmasterdata.entity.CongeEmploye.StatutConge.TERMINE) AND " +
           "((" +
           "  c.dateDebutReel IS NOT NULL AND c.dateFinReel IS NOT NULL AND " +
           "  :dateJour BETWEEN c.dateDebutReel AND c.dateFinReel" +
           ") OR (" +
           "  (c.dateDebutReel IS NULL OR c.dateFinReel IS NULL) AND " +
           "  :dateJour BETWEEN c.dateDebutPlan AND c.dateFinPlan" +
           "))")
    boolean existsCongeForDate(
            @Param("employeId") Long employeId,
            @Param("dateJour") LocalDate dateJour);

    List<CongeEmploye> findByStatutAndDateFinPlanBefore(CongeEmploye.StatutConge statut, LocalDate dateFinPlan);

    List<CongeEmploye> findByStatut(CongeEmploye.StatutConge statut);

    List<CongeEmploye> findByStatutAndDateDebutPlanLessThanEqual(
            CongeEmploye.StatutConge statut,
            LocalDate dateDebutPlan);
}
