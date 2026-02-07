package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.RessourceUi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RessourceUiRepository extends JpaRepository<RessourceUi, Long> {
    List<RessourceUi> findByActif(String actif);

    List<RessourceUi> findAllByOrderByCodeResourceAsc();
}
