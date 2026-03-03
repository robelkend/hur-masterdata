package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PresenceEmployeRepository extends JpaRepository<PresenceEmploye, Long> {

    @Query("SELECT p FROM PresenceEmploye p WHERE " +
           "p.dateJour >= :dateDebut AND p.dateJour <= :dateFin AND " +
           "(:employeId IS NULL OR p.employe.id = :employeId) AND " +
           "(:statut IS NULL OR p.statutPresence = :statut) AND " +
           "(:entrepriseId IS NULL OR p.entreprise.id = :entrepriseId)")
    Page<PresenceEmploye> findByFilters(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("employeId") Long employeId,
            @Param("statut") PresenceEmploye.StatutPresence statut,
            @Param("entrepriseId") Long entrepriseId,
            Pageable pageable);

    @Query("SELECT p FROM PresenceEmploye p WHERE " +
           "p.dateJour >= :dateDebut AND p.dateJour <= :dateFin AND " +
           "(:employeId IS NULL OR p.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR p.entreprise.id = :entrepriseId)")
    java.util.List<PresenceEmploye> findForRearrange(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("employeId") Long employeId,
            @Param("entrepriseId") Long entrepriseId);

    @Query("SELECT p FROM PresenceEmploye p " +
           "LEFT JOIN EmploiEmploye ee ON ee.employe = p.employe " +
           "AND ee.principal = 'Y' " +
           "AND ee.dateDebut <= p.dateJour " +
           "AND (ee.dateFin IS NULL OR ee.dateFin >= p.dateJour) " +
           "WHERE p.dateJour >= :dateDebut AND p.dateJour <= :dateFin " +
           "AND (:entrepriseId IS NULL OR p.entreprise.id = :entrepriseId) " +
           "AND (:employeId IS NULL OR p.employe.id = :employeId) " +
           "AND (:actif IS NULL OR p.employe.actif = :actif) " +
           "AND (:typeEmployeId IS NULL OR ee.typeEmploye.id = :typeEmployeId OR p.typeEmploye.id = :typeEmployeId) " +
           "AND (:uniteOrganisationnelleId IS NULL OR ee.uniteOrganisationnelle.id = :uniteOrganisationnelleId) " +
           "AND (:gestionnaireId IS NULL OR ee.gestionnaire.id = :gestionnaireId) " +
           "AND (:nuit IS NULL OR " +
           "     (:nuit = 'Y' AND p.dateDepart IS NOT NULL AND p.dateDepart > p.dateJour) OR " +
           "     (:nuit = 'N' AND (p.dateDepart IS NULL OR p.dateDepart <= p.dateJour))" +
           ") " +
           "AND (:regimePaieIds IS NULL OR p.regimePaie.id IN :regimePaieIds) " +
           "ORDER BY p.employe.codeEmploye ASC, p.dateJour ASC, p.heureArrivee ASC")
    java.util.List<PresenceEmploye> findForPresenceReport(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("entrepriseId") Long entrepriseId,
            @Param("employeId") Long employeId,
            @Param("actif") String actif,
            @Param("typeEmployeId") Long typeEmployeId,
            @Param("uniteOrganisationnelleId") Long uniteOrganisationnelleId,
            @Param("gestionnaireId") Long gestionnaireId,
            @Param("nuit") String nuit,
            @Param("regimePaieIds") java.util.List<Long> regimePaieIds);

    PresenceEmploye findTopByEmployeIdAndDateJourAndStatutPresenceOrderByIdDesc(
            Long employeId,
            LocalDate dateJour,
            PresenceEmploye.StatutPresence statutPresence);

    PresenceEmploye findTopByEmployeIdAndStatutPresenceOrderByIdDesc(
            Long employeId,
            PresenceEmploye.StatutPresence statutPresence);

    @Query("SELECT p FROM PresenceEmploye p WHERE " +
           "p.employe.id = :employeId AND " +
           "p.statutPresence = com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye.StatutPresence.VALIDE AND " +
           "p.dateJour >= :dateDebut AND p.dateJour <= :dateFin")
    java.util.List<PresenceEmploye> findValidesForPayroll(
            @Param("employeId") Long employeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);

    @Query("SELECT p FROM PresenceEmploye p WHERE " +
           "p.statutPresence = com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye.StatutPresence.VALIDE AND " +
           "p.dateJour >= :dateDebut AND p.dateJour <= :dateFin AND " +
           "(:entrepriseId IS NULL OR p.entreprise.id = :entrepriseId)")
    java.util.List<PresenceEmploye> findValidesForPrimePresence(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("entrepriseId") Long entrepriseId);

    java.util.List<PresenceEmploye> findByStatutPresence(PresenceEmploye.StatutPresence statutPresence);

    boolean existsByEmployeIdAndDateJour(Long employeId, LocalDate dateJour);
}
