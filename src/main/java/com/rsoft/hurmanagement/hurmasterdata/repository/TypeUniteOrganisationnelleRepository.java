package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.TypeUniteOrganisationnelle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TypeUniteOrganisationnelleRepository extends JpaRepository<TypeUniteOrganisationnelle, Long> {
    Optional<TypeUniteOrganisationnelle> findByCode(String code);
    Page<TypeUniteOrganisationnelle> findAll(Pageable pageable);
    boolean existsByCode(String code);
}
