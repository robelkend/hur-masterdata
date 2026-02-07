package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.PermissionAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionActionRepository extends JpaRepository<PermissionAction, Long> {
    List<PermissionAction> findAllByOrderByCodeActionAsc();
}
