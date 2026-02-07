package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByIdentifiant(String identifiant);
    
    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByIdentifiantOrEmail(String identifiant, String email);
    
    boolean existsByIdentifiant(String identifiant);
    
    boolean existsByEmail(String email);
    
    List<Utilisateur> findByActif(String actif);
    
    List<Utilisateur> findAllByOrderByIdentifiantAsc();
}
