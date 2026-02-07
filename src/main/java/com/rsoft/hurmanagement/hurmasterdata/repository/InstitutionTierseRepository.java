package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.InstitutionTierse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InstitutionTierseRepository extends JpaRepository<InstitutionTierse, Long> {
    Optional<InstitutionTierse> findByCodeInstitution(String codeInstitution);
    Page<InstitutionTierse> findAll(Pageable pageable);
    boolean existsByCodeInstitution(String codeInstitution);
}
