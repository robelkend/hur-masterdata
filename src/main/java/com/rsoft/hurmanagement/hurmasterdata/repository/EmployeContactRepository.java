package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeContactRepository extends JpaRepository<EmployeContact, Long> {
    List<EmployeContact> findByEmployeId(Long employeId);
    List<EmployeContact> findByEmployeIdAndActif(Long employeId, String actif);
}
