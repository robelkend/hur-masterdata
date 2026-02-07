package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeNoteRepository extends JpaRepository<EmployeNote, Long> {
    List<EmployeNote> findByEmployeId(Long employeId);
}
