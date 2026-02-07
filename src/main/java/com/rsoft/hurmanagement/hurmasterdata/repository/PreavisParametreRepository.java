package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PreavisParametre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreavisParametreRepository extends JpaRepository<PreavisParametre, Long> {
    Page<PreavisParametre> findAll(Pageable pageable);
    Page<PreavisParametre> findByEntrepriseId(Long entrepriseId, Pageable pageable);
    List<PreavisParametre> findByEntrepriseIdAndActifOrderByPrioriteDesc(Long entrepriseId, String actif);
    
    @Query("SELECT pp FROM PreavisParametre pp WHERE " +
           "pp.entreprise.id = :entrepriseId AND " +
           "pp.actif = 'Y' AND " +
           "(pp.typeEmploye.id = :typeEmployeId OR pp.typeEmploye IS NULL) AND " +
           "(pp.regimePaie.id = :regimePaieId OR pp.regimePaie IS NULL) AND " +
           "pp.typeDepart = :typeDepart AND " +
           "(:anciennete BETWEEN pp.ancienneteMin AND COALESCE(pp.ancienneteMax, :anciennete) OR " +
           "(pp.ancienneteMax IS NULL AND :anciennete >= pp.ancienneteMin)) " +
           "ORDER BY pp.priorite DESC")
    List<PreavisParametre> findMatchingParams(
            @Param("entrepriseId") Long entrepriseId,
            @Param("typeEmployeId") Long typeEmployeId,
            @Param("regimePaieId") Long regimePaieId,
            @Param("typeDepart") String typeDepart,
            @Param("anciennete") Integer anciennete
    );
}
