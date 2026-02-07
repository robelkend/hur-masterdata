package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterfaceLoadingRepository extends JpaRepository<InterfaceLoading, Long> {
    boolean existsByCodeLoading(String codeLoading);
    Optional<InterfaceLoading> findByCodeLoading(String codeLoading);
}
