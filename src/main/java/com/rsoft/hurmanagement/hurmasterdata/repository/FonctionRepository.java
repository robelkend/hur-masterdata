package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.Fonction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FonctionRepository extends JpaRepository<Fonction, Long> {
    Optional<Fonction> findByCodeFonction(String codeFonction);
    Page<Fonction> findAll(Pageable pageable);
    boolean existsByCodeFonction(String codeFonction);
}
