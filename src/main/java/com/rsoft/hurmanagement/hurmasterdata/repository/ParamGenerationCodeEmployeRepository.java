package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.ParamGenerationCodeEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ParamGenerationCodeEmployeRepository extends JpaRepository<ParamGenerationCodeEmploye, Long> {
    Page<ParamGenerationCodeEmploye> findByActifOrderByDateEffectifDesc(String actif, Pageable pageable);
    
    List<ParamGenerationCodeEmploye> findByActifAndDateEffectifLessThanEqualAndDateFinGreaterThanEqualOrDateFinIsNull(
            String actif, LocalDate dateEffectif, LocalDate dateEffectif2);
    
    List<ParamGenerationCodeEmploye> findByEntrepriseIdAndActifAndDateEffectifLessThanEqualAndDateFinGreaterThanEqualOrDateFinIsNull(
            Long entrepriseId, String actif, LocalDate dateEffectif, LocalDate dateEffectif2);
    
    List<ParamGenerationCodeEmploye> findByTypeEmployeIdAndActifAndDateEffectifLessThanEqualAndDateFinGreaterThanEqualOrDateFinIsNull(
            Long typeEmployeId, String actif, LocalDate dateEffectif, LocalDate dateEffectif2);
}
