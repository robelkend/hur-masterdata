package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollEmployeRepository extends JpaRepository<PayrollEmploye, Long> {
    List<PayrollEmploye> findByPayrollId(Long payrollId);

    Optional<PayrollEmploye> findFirstByEmployeIdAndPayrollRegimePaieIdAndPayrollStatutInAndPayrollDateFinLessThanEqualOrderByPayrollDateFinDesc(
            Long employeId,
            Long regimePaieId,
            List<Payroll.StatutPayroll> statuts,
            LocalDate dateFin
    );

    Optional<PayrollEmploye> findFirstByEmployeIdAndPayrollDateFinLessThanEqualOrderByPayrollDateFinDesc(
            Long employeId,
            LocalDate dateFin
    );

    @Query("SELECT pe FROM PayrollEmploye pe WHERE " +
           "pe.employe.id = :employeId AND " +
           "pe.payroll.regimePaie.id = :regimePaieId AND " +
           "pe.payroll.statut IN :statuts AND " +
           "pe.payroll.dateFin > :dateAfter AND " +
           "pe.payroll.dateFin <= :dateFin " +
           "ORDER BY pe.payroll.dateFin ASC")
    List<PayrollEmploye> findForTaxeCycle(
            @Param("employeId") Long employeId,
            @Param("regimePaieId") Long regimePaieId,
            @Param("statuts") List<Payroll.StatutPayroll> statuts,
            @Param("dateAfter") LocalDate dateAfter,
            @Param("dateFin") LocalDate dateFin);

    @Query("SELECT pe FROM PayrollEmploye pe WHERE " +
           "pe.employe.id = :employeId AND " +
           "pe.payroll.regimePaie.id = :regimePaieId AND " +
           "pe.payroll.statut IN :statuts AND " +
           "pe.payroll.dateFin <= :dateFin " +
           "ORDER BY pe.payroll.dateFin ASC")
    List<PayrollEmploye> findForTaxeCycleFromStart(
            @Param("employeId") Long employeId,
            @Param("regimePaieId") Long regimePaieId,
            @Param("statuts") List<Payroll.StatutPayroll> statuts,
            @Param("dateFin") LocalDate dateFin);

    @Query("SELECT COALESCE(SUM(pe.montantSalaireBase), 0) " +
           "FROM PayrollEmploye pe " +
           "WHERE pe.employe.id = :employeId " +
           "AND pe.payroll.statut IN :statuts " +
           "AND pe.payroll.dateFin >= :dateDebut " +
           "AND pe.payroll.dateFin <= :dateFin")
    BigDecimal sumMontantSalaireBaseByEmployeAndDateFinBetween(
            @Param("employeId") Long employeId,
            @Param("statuts") List<Payroll.StatutPayroll> statuts,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);

    @Query("SELECT COUNT(DISTINCT pe.payroll.id) " +
            "FROM PayrollEmploye pe " +
            "WHERE pe.employe.id = :employeId " +
            "AND pe.payroll.statut IN :statuts " +
            "AND pe.payroll.dateFin >= :dateDebut " +
            "AND pe.payroll.dateFin <= :dateFin")
    Long countPayrollsByEmployeAndDateFinBetween(
            @Param("employeId") Long employeId,
            @Param("statuts") List<Payroll.StatutPayroll> statuts,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);

    @Query("SELECT pe FROM PayrollEmploye pe " +
           "JOIN pe.payroll p " +
           "JOIN pe.employe e " +
           "WHERE p.dateFin BETWEEN :dateDebut AND :dateFin " +
           "AND (:entrepriseId IS NULL OR e.entreprise.id = :entrepriseId) " +
           "AND (:employeId IS NULL OR e.id = :employeId) " +
           "AND (:actif IS NULL OR e.actif = :actif) " +
           "AND (:statut IS NULL OR p.statut = :statut) " +
           "AND (:regimePaieIds IS NULL OR p.regimePaie.id IN :regimePaieIds) " +
           "AND ((" +
           "  :uniteOrganisationnelleIds IS NULL AND :typeEmployeIds IS NULL AND :gestionnaireId IS NULL AND :nuit IS NULL" +
           ") OR EXISTS (" +
           "  SELECT 1 FROM EmploiEmploye ee LEFT JOIN ee.horaire h " +
           "  WHERE ee.employe.id = e.id " +
           "    AND ee.statutEmploi IN (com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye.StatutEmploi.ACTIF, " +
           "                             com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye.StatutEmploi.SUSPENDU) " +
           "    AND ee.dateDebut <= p.dateFin " +
           "    AND (ee.dateFin IS NULL OR ee.dateFin >= p.dateFin) " +
           "    AND (:uniteOrganisationnelleIds IS NULL OR ee.uniteOrganisationnelle.id IN :uniteOrganisationnelleIds) " +
           "    AND (:typeEmployeIds IS NULL OR ee.typeEmploye.id IN :typeEmployeIds) " +
           "    AND (:gestionnaireId IS NULL OR ee.gestionnaire.id = :gestionnaireId) " +
           "    AND (:nuit IS NULL OR " +
           "         (:nuit = 'Y' AND h.shiftEncours = com.rsoft.hurmanagement.hurmasterdata.entity.Horaire.ShiftEncours.soir) OR " +
           "         (:nuit = 'N' AND (h.shiftEncours IS NULL OR h.shiftEncours <> com.rsoft.hurmanagement.hurmasterdata.entity.Horaire.ShiftEncours.soir)))" +
           ")) " +
           "ORDER BY p.dateFin DESC, e.codeEmploye ASC")
    List<PayrollEmploye> findForFeuillePayrollReport(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("entrepriseId") Long entrepriseId,
            @Param("nuit") String nuit,
            @Param("uniteOrganisationnelleIds") List<Long> uniteOrganisationnelleIds,
            @Param("typeEmployeIds") List<Long> typeEmployeIds,
            @Param("gestionnaireId") Long gestionnaireId,
            @Param("employeId") Long employeId,
            @Param("actif") String actif,
            @Param("regimePaieIds") List<Long> regimePaieIds,
            @Param("statut") Payroll.StatutPayroll statut
    );
}
