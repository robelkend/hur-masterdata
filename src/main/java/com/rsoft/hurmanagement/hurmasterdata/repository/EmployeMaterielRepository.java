package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeMateriel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface EmployeMaterielRepository extends JpaRepository<EmployeMateriel, Long> {

    @Query("""
            SELECT em FROM EmployeMateriel em
             WHERE (:employeId IS NULL OR em.employe.id = :employeId)
               AND (:materielId IS NULL OR em.materiel.id = :materielId)
               AND (:statut IS NULL OR em.statut = :statut)
               AND (:dateDebut IS NULL OR em.dateAttribution >= :dateDebut)
               AND (:dateFin IS NULL OR em.dateAttribution <= :dateFin)
            """)
    Page<EmployeMateriel> findAllWithFilters(@Param("employeId") Long employeId,
                                             @Param("materielId") Long materielId,
                                             @Param("statut") EmployeMateriel.StatutMateriel statut,
                                             @Param("dateDebut") LocalDate dateDebut,
                                             @Param("dateFin") LocalDate dateFin,
                                             Pageable pageable);
}
