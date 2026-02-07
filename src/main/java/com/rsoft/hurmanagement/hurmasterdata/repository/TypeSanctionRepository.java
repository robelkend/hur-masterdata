package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.TypeSanction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TypeSanctionRepository extends JpaRepository<TypeSanction, Long> {
    Optional<TypeSanction> findByCodeSanction(String codeSanction);
    Page<TypeSanction> findAll(Pageable pageable);
    boolean existsByCodeSanction(String codeSanction);
}
