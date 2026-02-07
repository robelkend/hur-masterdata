package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeConge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TypeCongeRepository extends JpaRepository<TypeConge, Long> {
    Optional<TypeConge> findByCodeConge(String codeConge);
    Page<TypeConge> findAll(Pageable pageable);
    boolean existsByCodeConge(String codeConge);

    List<TypeConge> findByCongeAnnuel(TypeConge.CongeAnnuel congeAnnuel);

    @Query("SELECT DISTINCT tc FROM EmploiEmploye ee " +
           "JOIN ee.typeConge tc " +
           "WHERE ee.employe.id = :employeId AND ee.statutEmploi = :statut")
    List<TypeConge> findDistinctByEmployeIdAndStatutEmploi(
            @Param("employeId") Long employeId,
            @Param("statut") EmploiEmploye.StatutEmploi statut);
}
