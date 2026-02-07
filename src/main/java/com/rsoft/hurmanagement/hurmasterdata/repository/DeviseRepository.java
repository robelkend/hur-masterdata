package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.Devise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DeviseRepository extends JpaRepository<Devise, Long> {
    Optional<Devise> findByCodeDevise(String codeDevise);
    Page<Devise> findAll(Pageable pageable);
    boolean existsByCodeDevise(String codeDevise);
}
