package com.example.account.receivable.CreditMemo.Repository;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.CreditMemo.Entity.CreditMemo;
import com.example.account.receivable.CreditMemo.StatusFile.CreditMemoStatus;

public interface CreditMemoRepository extends JpaRepository<CreditMemo, Long> {
    boolean existsByCreditMemoNo(String creditMemoNo);

    @Query("""
        SELECT COALESCE(SUM(cm.amount), 0)
        FROM CreditMemo cm
        WHERE cm.customer.id = :customerId
        AND cm.status = :status
    """)
    BigDecimal getCustomerTotalCreditsByStatus(
            @Param("customerId") Long customerId,
            @Param("status") CreditMemoStatus status
    );
    
    @Query("""
        SELECT cm
        FROM CreditMemo cm
        JOIN CompanyCustomers cc ON cc.customer = cm.customer
        WHERE cc.company.id = :companyId
        AND (:status IS NULL OR cm.status = :status)
    """)
    Page<CreditMemo> findCompanyCreditMemos(
            @Param("companyId") Long companyId,
            @Param("status") CreditMemoStatus status,
            Pageable pageable
    );

}

