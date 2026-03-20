package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.ProcessusParametre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessusParametreRepository extends JpaRepository<ProcessusParametre, Long> {
    Optional<ProcessusParametre> findByCodeProcessus(String codeProcessus);

    @Query("SELECT p FROM ProcessusParametre p WHERE " +
           "(p.actif = 'Y' OR p.actif = 'O') AND " +
           "p.statut <> com.rsoft.hurmanagement.hurmasterdata.entity.ProcessusParametre$Statut.EN_EXECUTION AND " +
           "(p.prochaineExecutionAt IS NULL OR p.prochaineExecutionAt <= :now)")
    List<ProcessusParametre> findDueJobs(@Param("now") OffsetDateTime now);
}
