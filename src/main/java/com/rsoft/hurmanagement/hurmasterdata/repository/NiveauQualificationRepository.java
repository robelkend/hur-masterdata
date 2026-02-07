package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.NiveauQualification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NiveauQualificationRepository extends JpaRepository<NiveauQualification, Long> {
    
    Optional<NiveauQualification> findByCodeNiveau(String codeNiveau);
    
    boolean existsByCodeNiveau(String codeNiveau);
    
    Page<NiveauQualification> findAll(Pageable pageable);
}
