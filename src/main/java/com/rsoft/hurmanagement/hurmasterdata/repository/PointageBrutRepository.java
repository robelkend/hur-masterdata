package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PointageBrut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PointageBrutRepository extends JpaRepository<PointageBrut, Long> {

    @Query("SELECT p FROM PointageBrut p WHERE " +
           "p.dateHeurePointage >= :dateDebut AND p.dateHeurePointage < :dateFin AND " +
           "(:employeId IS NULL OR p.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR p.entreprise.id = :entrepriseId)")
    Page<PointageBrut> findByFilters(
            @Param("dateDebut") OffsetDateTime dateDebut,
            @Param("dateFin") OffsetDateTime dateFin,
            @Param("employeId") Long employeId,
            @Param("entrepriseId") Long entrepriseId,
            Pageable pageable);

    @Query("SELECT p FROM PointageBrut p WHERE " +
           "p.dateHeurePointage >= :dateDebut AND p.dateHeurePointage < :dateFin AND " +
           "(:employeId IS NULL OR p.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR p.entreprise.id = :entrepriseId) AND " +
           "p.statutTraitement IN :statuts " +
           "ORDER BY p.dateHeurePointage ASC")
    List<PointageBrut> findForPresenceBuild(
            @Param("dateDebut") OffsetDateTime dateDebut,
            @Param("dateFin") OffsetDateTime dateFin,
            @Param("employeId") Long employeId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("statuts") List<PointageBrut.StatutTraitement> statuts);

    @Query("SELECT p FROM PointageBrut p WHERE " +
           "(:employeId IS NULL OR p.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR p.entreprise.id = :entrepriseId) AND " +
           "p.statutTraitement IN :statuts " +
           "ORDER BY p.dateHeurePointage ASC")
    List<PointageBrut> findForPresenceBuildAll(
            @Param("employeId") Long employeId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("statuts") List<PointageBrut.StatutTraitement> statuts);

    PointageBrut findTopByEmployeIdAndDateHeurePointageLessThanOrderByDateHeurePointageDesc(
            Long employeId, OffsetDateTime dateHeurePointage);

    boolean existsByEmployeIdAndDateHeurePointageBetween(Long employeId, OffsetDateTime start, OffsetDateTime end);

    List<PointageBrut> findByPresenceEmployeIdOrderByDateHeurePointageAsc(Long presenceEmployeId);
}
