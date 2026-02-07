package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionLiaison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterfaceExtractionLiaisonRepository extends JpaRepository<InterfaceExtractionLiaison, Long> {
    List<InterfaceExtractionLiaison> findByRequeteFilleIdOrderByParamPositionAsc(Long requeteFilleId);
    Optional<InterfaceExtractionLiaison> findByRequeteFilleIdAndParamPosition(Long requeteFilleId, Integer paramPosition);
    void deleteByRequeteFilleId(Long requeteFilleId);
}
