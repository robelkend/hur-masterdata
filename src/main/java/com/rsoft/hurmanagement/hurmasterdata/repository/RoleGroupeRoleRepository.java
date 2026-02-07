package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.RoleGroupeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleGroupeRoleRepository extends JpaRepository<RoleGroupeRole, Long> {
    List<RoleGroupeRole> findByGroupeIdOrderByIdAsc(Long groupeId);

    void deleteByGroupeId(Long groupeId);
}
