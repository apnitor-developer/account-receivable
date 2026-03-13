package com.example.account.receivable.LateFee.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.LateFee.Entity.LateFeeRule;

public interface LateFeeRuleRepository extends JpaRepository<LateFeeRule, Long> {

    Optional<LateFeeRule> findByCompanyIdAndDelatedFalse(Long companyId);

    boolean existsByCompanyIdAndDelatedFalse(Long companyId);
}
