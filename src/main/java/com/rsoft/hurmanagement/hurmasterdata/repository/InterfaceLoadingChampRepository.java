package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoadingChamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterfaceLoadingChampRepository extends JpaRepository<InterfaceLoadingChamp, Long> {
    List<InterfaceLoadingChamp> findByLoadingId(Long loadingId);
    void deleteByLoadingId(Long loadingId);
}
