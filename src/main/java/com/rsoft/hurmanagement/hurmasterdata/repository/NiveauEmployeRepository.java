package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.NiveauEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NiveauEmployeRepository extends JpaRepository<NiveauEmploye, Long> {
    
    Optional<NiveauEmploye> findByCodeNiveau(String codeNiveau);
    
    boolean existsByCodeNiveau(String codeNiveau);
    
    Page<NiveauEmploye> findAll(Pageable pageable);
}
