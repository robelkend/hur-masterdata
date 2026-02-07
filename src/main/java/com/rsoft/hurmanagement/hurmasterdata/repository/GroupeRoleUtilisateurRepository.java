package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.GroupeRoleUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupeRoleUtilisateurRepository extends JpaRepository<GroupeRoleUtilisateur, Long> {
    List<GroupeRoleUtilisateur> findByUtilisateurIdOrderByIdAsc(Long utilisateurId);
    void deleteByUtilisateurId(Long utilisateurId);
}
