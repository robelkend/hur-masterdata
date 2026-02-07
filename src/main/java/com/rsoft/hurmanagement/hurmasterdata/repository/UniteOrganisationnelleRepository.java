package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.UniteOrganisationnelle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UniteOrganisationnelleRepository extends JpaRepository<UniteOrganisationnelle, Long> {
    
    Optional<UniteOrganisationnelle> findByCode(String code);
    
    boolean existsByCode(String code);
    
    Page<UniteOrganisationnelle> findAll(Pageable pageable);
    
    // Find all units excluding a specific one (for parent selection)
    @Query("SELECT u FROM UniteOrganisationnelle u WHERE u.id != :excludeId OR :excludeId IS NULL")
    List<UniteOrganisationnelle> findAllExcluding(@Param("excludeId") Long excludeId);
    
}
