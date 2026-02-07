package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterfaceExtractionParamRepository extends JpaRepository<InterfaceExtractionParam, Long> {
    List<InterfaceExtractionParam> findByRequeteIdOrderByPositionAsc(Long requeteId);
    Optional<InterfaceExtractionParam> findByRequeteIdAndPosition(Long requeteId, Integer position);
    void deleteByRequeteId(Long requeteId);
}
