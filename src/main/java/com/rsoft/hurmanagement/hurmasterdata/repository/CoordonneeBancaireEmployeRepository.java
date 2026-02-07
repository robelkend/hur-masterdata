package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.CoordonneeBancaireEmploye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CoordonneeBancaireEmployeRepository extends JpaRepository<CoordonneeBancaireEmploye, Long> {
    List<CoordonneeBancaireEmploye> findByEmployeId(Long employeId);
    List<CoordonneeBancaireEmploye> findByEmployeIdAndActif(Long employeId, String actif);
}
