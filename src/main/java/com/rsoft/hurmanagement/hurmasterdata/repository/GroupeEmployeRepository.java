package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.GroupeEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GroupeEmployeRepository extends JpaRepository<GroupeEmploye, Long> {
    Optional<GroupeEmploye> findByCodeGroupe(String codeGroupe);
    Page<GroupeEmploye> findAll(Pageable pageable);
    boolean existsByCodeGroupe(String codeGroupe);
}
