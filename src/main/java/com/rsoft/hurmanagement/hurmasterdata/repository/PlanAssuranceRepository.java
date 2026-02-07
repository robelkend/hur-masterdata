package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PlanAssurance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanAssuranceRepository extends JpaRepository<PlanAssurance, Long> {
    
    Optional<PlanAssurance> findByCodePlan(String codePlan);
    
    boolean existsByCodePlan(String codePlan);
    
    Page<PlanAssurance> findAll(Pageable pageable);
}
