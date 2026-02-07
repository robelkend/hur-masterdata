package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterfaceExtractionRepository extends JpaRepository<InterfaceExtraction, Long> {
    boolean existsByCodeExtraction(String codeExtraction);
    Optional<InterfaceExtraction> findByCodeExtraction(String codeExtraction);
}
