package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.DefinitionDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefinitionDeductionRepository extends JpaRepository<DefinitionDeduction, Long> {
    boolean existsByCodeDeduction(String codeDeduction);
    Optional<DefinitionDeduction> findByCodeDeduction(String codeDeduction);
}
