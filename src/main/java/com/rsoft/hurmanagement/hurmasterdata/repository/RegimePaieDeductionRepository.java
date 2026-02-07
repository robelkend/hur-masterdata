package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaieDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegimePaieDeductionRepository extends JpaRepository<RegimePaieDeduction, Long> {
    
    List<RegimePaieDeduction> findByRegimePaieId(Long regimePaieId);
    
    Optional<RegimePaieDeduction> findByRegimePaieIdAndDeductionCodeId(Long regimePaieId, Long deductionCodeId);
    
    boolean existsByRegimePaieIdAndDeductionCodeId(Long regimePaieId, Long deductionCodeId);
    
    void deleteByRegimePaieId(Long regimePaieId);
}
