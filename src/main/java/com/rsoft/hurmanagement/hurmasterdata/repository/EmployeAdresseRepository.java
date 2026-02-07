package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeAdresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeAdresseRepository extends JpaRepository<EmployeAdresse, Long> {
    List<EmployeAdresse> findByEmployeId(Long employeId);
    List<EmployeAdresse> findByEmployeIdAndActif(Long employeId, String actif);
}
