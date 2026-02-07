package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.RubriquePaieDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface RubriquePaieDeductionRepository extends JpaRepository<RubriquePaieDeduction, Long> {
    List<RubriquePaieDeduction> findByDefinitionDeductionId(Long definitionDeductionId);
    
    @Query("SELECT rpd FROM RubriquePaieDeduction rpd WHERE rpd.rubriquePaie.id = :rubriquePaieId")
    List<RubriquePaieDeduction> findByRubriquePaieId(@Param("rubriquePaieId") Long rubriquePaieId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RubriquePaieDeduction rpd WHERE rpd.rubriquePaie.id = :rubriquePaieId")
    void deleteByRubriquePaieId(@Param("rubriquePaieId") Long rubriquePaieId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RubriquePaieDeduction rpd WHERE rpd.definitionDeduction.id = :definitionDeductionId AND rpd.rubriquePaie.taxesSpeciaux <> :taxesSpeciaux")
    void deleteByDefinitionDeductionIdAndRubriquePaieTaxesSpeciauxNot(
            @Param("definitionDeductionId") Long definitionDeductionId,
            @Param("taxesSpeciaux") String taxesSpeciaux
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM RubriquePaieDeduction rpd WHERE rpd.rubriquePaie.id = :rubriquePaieId AND rpd.definitionDeduction.specialise <> :taxesSpeciaux")
    void deleteByRubriquePaieIdAndDefinitionDeductionSpecialiseNot(
            @Param("rubriquePaieId") Long rubriquePaieId,
            @Param("taxesSpeciaux") String taxesSpeciaux
    );
    
    boolean existsByDefinitionDeductionIdAndRubriquePaieId(Long definitionDeductionId, Long rubriquePaieId);
}
