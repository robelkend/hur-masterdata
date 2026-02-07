package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.ExclusionDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExclusionDeductionRepository extends JpaRepository<ExclusionDeduction, Long> {

    @Query("SELECT e FROM ExclusionDeduction e WHERE e.typeEmploye.id = :typeEmployeId")
    List<ExclusionDeduction> findByTypeEmployeId(@Param("typeEmployeId") Long typeEmployeId);

    boolean existsByTypeEmployeIdAndDefinitionDeductionId(Long typeEmployeId, Long definitionDeductionId);
}
