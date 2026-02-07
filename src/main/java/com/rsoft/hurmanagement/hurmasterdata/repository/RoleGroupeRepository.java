package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.RoleGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleGroupeRepository extends JpaRepository<RoleGroupe, Long> {
    
    Optional<RoleGroupe> findByCodeGroupe(String codeGroupe);
    
    boolean existsByCodeGroupe(String codeGroupe);
    
    List<RoleGroupe> findByActif(String actif);
    
    List<RoleGroupe> findAllByOrderByCodeGroupeAsc();
}
