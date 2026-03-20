package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeMaterielEvenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeMaterielEvenementRepository extends JpaRepository<EmployeMaterielEvenement, Long> {
    List<EmployeMaterielEvenement> findByEmployeMaterielIdOrderByDateEvenementDescIdDesc(Long employeMaterielId);
    void deleteByEmployeMaterielId(Long employeMaterielId);
}
