package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireSpecial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HoraireSpecialRepository extends JpaRepository<HoraireSpecial, Long> {
    Page<HoraireSpecial> findAll(Pageable pageable);
    Page<HoraireSpecial> findByEmployeId(Long employeId, Pageable pageable);
    List<HoraireSpecial> findByEmployeIdAndActif(Long employeId, String actif);

    @Query("SELECT hs FROM HoraireSpecial hs WHERE " +
           "(:employeId IS NULL OR hs.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR hs.employe.entreprise.id = :entrepriseId) AND " +
           "hs.dateDebut >= COALESCE(:dateDebutFrom, hs.dateDebut) AND " +
           "hs.dateDebut <= COALESCE(:dateDebutTo, hs.dateDebut)")
    Page<HoraireSpecial> findByCriteria(@Param("employeId") Long employeId,
                                        @Param("entrepriseId") Long entrepriseId,
                                        @Param("dateDebutFrom") LocalDate dateDebutFrom,
                                        @Param("dateDebutTo") LocalDate dateDebutTo,
                                        Pageable pageable);

    @Query("SELECT hs FROM HoraireSpecial hs WHERE " +
           "(:employeId IS NULL OR hs.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR hs.employe.entreprise.id = :entrepriseId) AND " +
           "hs.dateDebut >= COALESCE(:dateDebutFrom, hs.dateDebut) AND " +
           "hs.dateDebut <= COALESCE(:dateDebutTo, hs.dateDebut)")
    List<HoraireSpecial> findByCriteria(@Param("employeId") Long employeId,
                                        @Param("entrepriseId") Long entrepriseId,
                                        @Param("dateDebutFrom") LocalDate dateDebutFrom,
                                        @Param("dateDebutTo") LocalDate dateDebutTo);
    
    @Query("SELECT hs FROM HoraireSpecial hs WHERE " +
           "hs.employe.id = :employeId AND " +
           "hs.actif = 'Y' AND " +
           "(:date BETWEEN hs.dateDebut AND COALESCE(hs.dateFin, :date) OR " +
           "hs.dateDebut <= :date AND hs.dateFin IS NULL)")
    List<HoraireSpecial> findActiveByEmployeIdAndDate(@Param("employeId") Long employeId, @Param("date") LocalDate date);
}
