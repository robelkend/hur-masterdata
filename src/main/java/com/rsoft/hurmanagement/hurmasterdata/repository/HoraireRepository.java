package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.Horaire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HoraireRepository extends JpaRepository<Horaire, Long> {
    Optional<Horaire> findByCodeHoraire(String codeHoraire);
    Page<Horaire> findAll(Pageable pageable);
    boolean existsByCodeHoraire(String codeHoraire);
}
