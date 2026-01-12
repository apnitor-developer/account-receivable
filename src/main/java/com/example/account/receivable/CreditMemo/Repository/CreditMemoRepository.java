package com.example.account.receivable.CreditMemo.Repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.CreditMemo.Entity.CreditMemo;

public interface CreditMemoRepository extends JpaRepository<CreditMemo, Long> {
    boolean existsByCreditMemoNo(String creditMemoNo);

    List<CreditMemo> findByCustomerId(Long customerId);

    @Query("""
        SELECT COALESCE(SUM(cm.amount), 0)
        FROM CreditMemo cm
        WHERE cm.customer.id = :customerId
        AND cm.status = 'Posted'
    """)
    BigDecimal getCustomerTotalPostedCredits(
            @Param("customerId") Long customerId
    );
    

    @Query("""
        SELECT cm
        FROM CreditMemo cm
        JOIN cm.customer c
        JOIN CompanyCustomers cc ON cc.customer = c
        WHERE cc.company.id = :companyId
          AND (:status IS NULL OR cm.status = :status)
        ORDER BY cm.createdAt DESC
    """)
    Page<CreditMemo> findCompanyCreditMemos(
            @Param("companyId") Long companyId,
            @Param("status") String status,
            Pageable pageable
    );
}

