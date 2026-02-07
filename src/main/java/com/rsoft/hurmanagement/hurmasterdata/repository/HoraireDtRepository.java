package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireDt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HoraireDtRepository extends JpaRepository<HoraireDt, Long> {
    List<HoraireDt> findByHoraireId(Long horaireId);
    HoraireDt findByHoraireIdAndJour(Long horaireId, Integer jour);
    boolean existsByHoraireIdAndJour(Long horaireId, Integer jour);
}
