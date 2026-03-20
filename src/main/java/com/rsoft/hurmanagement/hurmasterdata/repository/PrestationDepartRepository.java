package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PrestationDepart;
import com.rsoft.hurmanagement.hurmasterdata.entity.StatutPrestationDepart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrestationDepartRepository extends JpaRepository<PrestationDepart, Long> {

    @Query("SELECT p FROM PrestationDepart p WHERE " +
            "(:employeId IS NULL OR p.employe.id = :employeId) AND " +
            "(:statut IS NULL OR p.statut = :statut)")
    Page<PrestationDepart> findAllWithFilters(
            @Param("employeId") Long employeId,
            @Param("statut") StatutPrestationDepart statut,
            Pageable pageable
    );
}
