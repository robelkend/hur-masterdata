package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeDocumentRepository extends JpaRepository<EmployeDocument, Long> {
    List<EmployeDocument> findByEmployeId(Long employeId);
}
