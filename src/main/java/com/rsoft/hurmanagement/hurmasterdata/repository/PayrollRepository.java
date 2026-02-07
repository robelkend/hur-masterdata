package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    @Query("SELECT p FROM Payroll p WHERE " +
            "(:regimePaieId IS NULL OR p.regimePaie.id = :regimePaieId) AND " +
            "(:statut IS NULL OR p.statut = :statut) AND " +
            "(p.dateFin >= COALESCE(:dateFinFrom, p.dateFin)) AND " +
            "(p.dateFin <= COALESCE(:dateFinTo, p.dateFin))")
    Page<Payroll> findByFilters(
            @Param("regimePaieId") Long regimePaieId,
            @Param("statut") Payroll.StatutPayroll statut,
            @Param("dateFinFrom") LocalDate dateFinFrom,
            @Param("dateFinTo") LocalDate dateFinTo,
            Pageable pageable);

    long countByRegimePaieIdAndStatutIn(Long regimePaieId, List<Payroll.StatutPayroll> statuts);

    @Query("SELECT COUNT(p) FROM Payroll p WHERE " +
            "p.regimePaie.id = :regimePaieId AND " +
            "p.statut IN :statuts AND " +
            "p.dateFin > :dateAfter AND " +
            "p.dateFin <= :dateFin")
    long countByRegimePaieIdAndStatutInAndDateFinRange(
            @Param("regimePaieId") Long regimePaieId,
            @Param("statuts") List<Payroll.StatutPayroll> statuts,
            @Param("dateAfter") LocalDate dateAfter,
            @Param("dateFin") LocalDate dateFin);

    @Query("SELECT COUNT(p) FROM Payroll p WHERE " +
            "p.regimePaie.id = :regimePaieId AND " +
            "p.statut IN :statuts AND " +
            "p.dateFin <= :dateFin")
    long countByRegimePaieIdAndStatutInAndDateFinBeforeOrEqual(
            @Param("regimePaieId") Long regimePaieId,
            @Param("statuts") List<Payroll.StatutPayroll> statuts,
            @Param("dateFin") LocalDate dateFin);
}
