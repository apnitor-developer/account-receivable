package com.example.account.receivable.ERA.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.ERA.Entity.EraClaim;

public interface EraClaimRepository extends JpaRepository<EraClaim, Long> {
    List<EraClaim> findByBatch_Id(Long batchId);
}
