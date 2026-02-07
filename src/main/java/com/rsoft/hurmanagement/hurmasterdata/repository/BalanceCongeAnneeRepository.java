package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.BalanceCongeAnnee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceCongeAnneeRepository extends JpaRepository<BalanceCongeAnnee, Long> {
    List<BalanceCongeAnnee> findByBalanceCongeIdOrderByAnneeDesc(Long balanceCongeId);

    java.util.Optional<BalanceCongeAnnee> findByBalanceCongeIdAndAnnee(Long balanceCongeId, Integer annee);
}
