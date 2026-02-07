package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.BaremeSanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaremeSanctionRepository extends JpaRepository<BaremeSanction, Long> {
    
    List<BaremeSanction> findByTypeEmployeId(Long typeEmployeId);
    
    void deleteByTypeEmployeId(Long typeEmployeId);
}
