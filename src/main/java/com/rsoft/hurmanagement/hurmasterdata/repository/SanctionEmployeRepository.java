package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.SanctionEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanctionEmployeRepository extends JpaRepository<SanctionEmploye, Long> {
    
    Page<SanctionEmploye> findByEmployeId(Long employeId, Pageable pageable);
    
    Page<SanctionEmploye> findByEntrepriseId(Long entrepriseId, Pageable pageable);
    
    Page<SanctionEmploye> findByStatut(SanctionEmploye.StatutSanction statut, Pageable pageable);
    
    @Query("SELECT s FROM SanctionEmploye s WHERE " +
           "(:employeId IS NULL OR s.employe.id = :employeId) AND " +
           "(:statut IS NULL OR s.statut = :statut) AND " +
           "(:entrepriseId IS NULL OR s.entreprise.id = :entrepriseId)")
    Page<SanctionEmploye> findByFilters(
            @Param("employeId") Long employeId,
            @Param("statut") SanctionEmploye.StatutSanction statut,
            @Param("entrepriseId") Long entrepriseId,
            Pageable pageable);
    
    List<SanctionEmploye> findByEmployeId(Long employeId);

    @Query("SELECT s FROM SanctionEmploye s WHERE " +
           "s.employe.id = :employeId AND " +
           "s.statut = com.rsoft.hurmanagement.hurmasterdata.entity.SanctionEmploye.StatutSanction.VALIDE AND " +
           "s.dateSanction >= :dateDebut AND s.dateSanction <= :dateFin")
    List<SanctionEmploye> findValidesForPayroll(
            @Param("employeId") Long employeId,
            @Param("dateDebut") java.time.LocalDate dateDebut,
            @Param("dateFin") java.time.LocalDate dateFin);
}
