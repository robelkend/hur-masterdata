package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    
    Optional<AppRole> findByCodeRole(String codeRole);
    
    boolean existsByCodeRole(String codeRole);
    
    List<AppRole> findByActif(String actif);
    
    List<AppRole> findAllByOrderByCodeRoleAsc();
}
