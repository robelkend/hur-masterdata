package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.FamilleMetier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilleMetierRepository extends JpaRepository<FamilleMetier, Long> {
    Page<FamilleMetier> findAll(Pageable pageable);
    boolean existsByCodeFamilleMetier(String codeFamilleMetier);
}
