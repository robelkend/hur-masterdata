package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.AssuranceEmploye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssuranceEmployeRepository extends JpaRepository<AssuranceEmploye, Long> {
    List<AssuranceEmploye> findByEmployeId(Long employeId);
    List<AssuranceEmploye> findByEmployeIdAndActif(Long employeId, String actif);
}
