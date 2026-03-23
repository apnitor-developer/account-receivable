package com.example.account.receivable.ERA.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.ERA.Entity.EraBatch;

public interface EraBatchRepository extends JpaRepository<EraBatch, Long> {

    Optional<EraBatch> findByPayerNameAndTotalPayment(
        String payerName,
        BigDecimal totalPayment
    );

    Optional<EraBatch> findByTraceNumber(String traceNumber);

    @Query("""
        SELECT b FROM EraBatch b
        WHERE UPPER(TRIM(b.payerName)) = UPPER(TRIM(:payerName))
          AND b.totalPayment = :totalPayment
    """)
    Optional<EraBatch> findByPayerNameIgnoreCaseAndTotalPayment(
        @Param("payerName") String payerName,
        @Param("totalPayment") BigDecimal totalPayment
    );

    List<EraBatch> findByTotalPayment(BigDecimal totalPayment);
}
