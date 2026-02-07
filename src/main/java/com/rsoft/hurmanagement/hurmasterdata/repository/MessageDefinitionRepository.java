package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.MessageDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageDefinitionRepository extends JpaRepository<MessageDefinition, Long> {
    Optional<MessageDefinition> findByCodeMessage(String codeMessage);
    boolean existsByCodeMessage(String codeMessage);
    Page<MessageDefinition> findByActifOrderByCodeMessageAsc(String actif, Pageable pageable);
    List<MessageDefinition> findByLangueAndActif(String langue, String actif);
    List<MessageDefinition> findByFrequenceAndActif(String frequence, String actif);
}
