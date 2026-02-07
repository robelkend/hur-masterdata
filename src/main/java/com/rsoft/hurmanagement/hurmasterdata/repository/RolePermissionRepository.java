package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleIdOrderByIdAsc(Long roleId);

    void deleteByRoleId(Long roleId);

    @Query("""
            select distinct rp from RolePermission rp
            join fetch rp.ressource res
            join fetch rp.action act
            join rp.role r
            join RoleGroupeRole rgr on rgr.role = r
            join rgr.groupe g
            join GroupeRoleUtilisateur gru on gru.groupe = g
            join gru.utilisateur u
            where (u.identifiant = :identifiant or u.email = :identifiant)
              and u.actif = 'Y'
              and g.actif = 'Y'
              and r.actif = 'Y'
              and rgr.actif = 'Y'
              and rp.actif = 'Y'
              and res.actif = 'Y'
            """)
    List<RolePermission> findActiveForUser(@Param("identifiant") String identifiant);
}
