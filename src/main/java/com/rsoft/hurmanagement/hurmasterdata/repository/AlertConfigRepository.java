package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.AlertConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlertConfigRepository extends JpaRepository<AlertConfig, Long> {
    Optional<AlertConfig> findByMessageDefinition_CodeMessage(String codeMessage);
    boolean existsByMessageDefinition_CodeMessage(String codeMessage);
    Page<AlertConfig> findAll(Pageable pageable);
}
