package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePrestation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RubriquePrestationRepository extends JpaRepository<RubriquePrestation, Long> {
    Optional<RubriquePrestation> findByCodePrestation(String codePrestation);
    Page<RubriquePrestation> findAll(Pageable pageable);
    boolean existsByCodePrestation(String codePrestation);
}
