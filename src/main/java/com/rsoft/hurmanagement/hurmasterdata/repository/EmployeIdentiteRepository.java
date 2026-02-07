package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeIdentite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeIdentiteRepository extends JpaRepository<EmployeIdentite, Long> {
    List<EmployeIdentite> findByEmployeId(Long employeId);
    List<EmployeIdentite> findByEmployeIdAndActif(Long employeId, String actif);
}
