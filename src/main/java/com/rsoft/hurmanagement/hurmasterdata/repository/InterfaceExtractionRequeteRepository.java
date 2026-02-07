package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionRequete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterfaceExtractionRequeteRepository extends JpaRepository<InterfaceExtractionRequete, Long> {
    List<InterfaceExtractionRequete> findByInterfaceExtractionIdOrderByOrdreExecutionAsc(Long extractionId);
    List<InterfaceExtractionRequete> findByParentIdOrderByOrdreExecutionAsc(Long parentId);
    List<InterfaceExtractionRequete> findByInterfaceExtractionIdAndParentIsNullOrderByOrdreExecutionAsc(Long extractionId);
    void deleteByInterfaceExtractionId(Long extractionId);
}
