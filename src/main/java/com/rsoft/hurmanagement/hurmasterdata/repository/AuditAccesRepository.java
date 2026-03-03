package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.AuditAcces;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AuditAccesRepository extends JpaRepository<AuditAcces, Long> {
    Page<AuditAcces> findByOrderByDateEvenementDesc(Pageable pageable);
    
    @Query("SELECT a FROM AuditAcces a WHERE a.dateEvenement >= :dateDebut AND a.dateEvenement <= :dateFin ORDER BY a.dateEvenement DESC")
    Page<AuditAcces> findByDateRange(@Param("dateDebut") OffsetDateTime dateDebut, 
                                      @Param("dateFin") OffsetDateTime dateFin, 
                                      Pageable pageable);
    
    Page<AuditAcces> findByUtilisateurOrderByDateEvenementDesc(String utilisateur, Pageable pageable);
    Page<AuditAcces> findByTypeEvenementOrderByDateEvenementDesc(String typeEvenement, Pageable pageable);
    Page<AuditAcces> findByResultatOrderByDateEvenementDesc(String resultat, Pageable pageable);
    Page<AuditAcces> findByEntrepriseIdOrderByDateEvenementDesc(Long entrepriseId, Pageable pageable);

    @Query("SELECT a FROM AuditAcces a WHERE " +
            "(:dateDebut IS NULL OR a.dateEvenement >= :dateDebut) AND " +
            "(:dateFin IS NULL OR a.dateEvenement <= :dateFin) AND " +
            "(:utilisateur IS NULL OR LOWER(a.utilisateur) LIKE LOWER(CONCAT('%', :utilisateur, '%'))) AND " +
            "(:typeEvenement IS NULL OR a.typeEvenement = :typeEvenement) AND " +
            "(:resultat IS NULL OR a.resultat = :resultat) AND " +
            "(:entrepriseId IS NULL OR a.entreprise.id = :entrepriseId)")
    Page<AuditAcces> findWithFilters(@Param("dateDebut") OffsetDateTime dateDebut,
                                    @Param("dateFin") OffsetDateTime dateFin,
                                    @Param("utilisateur") String utilisateur,
                                    @Param("typeEvenement") AuditAcces.TypeEvenement typeEvenement,
                                    @Param("resultat") AuditAcces.Resultat resultat,
                                    @Param("entrepriseId") Long entrepriseId,
                                    Pageable pageable);
}
