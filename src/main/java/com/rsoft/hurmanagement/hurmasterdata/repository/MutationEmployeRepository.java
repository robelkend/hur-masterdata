package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.MutationEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MutationEmployeRepository extends JpaRepository<MutationEmploye, Long> {
    
    Page<MutationEmploye> findAll(Pageable pageable);
    
    @Query("SELECT m FROM MutationEmploye m WHERE " +
           "(:employeId IS NULL OR m.employe.id = :employeId) AND " +
           "(:typeMutation IS NULL OR m.typeMutation = :typeMutation) AND " +
           "(:statut IS NULL OR m.statut = :statut) AND " +
           "m.dateEffet >= :dateDebut AND m.dateEffet <= :dateFin")
    Page<MutationEmploye> findAllWithFilters(
        @Param("employeId") Long employeId,
        @Param("typeMutation") MutationEmploye.TypeMutation typeMutation,
        @Param("statut") MutationEmploye.StatutMutation statut,
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin,
        Pageable pageable
    );
    
    List<MutationEmploye> findByEmployeId(Long employeId);
    
    List<MutationEmploye> findByEmployeIdAndStatut(Long employeId, MutationEmploye.StatutMutation statut);

    List<MutationEmploye> findByStatutAndDateEffetLessThanEqual(
            MutationEmploye.StatutMutation statut,
            LocalDate dateEffet
    );
}
