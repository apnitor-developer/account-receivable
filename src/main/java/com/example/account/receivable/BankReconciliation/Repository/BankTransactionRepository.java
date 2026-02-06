package com.example.account.receivable.BankReconciliation.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.BankReconciliation.Entity.BankTransaction;
import com.example.account.receivable.BankReconciliation.Enum.PaymentStatus;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findByCompany_Id(Long companyId);

    @Query("""
        SELECT bt
        FROM BankTransaction bt
        WHERE bt.company.id = :companyId
        AND bt.status = :status
        AND (:fromDate IS NULL OR bt.transactionDate >= :fromDate)
        AND (:toDate IS NULL OR bt.transactionDate <= :toDate)
    """)
    List<BankTransaction> findByCompanyIdFiltered(
            @Param("companyId") Long companyId,
            @Param("status") PaymentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
