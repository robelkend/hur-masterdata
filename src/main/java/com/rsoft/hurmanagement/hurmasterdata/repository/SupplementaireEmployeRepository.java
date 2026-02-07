package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.SupplementaireEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplementaireEmployeRepository extends JpaRepository<SupplementaireEmploye, Long> {
    
    Page<SupplementaireEmploye> findByEmployeId(Long employeId, Pageable pageable);
    
    Page<SupplementaireEmploye> findByEntrepriseId(Long entrepriseId, Pageable pageable);
    
    Page<SupplementaireEmploye> findByStatut(SupplementaireEmploye.StatutSupplementaire statut, Pageable pageable);
    
    @Query("SELECT s FROM SupplementaireEmploye s WHERE " +
           "s.dateJour >= :dateDebut AND s.dateJour <= :dateFin AND " +
           "(:employeId IS NULL OR s.employe.id = :employeId) AND " +
           "(:statut IS NULL OR s.statut = :statut) AND " +
           "(:entrepriseId IS NULL OR s.entreprise.id = :entrepriseId)")
    Page<SupplementaireEmploye> findByFilters(
            @Param("dateDebut") java.time.LocalDate dateDebut,
            @Param("dateFin") java.time.LocalDate dateFin,
            @Param("employeId") Long employeId,
            @Param("statut") SupplementaireEmploye.StatutSupplementaire statut,
            @Param("entrepriseId") Long entrepriseId,
            Pageable pageable);
    
    List<SupplementaireEmploye> findByEmployeId(Long employeId);

    @Query("SELECT s FROM SupplementaireEmploye s WHERE " +
           "s.employe.id = :employeId AND " +
           "s.statut = com.rsoft.hurmanagement.hurmasterdata.entity.SupplementaireEmploye.StatutSupplementaire.VALIDE AND " +
           "s.dateJour >= :dateDebut AND s.dateJour <= :dateFin AND " +
           "s.noPayroll = 0")
    List<SupplementaireEmploye> findValidesForPayroll(
            @Param("employeId") Long employeId,
            @Param("dateDebut") java.time.LocalDate dateDebut,
            @Param("dateFin") java.time.LocalDate dateFin);

    @Query("SELECT s FROM SupplementaireEmploye s WHERE " +
           "s.employe.id = :employeId AND " +
           "s.statut = com.rsoft.hurmanagement.hurmasterdata.entity.SupplementaireEmploye.StatutSupplementaire.VALIDE AND " +
           "s.dateJour >= :dateDebut AND s.dateJour <= :dateFin AND " +
           "s.noPayroll = :payrollId")
    List<SupplementaireEmploye> findValidesForPayrollWithPayrollId(
            @Param("employeId") Long employeId,
            @Param("dateDebut") java.time.LocalDate dateDebut,
            @Param("dateFin") java.time.LocalDate dateFin,
            @Param("payrollId") Integer payrollId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SupplementaireEmploye s SET s.noPayroll = 0 WHERE s.noPayroll = :payrollId")
    int clearPayrollNo(@Param("payrollId") Integer payrollId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SupplementaireEmploye s WHERE " +
           "s.automatique = 'Y' AND s.statut = com.rsoft.hurmanagement.hurmasterdata.entity.SupplementaireEmploye.StatutSupplementaire.BROUILLON AND " +
           "s.dateJour >= :dateDebut AND s.dateJour <= :dateFin AND " +
           "(:employeId IS NULL OR s.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR s.entreprise.id = :entrepriseId)")
    int deleteAutoDraftsInRange(
            @Param("dateDebut") java.time.LocalDate dateDebut,
            @Param("dateFin") java.time.LocalDate dateFin,
            @Param("employeId") Long employeId,
            @Param("entrepriseId") Long entrepriseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SupplementaireEmploye s SET " +
           "s.statut = com.rsoft.hurmanagement.hurmasterdata.entity.SupplementaireEmploye.StatutSupplementaire.VALIDE, " +
           "s.updatedBy = :username, " +
           "s.updatedOn = :updatedOn, " +
           "s.rowscn = s.rowscn + 1 " +
           "WHERE s.statut = com.rsoft.hurmanagement.hurmasterdata.entity.SupplementaireEmploye.StatutSupplementaire.BROUILLON AND " +
           "s.dateJour >= :dateDebut AND s.dateJour <= :dateFin AND " +
           "(:employeId IS NULL OR s.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR s.entreprise.id = :entrepriseId)")
    int validateDraftsInRange(
            @Param("dateDebut") java.time.LocalDate dateDebut,
            @Param("dateFin") java.time.LocalDate dateFin,
            @Param("employeId") Long employeId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("username") String username,
            @Param("updatedOn") java.time.OffsetDateTime updatedOn);
}
