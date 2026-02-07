package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.AbsenceEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AbsenceEmployeRepository extends JpaRepository<AbsenceEmploye, Long> {

    @Query("SELECT a FROM AbsenceEmploye a WHERE " +
           "a.dateJour >= :dateDebut AND a.dateJour <= :dateFin AND " +
           "(:employeId IS NULL OR a.employe.id = :employeId) AND " +
           "(:statut IS NULL OR a.statut = :statut) AND " +
           "(:typeEvenement IS NULL OR a.typeEvenement = :typeEvenement) AND " +
           "(:entrepriseId IS NULL OR a.entreprise.id = :entrepriseId)")
    Page<AbsenceEmploye> findByFilters(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("employeId") Long employeId,
            @Param("statut") AbsenceEmploye.StatutAbsence statut,
            @Param("typeEvenement") AbsenceEmploye.TypeEvenement typeEvenement,
            @Param("entrepriseId") Long entrepriseId,
            Pageable pageable);

    @Query("SELECT COUNT(a) > 0 FROM AbsenceEmploye a WHERE " +
           "a.employe.id = :employeId AND a.dateJour = :dateJour AND a.typeEvenement = :typeEvenement")
    boolean existsForDate(
            @Param("employeId") Long employeId,
            @Param("dateJour") LocalDate dateJour,
            @Param("typeEvenement") AbsenceEmploye.TypeEvenement typeEvenement);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AbsenceEmploye a WHERE " +
           "a.source = com.rsoft.hurmanagement.hurmasterdata.entity.AbsenceEmploye.SourceAbsence.SYSTEME AND " +
           "a.statut IN (com.rsoft.hurmanagement.hurmasterdata.entity.AbsenceEmploye.StatutAbsence.BROUILLON, " +
           "           com.rsoft.hurmanagement.hurmasterdata.entity.AbsenceEmploye.StatutAbsence.ANNULE) AND " +
           "a.dateJour >= :dateDebut AND a.dateJour <= :dateFin AND " +
           "(:employeId IS NULL OR a.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR a.entreprise.id = :entrepriseId)")
    int deleteSystemDraftsInRange(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("employeId") Long employeId,
            @Param("entrepriseId") Long entrepriseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AbsenceEmploye a SET " +
           "a.statut = com.rsoft.hurmanagement.hurmasterdata.entity.AbsenceEmploye.StatutAbsence.VALIDE, " +
           "a.updatedBy = :username, " +
           "a.updatedOn = :updatedOn, " +
           "a.rowscn = a.rowscn + 1 " +
           "WHERE a.statut = com.rsoft.hurmanagement.hurmasterdata.entity.AbsenceEmploye.StatutAbsence.BROUILLON AND " +
           "a.dateJour >= :dateDebut AND a.dateJour <= :dateFin AND " +
           "(:employeId IS NULL OR a.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR a.entreprise.id = :entrepriseId)")
    int validateDraftsInRange(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("employeId") Long employeId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("username") String username,
            @Param("updatedOn") java.time.OffsetDateTime updatedOn);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AbsenceEmploye a SET " +
           "a.statut = com.rsoft.hurmanagement.hurmasterdata.entity.AbsenceEmploye.StatutAbsence.ANNULE, " +
           "a.updatedBy = :username, " +
           "a.updatedOn = :updatedOn, " +
           "a.rowscn = a.rowscn + 1 " +
           "WHERE a.statut = com.rsoft.hurmanagement.hurmasterdata.entity.AbsenceEmploye.StatutAbsence.VALIDE AND " +
           "a.dateJour >= :dateDebut AND a.dateJour <= :dateFin AND " +
           "(:employeId IS NULL OR a.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR a.entreprise.id = :entrepriseId)")
    int cancelValidatedInRange(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("employeId") Long employeId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("username") String username,
            @Param("updatedOn") java.time.OffsetDateTime updatedOn);

    @Query("SELECT a FROM AbsenceEmploye a WHERE " +
           "a.employe.id = :employeId AND " +
           "a.statut = com.rsoft.hurmanagement.hurmasterdata.entity.AbsenceEmploye.StatutAbsence.VALIDE AND " +
           "a.dateJour >= :dateDebut AND a.dateJour <= :dateFin AND " +
           "a.montantEquivalent > 0")
    java.util.List<AbsenceEmploye> findValidesForPayroll(
            @Param("employeId") Long employeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AbsenceEmploye a SET a.payrollId = NULL WHERE a.payrollId = :payrollId")
    int clearPayrollId(@Param("payrollId") Long payrollId);
}
