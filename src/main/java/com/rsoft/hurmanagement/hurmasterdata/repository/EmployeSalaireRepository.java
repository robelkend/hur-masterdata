package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeSalaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeSalaireRepository extends JpaRepository<EmployeSalaire, Long> {
    List<EmployeSalaire> findByEmployeId(Long employeId);
    List<EmployeSalaire> findByEmployeIdAndActif(Long employeId, String actif);
    List<EmployeSalaire> findByEmploiId(Long emploiId);

    boolean existsByEmployeIdAndActif(Long employeId, String actif);
    
    Optional<EmployeSalaire> findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(Long employeId, String actif);
    Optional<EmployeSalaire> findFirstByEmployeIdAndActifAndRegimePaieIdOrderByPrincipalDescIdDesc(Long employeId, String actif, Long regimePaieId);

    @Query("SELECT s FROM EmployeSalaire s WHERE s.regimePaie.id = :regimePaieId AND s.principal = 'Y' AND s.actif = 'Y'")
    List<EmployeSalaire> findPrincipauxActifsByRegimePaie(@Param("regimePaieId") Long regimePaieId);

    @Query("SELECT s FROM EmployeSalaire s WHERE s.regimePaie.id = :regimePaieId AND s.actif = 'Y'")
    List<EmployeSalaire> findActifsByRegimePaie(@Param("regimePaieId") Long regimePaieId);

    @Query("SELECT s FROM EmployeSalaire s WHERE s.employe.id = :employeId AND s.actif = 'Y' " +
           "AND s.regimePaie.modeRemuneration IN :modes ORDER BY s.principal DESC, s.id DESC")
    List<EmployeSalaire> findActiveByEmployeIdAndMode(@Param("employeId") Long employeId,
                                                      @Param("modes") List<com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaie.ModeRemuneration> modes);

    @Query("SELECT s FROM EmployeSalaire s " +
           "JOIN s.employe e " +
           "JOIN s.emploi emp " +
           "JOIN s.regimePaie rp " +
           "WHERE s.actif = 'Y' " +
           "AND e.actif = 'Y' " +
           "AND emp.principal = 'Y' " +
           "AND emp.statutEmploi = com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye.StatutEmploi.ACTIF " +
           "AND rp.horaireActif = 'Y' " +
           "AND s.dateDebut <= :dateFin " +
           "AND (s.dateFin IS NULL OR s.dateFin >= :dateDebut) " +
           "AND emp.dateDebut <= :dateFin " +
           "AND (emp.dateFin IS NULL OR emp.dateFin >= :dateDebut) " +
           "AND (:entrepriseId IS NULL OR e.entreprise.id = :entrepriseId) " +
           "AND (:employeId IS NULL OR e.id = :employeId) " +
           "AND (:typeEmployeIds IS NULL OR emp.typeEmploye.id IN :typeEmployeIds) " +
           "AND (:uniteOrganisationnelleIds IS NULL OR emp.uniteOrganisationnelle.id IN :uniteOrganisationnelleIds) " +
           "AND (:gestionnaireId IS NULL OR emp.gestionnaire.id = :gestionnaireId) " +
           "AND (:regimePaieIds IS NULL OR rp.id IN :regimePaieIds) " +
           "ORDER BY emp.uniteOrganisationnelle.code ASC, e.codeEmploye ASC")
    List<EmployeSalaire> findBaseForPresenceUniteReport(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("entrepriseId") Long entrepriseId,
            @Param("employeId") Long employeId,
            @Param("typeEmployeIds") List<Long> typeEmployeIds,
            @Param("uniteOrganisationnelleIds") List<Long> uniteOrganisationnelleIds,
            @Param("gestionnaireId") Long gestionnaireId,
            @Param("regimePaieIds") List<Long> regimePaieIds
    );
}
