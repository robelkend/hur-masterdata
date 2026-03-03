package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {
    
    Optional<Entreprise> findByCodeEntreprise(String codeEntreprise);
    
    boolean existsByCodeEntreprise(String codeEntreprise);
    
    List<Entreprise> findByActif(String actif);
    
    Optional<Entreprise> findFirstByActif(String actif);

    Optional<Entreprise> findFirstByEntrepriseMereIsNullOrderByIdAsc();
    
    List<Entreprise> findAllByOrderByCodeEntrepriseAsc();
}
