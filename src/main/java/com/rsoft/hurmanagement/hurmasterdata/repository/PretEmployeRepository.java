package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PretEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PretEmployeRepository extends JpaRepository<PretEmploye, Long> {
    
    Page<PretEmploye> findAll(Pageable pageable);
    
    @Query("SELECT p FROM PretEmploye p WHERE " +
           "(:employeId IS NULL OR p.employe.id = :employeId) AND " +
           "(:statut IS NULL OR p.statut = :statut) AND " +
           "(:avance IS NULL OR p.avance = :avance) AND " +
           "p.datePret >= :dateDebut AND p.datePret <= :dateFin")
    Page<PretEmploye> findAllWithFilters(
        @Param("employeId") Long employeId,
        @Param("statut") PretEmploye.StatutPret statut,
        @Param("avance") String avance,
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin,
        Pageable pageable
    );
    
    List<PretEmploye> findByEmployeId(Long employeId);
    
    List<PretEmploye> findByEmployeIdAndStatut(Long employeId, PretEmploye.StatutPret statut);

    @Query("SELECT p FROM PretEmploye p WHERE " +
           "p.employe.id = :employeId AND " +
           "p.preleverDansPayroll = 'Y' AND " +
           "p.statut = com.rsoft.hurmanagement.hurmasterdata.entity.PretEmploye.StatutPret.EN_COURS AND " +
           "(p.regimePaie IS NULL OR p.regimePaie.id = :regimePaieId)")
    List<PretEmploye> findPrelevablesForPayroll(
            @Param("employeId") Long employeId,
            @Param("regimePaieId") Long regimePaieId);
    
    @Query("SELECT SUM(r.montantRembourse) FROM PretRemboursement r WHERE r.pretEmploye.id = :pretEmployeId")
    BigDecimal calculateMontantVerse(@Param("pretEmployeId") Long pretEmployeId);
}
