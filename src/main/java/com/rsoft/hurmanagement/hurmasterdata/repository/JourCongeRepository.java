package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.JourConge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface JourCongeRepository extends JpaRepository<JourConge, Long> {
    
    Optional<JourConge> findById(Long id);

    boolean existsByDateCongeAndActif(LocalDate dateConge, JourConge.Actif actif);
    
    Page<JourConge> findAll(Pageable pageable);
}
