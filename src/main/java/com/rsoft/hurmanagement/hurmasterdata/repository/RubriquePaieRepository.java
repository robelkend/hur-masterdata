package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePaie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RubriquePaieRepository extends JpaRepository<RubriquePaie, Long> {
    Optional<RubriquePaie> findByCodeRubrique(String codeRubrique);
    Optional<RubriquePaie> findByHardcoded(String hardcoded);
    Page<RubriquePaie> findAll(Pageable pageable);
    boolean existsByCodeRubrique(String codeRubrique);
    
    @Query("SELECT r FROM RubriquePaie r WHERE r.imposable = 'Y' ORDER BY r.codeRubrique")
    List<RubriquePaie> findAllImposable();
}
