package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.RefFormuleToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefFormuleTokenRepository extends JpaRepository<RefFormuleToken, String> {
    boolean existsByCodeElement(String codeElement);
    Optional<RefFormuleToken> findByCodeElement(String codeElement);
    List<RefFormuleToken> findByTypeElementOrderByLibelleAsc(RefFormuleToken.TypeElement typeElement);
    List<RefFormuleToken> findAllByOrderByTypeElementAscLibelleAsc();
}
