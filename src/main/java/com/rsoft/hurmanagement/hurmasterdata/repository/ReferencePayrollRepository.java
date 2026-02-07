package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.ReferencePayroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferencePayrollRepository extends JpaRepository<ReferencePayroll, Long> {
    
    Optional<ReferencePayroll> findByCodePayroll(String codePayroll);
    
    boolean existsByCodePayroll(String codePayroll);
    
    Page<ReferencePayroll> findAll(Pageable pageable);
}
