package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.TypeRevenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TypeRevenuRepository extends JpaRepository<TypeRevenu, Long> {
    Optional<TypeRevenu> findByCodeRevenu(String codeRevenu);

    @Query("SELECT t FROM TypeRevenu t WHERE " +
           "t.codeRevenu = :codeRevenu AND " +
           "(:entrepriseId IS NULL AND t.entreprise IS NULL OR t.entreprise.id = :entrepriseId)")
    Optional<TypeRevenu> findByCodeRevenuAndEntrepriseId(@Param("codeRevenu") String codeRevenu,
                                                         @Param("entrepriseId") Long entrepriseId);
    
    @Query("SELECT t FROM TypeRevenu t WHERE " +
           "(:entrepriseId IS NULL OR t.entreprise.id = :entrepriseId) AND " +
           "(:rubriquePaieId IS NULL OR t.rubriquePaie.id = :rubriquePaieId) AND " +
           "(:actif IS NULL OR t.actif = :actif)")
    Page<TypeRevenu> findAllWithFilters(
            @Param("entrepriseId") Long entrepriseId,
            @Param("rubriquePaieId") Long rubriquePaieId,
            @Param("actif") String actif,
            Pageable pageable);
    
    Page<TypeRevenu> findAll(Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TypeRevenu t WHERE " +
           "t.codeRevenu = :codeRevenu AND " +
           "(:entrepriseId IS NULL AND t.entreprise IS NULL OR t.entreprise.id = :entrepriseId)")
    boolean existsByCodeRevenuAndEntrepriseId(@Param("codeRevenu") String codeRevenu, @Param("entrepriseId") Long entrepriseId);
    
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TypeRevenu t WHERE " +
           "t.codeRevenu = :codeRevenu AND " +
           "(:entrepriseId IS NULL AND t.entreprise IS NULL OR t.entreprise.id = :entrepriseId) AND " +
           "t.id != :excludeId")
    boolean existsByCodeRevenuAndEntrepriseIdExcludingId(
            @Param("codeRevenu") String codeRevenu, 
            @Param("entrepriseId") Long entrepriseId,
            @Param("excludeId") Long excludeId);
}
