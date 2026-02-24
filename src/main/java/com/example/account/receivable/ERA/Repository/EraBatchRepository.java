package com.example.account.receivable.ERA.Repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.ERA.Entity.EraBatch;

public interface EraBatchRepository extends JpaRepository<EraBatch, Long> {

    Optional<EraBatch> findByPayerNameAndTotalPayment(
        String payerName,
        BigDecimal totalPayment
    );
}
