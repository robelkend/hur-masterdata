package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.RefMateriel;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefMaterielRepository extends JpaRepository<RefMateriel, Long> {
    List<RefMateriel> findByActif(String actif, Sort sort);
}
