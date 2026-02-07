package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.Poste;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PosteRepository extends JpaRepository<Poste, Long> {
    
    Optional<Poste> findByCodePoste(String codePoste);
    
    boolean existsByCodePoste(String codePoste);
    
    Page<Poste> findAll(Pageable pageable);
}
